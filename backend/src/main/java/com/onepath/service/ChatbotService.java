package com.onepath.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onepath.dto.*;
import com.onepath.entity.User;
import com.onepath.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private static final String DEFAULT_GEMINI_MODEL = "gemini-2.5-flash";
    private static final List<String> FALLBACK_MODELS = List.of("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash");

    private final CivicServiceService civicServiceService;
    private final CollegeService collegeService;
    private final ChecklistService checklistService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${ai.api.key:${AI_API_KEY:}}")
    private String apiKey;

    @Value("${ai.model:${AI_MODEL:gemini-2.5-flash}}")
    private String modelName;

    public ChatbotService(
            CivicServiceService civicServiceService,
            CollegeService collegeService,
            ChecklistService checklistService,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.civicServiceService = civicServiceService;
        this.collegeService = collegeService;
        this.checklistService = checklistService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public ChatResponseDto processChat(ChatRequestDto request) {
        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMessage.isEmpty()) {
            return ChatResponseDto.error("Please enter a valid message.");
        }

        // Check if API key is provided
        String effectiveKey = getEffectiveApiKey();
        boolean hasKey = effectiveKey != null && !effectiveKey.trim().isEmpty();

        // 1. Gather verified system context from OnePath database & current user context
        String systemKnowledge = buildSystemKnowledgeContext(request.getCurrentPage());

        if (!hasKey) {
            // Intelligent fallback directly from database when AI_API_KEY is not yet configured
            return handleKeyNotConfigured(userMessage);
        }

        // 2. Call Google Gemini API
        try {
            return callGeminiApi(effectiveKey, userMessage, request.getConversationHistory(), systemKnowledge);
        } catch (HttpTimeoutException te) {
            log.error("AI Provider request timed out");
            return ChatResponseDto.error("The AI service response timed out. Please try asking again in a moment.");
        } catch (Exception e) {
            log.error("Error communicating with AI Provider: {}", e.getMessage());
            return ChatResponseDto.error("Unable to contact the AI assistant at the moment. Please try again shortly.");
        }
    }

    private String getEffectiveApiKey() {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey.trim();
        }
        String envKey = System.getenv("AI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String geminiEnv = System.getenv("GEMINI_API_KEY");
        if (geminiEnv != null && !geminiEnv.trim().isEmpty()) {
            return geminiEnv.trim();
        }
        return null;
    }

    private String sanitizeModelName(String model) {
        if (model == null) return DEFAULT_GEMINI_MODEL;
        String clean = model.trim();
        if (clean.toLowerCase().startsWith("models/")) {
            clean = clean.substring(7);
        }
        clean = clean.replaceAll("^/+", "").replaceAll("/+$", "").trim();
        return clean.isEmpty() ? DEFAULT_GEMINI_MODEL : clean;
    }

    private ChatResponseDto callGeminiApi(
            String key,
            String userMessage,
            List<ChatMessageDto> history,
            String systemKnowledge) throws Exception {

        String primaryModel = sanitizeModelName(modelName);

        ObjectNode rootNode = objectMapper.createObjectNode();

        // System Instruction (Official Google Gemini v1beta REST field: system_instruction)
        ObjectNode systemInstructionNode = rootNode.putObject("system_instruction");
        ArrayNode sysParts = systemInstructionNode.putArray("parts");
        sysParts.addObject().put("text", systemKnowledge);

        // Contents (History + Current Prompt)
        ArrayNode contentsNode = rootNode.putArray("contents");

        if (history != null && !history.isEmpty()) {
            // Include up to last 8 turns for conversational continuity
            int startIdx = Math.max(0, history.size() - 8);
            for (int i = startIdx; i < history.size(); i++) {
                ChatMessageDto msg = history.get(i);
                if (msg.getContent() == null || msg.getContent().trim().isEmpty()) continue;
                String role = "assistant".equalsIgnoreCase(msg.getRole()) || "model".equalsIgnoreCase(msg.getRole())
                        ? "model"
                        : "user";
                ObjectNode turn = contentsNode.addObject();
                turn.put("role", role);
                ArrayNode parts = turn.putArray("parts");
                parts.addObject().put("text", msg.getContent().trim());
            }
        }

        // Current message
        ObjectNode currentTurn = contentsNode.addObject();
        currentTurn.put("role", "user");
        ArrayNode currentParts = currentTurn.putArray("parts");
        currentParts.addObject().put("text", userMessage);

        // Generation Config
        ObjectNode genConfig = rootNode.putObject("generationConfig");
        genConfig.put("temperature", 0.4);
        genConfig.put("maxOutputTokens", 1500);

        String requestJson = objectMapper.writeValueAsString(rootNode);

        // Candidate models list: primary requested model first, then known official fallback models
        List<String> candidateModels = new ArrayList<>();
        candidateModels.add(primaryModel);
        for (String fallback : FALLBACK_MODELS) {
            if (!candidateModels.contains(fallback)) {
                candidateModels.add(fallback);
            }
        }

        HttpResponse<String> response = null;
        String attemptedModel = primaryModel;
        int statusCode = 0;
        String responseBody = "";

        for (String modelToTry : candidateModels) {
            attemptedModel = modelToTry;
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + modelToTry + ":generateContent";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", key)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            statusCode = response.statusCode();
            responseBody = response.body();

            if (statusCode == 200) {
                break;
            }

            // Only retry with alternate model if 404 (model not found / deprecated for this API key)
            if (statusCode != 404) {
                break;
            }
            log.warn("Gemini model '{}' returned HTTP 404. Attempting fallback model...", modelToTry);
        }

        // Extract Google API error message if available
        String googleErrorMessage = "";
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                JsonNode errRoot = objectMapper.readTree(responseBody);
                googleErrorMessage = errRoot.path("error").path("message").asText("").trim();
            } catch (Exception ignored) {}
        }

        if (statusCode == 200) {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode candidates = responseJson.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode contentNode = candidates.get(0).path("content");
                JsonNode parts = contentNode.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String reply = parts.get(0).path("text").asText("").trim();
                    if (!reply.isEmpty()) {
                        List<String> suggested = generateSmartFollowups(userMessage, reply);
                        return ChatResponseDto.success(reply, suggested);
                    }
                }
                String finishReason = candidates.get(0).path("finishReason").asText("");
                if (!finishReason.isEmpty() && !"STOP".equalsIgnoreCase(finishReason)) {
                    log.warn("Gemini generation finished with reason: {}", finishReason);
                    return ChatResponseDto.error("The AI assistant response was flagged or stopped by safety filters (" + finishReason + "). Please rephrase your question.");
                }
            }
            return ChatResponseDto.error("Received an empty response from AI provider. Please rephrase your question.");
        } else if (statusCode == 429) {
            log.warn("Gemini API rate limit reached (HTTP 429)");
            return ChatResponseDto.error("The Google Gemini rate limit has been reached. Please wait a few seconds and try again.");
        } else if (statusCode == 400 || statusCode == 403) {
            log.warn("Gemini API rejected request with status code: {}", statusCode);
            return ChatResponseDto.error("Google Gemini authentication error (HTTP " + statusCode + "): " +
                    (!googleErrorMessage.isEmpty() ? googleErrorMessage : "The API key was rejected. Please verify AI_API_KEY and ensure the Generative Language API is enabled."));
        } else if (statusCode == 404) {
            log.warn("Gemini API returned 404 Not Found for model: {}", attemptedModel);
            return ChatResponseDto.error("Google Gemini model not found (HTTP 404): " +
                    (!googleErrorMessage.isEmpty() ? googleErrorMessage : "Model '" + attemptedModel + "' was not found.") +
                    " You can specify an active model with the AI_MODEL environment variable (e.g. gemini-2.5-flash or gemini-1.5-flash).");
        } else {
            log.error("Gemini API error status: {}", statusCode);
            return ChatResponseDto.error("Google Gemini returned HTTP " + statusCode +
                    (!googleErrorMessage.isEmpty() ? ": " + googleErrorMessage : ". Please try again shortly."));
        }
    }

    private String buildSystemKnowledgeContext(String currentPage) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the OnePath Assistant, the official AI guide embedded in the OnePath web platform.\n");
        sb.append("OnePath's mission: 'Understand. Apply. Stay Updated.' We simplify complex Indian government citizen services and college administrative procedures.\n\n");

        sb.append("CORE RULES:\n");
        sb.append("1. GROUND TRUTH: Base all procedural steps, eligibility criteria, fees, timelines, and URLs strictly on the verified database information below.\n");
        sb.append("2. ACCURACY: Never invent government fees, eligibility requirements, or URLs. If information is not in the data, state clearly that it is not available in OnePath.\n");
        sb.append("3. OFFICIAL LINKS: When explaining where to apply, provide the official government URL from the data (e.g. https://www.passportindia.gov.in, https://sarathi.parivahan.gov.in, https://serviceonline.bihar.gov.in, etc.).\n");
        sb.append("4. TONE: Friendly, helpful, clear, and empathetic to students and ordinary citizens. Use markdown bullet points and bold headers for readability.\n");
        sb.append("5. SMART NAVIGATOR: Inform users that for a personalized assessment (tailored fees, documents, and pathway), they can open the service page on OnePath and use the Smart Navigator questionnaire.\n");
        sb.append("6. NEVER claim an external application has been officially submitted or processed.\n\n");

        // Current user context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                sb.append("ACTIVE LOGGED-IN USER CONTEXT:\n");
                sb.append("- User Name: ").append(user.getName()).append("\n");
                sb.append("- User Role: ").append(user.getRole()).append("\n");

                try {
                    List<ChecklistItemDto> missing = checklistService.getUserMissingDocuments(user.getId());
                    if (missing.isEmpty()) {
                        sb.append("- Personal Document Checklist: All personal documents are currently marked complete.\n");
                    } else {
                        sb.append("- Personal Document Checklist (Pending / Missing Docs):\n");
                        for (ChecklistItemDto item : missing) {
                            sb.append("  * ").append(item.getDocumentName())
                                    .append(item.isMissing() ? " [Marked Missing]" : " [Pending]")
                                    .append("\n");
                        }
                    }
                } catch (Exception ignored) {}
                sb.append("\n");
            }
        } else {
            sb.append("USER CONTEXT: Visitor / Guest (Not logged in. For personal document checklists, recommend logging in or creating a free OnePath account).\n\n");
        }

        if (currentPage != null && !currentPage.trim().isEmpty()) {
            sb.append("CURRENT PAGE CONTEXT: The user is currently viewing ").append(currentPage).append(" on OnePath.\n\n");
        }

        // Verified Government Services from Database
        sb.append("VERIFIED GOVERNMENT & CIVIC SERVICES IN ONEPATH:\n\n");
        try {
            List<ServiceDto> services = civicServiceService.getAllServices(null, null);
            for (ServiceDto s : services) {
                sb.append("SERVICE: ").append(s.getName()).append(" (Slug: ").append(s.getSlug()).append(")\n");
                sb.append("- Category: ").append(s.getCategory()).append("\n");
                sb.append("- Description: ").append(s.getDescription()).append("\n");
                sb.append("- Eligibility: ").append(s.getEligibility()).append("\n");
                sb.append("- Official Fees: ").append(s.getFees()).append("\n");
                sb.append("- Timeline: ").append(s.getTimeline()).append("\n");
                sb.append("- Official Portal URL: ").append(s.getOfficialPortalUrl()).append("\n");
                sb.append("- Key Notes: ").append(s.getImportantNotes()).append("\n");

                if (s.getRequiredDocuments() != null && !s.getRequiredDocuments().isEmpty()) {
                    sb.append("- Standard Required Documents:\n");
                    for (ServiceRequiredDocumentDto doc : s.getRequiredDocuments()) {
                        sb.append("  * ").append(doc.getDocumentName())
                                .append(" (").append(doc.isMandatory() ? "Mandatory" : "Optional").append("): ")
                                .append(doc.getDescription())
                                .append(" [How to obtain: ").append(doc.getHowToObtain()).append("]\n");
                    }
                }

                if (s.getSteps() != null && !s.getSteps().isEmpty()) {
                    sb.append("- Application Steps:\n");
                    for (ServiceStepDto step : s.getSteps()) {
                        sb.append("  Step ").append(step.getStepNumber()).append(": ")
                                .append(step.getTitle()).append(" — ")
                                .append(step.getDescription())
                                .append(" (Estimated time: ").append(step.getEstimatedTime()).append(")\n");
                    }
                }
                sb.append("\n");
            }
        } catch (Exception e) {
            log.error("Error gathering services context: {}", e.getMessage());
        }

        // College Processes Context
        sb.append("COLLEGE & INSTITUTIONAL PROCESSES IN ONEPATH:\n");
        sb.append("- OnePath enables affiliated universities and colleges to digitize campus administrative procedures (such as Bonafide Certificates, Hostel Allotment, No-Dues clearance, Transcript requests).\n");
        sb.append("- Students can search their college under the '/colleges' directory to view institutional deadlines, verified steps, required department signatures, and official circulars/notices.\n\n");

        return sb.toString();
    }

    private ChatResponseDto handleKeyNotConfigured(String userMessage) {
        String lower = userMessage.toLowerCase();
        StringBuilder reply = new StringBuilder();

        reply.append("Hello! I am your **OnePath Assistant**.\n\n");
        reply.append("> ℹ️ **Notice**: The server environment variable `AI_API_KEY` is not yet configured, but I can still provide verified information directly from OnePath's database:\n\n");

        if (lower.contains("passport")) {
            reply.append("### 🛂 Passport Application & Renewal\n");
            reply.append("- **Official Portal**: [Passport Seva Portal](https://www.passportindia.gov.in)\n");
            reply.append("- **Fees (Effective July 2026)**:\n");
            reply.append("  * Fresh / Re-issue (36 Pages, Normal): **₹1,500**\n");
            reply.append("  * Fresh / Re-issue (60 Pages Jumbo, Normal): **₹2,000**\n");
            reply.append("  * Minor below 18 years: **₹1,000**\n");
            reply.append("  * Tatkaal additional fee: **+₹2,000** (Total ₹3,500 / ₹4,000)\n");
            reply.append("- **Core Documents**: Aadhaar Card (Address & DOB proof), 10th marksheet (Non-ECR), and Student Bonafide/Hostel certificate (for college students).\n");
            reply.append("- **Smart Navigator**: Visit the [Passport Service Page](/services/passport-application-renewal) to run the questionnaire for your personalized pathway.");

            return ChatResponseDto.notConfigured(reply.toString());
        } else if (lower.contains("driving") || lower.contains("licence") || lower.contains("license") || lower.contains("rc") || lower.contains("vehicle")) {
            reply.append("### 🚗 Driving Licence & Vehicle Registration\n");
            reply.append("- **Official Portal**: [Sarathi Parivahan](https://sarathi.parivahan.gov.in) (Licence) / [Vahan](https://vahan.parivahan.gov.in) (RC)\n");
            reply.append("- **Fees (CMVR Rule 32 & 81)**:\n");
            reply.append("  * Learner's Licence (LL): **₹200** single class / **₹350** dual (MCWG + LMV)\n");
            reply.append("  * Permanent DL: **₹700** single class / **₹1,000** dual (post practical test)\n");
            reply.append("  * DL Renewal / Duplicate: **₹400**\n");
            reply.append("  * Vehicle RC: **₹300** two-wheeler / **₹600** car (+ state road tax)\n");
            reply.append("- **Golden Rule**: You MUST hold an active Learner's Licence for at least 30 days before booking the permanent driving test.\n");
            reply.append("- **Smart Navigator**: Visit the [Driving Licence Page](/services/driving-licence-vehicle-registration) to determine your pathway.");

            return ChatResponseDto.notConfigured(reply.toString());
        } else if (lower.contains("ews") || lower.contains("income") || lower.contains("caste")) {
            reply.append("### 📜 EWS, Income & Caste Certificates\n");
            reply.append("- **Central EWS Criteria (DoPT Memo 2019)**:\n");
            reply.append("  * Category: Must belong to **General / Unreserved** category (SC, ST, and OBC candidates are covered under separate quotas).\n");
            reply.append("  * Gross Annual Family Income: Strictly **below ₹8,00,000 (< ₹8 LPA)**.\n");
            reply.append("  * Asset Limits: Must NOT own 5+ acres agricultural land, 1,000+ sq ft residential flat, or 100/200 sq yd plots.\n");
            reply.append("  * Validity: **1 Financial Year** (requires annual renewal).\n");
            reply.append("- **Portals & Fees**: Governed by state e-District portals (e.g. Aaple Sarkar in MH: ₹20; eDistrict UP: ₹15; Nadakacheri in KA: ₹25; Delhi: Free).\n");
            reply.append("- **Smart Navigator**: Visit the [EWS, Income & Caste Page](/services/ews-income-caste-certificate) for state-specific guidance.");

            return ChatResponseDto.notConfigured(reply.toString());
        } else {
            reply.append("OnePath helps you easily navigate official procedures for:\n");
            reply.append("1. **Passport Application & Renewal** (Fresh, Tatkaal, Student hostel proof)\n");
            reply.append("2. **Driving Licence & Vehicle Registration** (LL, Permanent DL, RC transfer)\n");
            reply.append("3. **EWS / Income & Caste Certificate Issuance** (State e-District & Central DoPT)\n");
            reply.append("4. **College Administrative Processes** (Transcripts, Bonafide certificates, circulars)\n\n");
            reply.append("To enable full natural conversational AI with Google Gemini, simply configure the `AI_API_KEY` environment variable on the server.");

            return ChatResponseDto.notConfigured(reply.toString());
        }
    }

    private List<String> generateSmartFollowups(String userMessage, String reply) {
        String lower = userMessage.toLowerCase();
        List<String> list = new ArrayList<>();

        if (lower.contains("passport")) {
            list.add("What documents do college students need for Passport?");
            list.add("What is the Tatkaal passport fee?");
            list.add("How does Police Verification work?");
        } else if (lower.contains("driving") || lower.contains("licence") || lower.contains("dl") || lower.contains("rc")) {
            list.add("Can I take the Learner's Licence test from home?");
            list.add("What is the 30-day Learner's Licence rule?");
            list.add("What documents are needed for vehicle RC?");
        } else if (lower.contains("ews") || lower.contains("caste") || lower.contains("income")) {
            list.add("What are the excluded assets for EWS?");
            list.add("Can OBC or SC/ST apply for EWS?");
            list.add("How do I get an OBC Non-Creamy Layer certificate?");
        } else {
            list.add("How do I apply for a Passport?");
            list.add("What are the fees for a Driving Licence?");
            list.add("Am I eligible for EWS reservation?");
        }
        return list;
    }
}
