package com.onepath.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepath.dto.ChatRequestDto;
import com.onepath.dto.ChatResponseDto;
import com.onepath.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ChatbotServiceTest {

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        // Mock only interfaces (works seamlessly on any Java version without ByteBuddy bytecode rewriting)
        ServiceRepository serviceRepo = mock(ServiceRepository.class);
        CollegeRepository collegeRepo = mock(CollegeRepository.class);
        CollegeProcessRepository processRepo = mock(CollegeProcessRepository.class);
        CollegeProcessStepRepository stepRepo = mock(CollegeProcessStepRepository.class);
        CollegeRequiredDocumentRepository docRepo = mock(CollegeRequiredDocumentRepository.class);
        CollegeProcessUpdateRepository updateRepo = mock(CollegeProcessUpdateRepository.class);
        UserDocumentChecklistRepository checklistRepo = mock(UserDocumentChecklistRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        CivicServiceService civicServiceService = new CivicServiceService(serviceRepo, Collections.emptyList());
        CollegeService collegeService = new CollegeService(collegeRepo, processRepo, stepRepo, docRepo, updateRepo);
        ChecklistService checklistService = new ChecklistService(checklistRepo);
        ObjectMapper objectMapper = new ObjectMapper();

        chatbotService = new ChatbotService(
                civicServiceService,
                collegeService,
                checklistService,
                userRepo,
                objectMapper
        );
    }

    @Test
    @DisplayName("Should validate empty chat message")
    void testEmptyMessage() {
        ChatRequestDto req = new ChatRequestDto("");
        ChatResponseDto res = chatbotService.processChat(req);
        assertNotNull(res);
        assertTrue(res.isError());
        assertTrue(res.getReply().contains("valid message"));
    }

    @Test
    @DisplayName("Should return verified Passport information when API key is unconfigured")
    void testPassportFallbackWithoutKey() {
        ChatRequestDto req = new ChatRequestDto("How do I apply for a passport?");
        ChatResponseDto res = chatbotService.processChat(req);

        assertNotNull(res);
        assertFalse(res.isConfigured());
        assertFalse(res.isError());
        assertTrue(res.getReply().contains("Passport Application & Renewal"));
        assertTrue(res.getReply().contains("passportindia.gov.in"));
        assertTrue(res.getReply().contains("₹1,500"));
        assertFalse(res.getSuggestedActions().isEmpty());
    }

    @Test
    @DisplayName("Should return verified Driving Licence information when API key is unconfigured")
    void testDrivingLicenceFallbackWithoutKey() {
        ChatRequestDto req = new ChatRequestDto("What are the driving licence fees and rules?");
        ChatResponseDto res = chatbotService.processChat(req);

        assertNotNull(res);
        assertFalse(res.isConfigured());
        assertTrue(res.getReply().contains("Driving Licence & Vehicle Registration"));
        assertTrue(res.getReply().contains("sarathi.parivahan.gov.in"));
        assertTrue(res.getReply().contains("30 days"));
        assertTrue(res.getReply().contains("₹700"));
    }

    @Test
    @DisplayName("Should return verified EWS certificate information when API key is unconfigured")
    void testEwsFallbackWithoutKey() {
        ChatRequestDto req = new ChatRequestDto("What are the eligibility rules for an EWS certificate?");
        ChatResponseDto res = chatbotService.processChat(req);

        assertNotNull(res);
        assertFalse(res.isConfigured());
        assertTrue(res.getReply().contains("EWS"));
        assertTrue(res.getReply().contains("General / Unreserved"));
        assertTrue(res.getReply().contains("8,00,000"));
        assertTrue(res.getReply().contains("5+ acres"));
    }

    @Test
    @DisplayName("Should return general OnePath guidance for general questions when key is unconfigured")
    void testGeneralGuidanceFallback() {
        ChatRequestDto req = new ChatRequestDto("How does OnePath work?");
        ChatResponseDto res = chatbotService.processChat(req);

        assertNotNull(res);
        assertFalse(res.isConfigured());
        assertTrue(res.getReply().contains("OnePath"));
        assertTrue(res.getReply().contains("AI_API_KEY"));
    }
}
