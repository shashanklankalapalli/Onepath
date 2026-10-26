package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Master Pathway Evaluator for Property / Land Revenue Registration
 * based on the Registration Act 1908 (Section 17/28), Indian Stamp Act 1899,
 * Andhra Pradesh Stamps & Registration Department (AP IGRS), and AP Revenue Department standards.
 */
@Component
public class PropertyRegistrationPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String PROPERTY_SLUG = "property-land-revenue-registration";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return PROPERTY_SLUG.equalsIgnoreCase(s)
                || "property-registration".equalsIgnoreCase(s)
                || "land-registration".equalsIgnoreCase(s)
                || "property-land-revenue".equalsIgnoreCase(s)
                || "land-revenue-registration".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(PROPERTY_SLUG);
        dto.setServiceName(service != null ? service.getName() : "Property / Land Revenue Registration");
        dto.setTitle("Property & Land Revenue Registration Navigator (Andhra Pradesh)");
        dto.setDescription("Answer a few quick questions to identify your applicable property pathway, calculate statutory stamp duty and registration rules, check 30-year document stacks, and understand post-registration revenue mutation (RoR-1B/Patta).");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: What do you need?
        questions.add(new QuestionnaireQuestionDto(
                "serviceRequirement",
                "What property or land service do you require?",
                "Identify your specific transaction or documentation requirement under Andhra Pradesh revenue & registration guidelines.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("SALE_PURCHASE_REGISTRATION", "Property Sale / Purchase Registration", "Conveyance deed registration of plots, flats, residential, agricultural, or commercial properties at Sub-Registrar Office.", "Full SRO Conveyancing"),
                        new QuestionOptionDto("ENCUMBRANCE_CERTIFICATE", "Encumbrance Certificate (EC Search)", "Check registered transactions, prior mortgages, liens, and attachments for a property (Form 15/16).", "Title Lien Verification"),
                        new QuestionOptionDto("LAND_RECORD_SEARCH", "Land & Property Record Search", "Find and view Adangal / Pahani, RoR-1B, survey village maps, and certified registered deeds.", "Official Record Search"),
                        new QuestionOptionDto("MUTATION_RECORD_UPDATE", "Mutation / Ownership Record Update", "Update land revenue records, Patta passbook, or municipal property tax title transfer after deed registration.", "Revenue Record Update"),
                        new QuestionOptionDto("LAND_RECORD_CORRECTION", "Land Record Correction / Grievance", "Rectify survey number, extent, boundary overlap, or name spelling errors in revenue records.", "Grievance Redressal"),
                        new QuestionOptionDto("OTHER_PROPERTY_SERVICE", "Other Deed Registration (Gift / Lease / Mortgage)", "Registration of Gift Deeds to family, Release Deeds, Settlement Deeds, or Long-Term Leases (>11 months).", "Special Instruments")
                )
        ));

        // Q2: Is property located in Andhra Pradesh?
        questions.add(new QuestionnaireQuestionDto(
                "isAndhraPradesh",
                "Is the property located in Andhra Pradesh?",
                "Under Section 28 of the Registration Act 1908, deeds must be registered at the Sub-Registrar Office within whose jurisdiction the property physically lies.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("AP_YES", "Yes, Property is in Andhra Pradesh", "Governed by AP Registration & Stamps Department (AP IGRS) and AP Revenue Department rules."),
                        new QuestionOptionDto("AP_NO", "No, Located in Another State / UT", "Must be registered in the specific State/UT where the property is geographically situated.")
                )
        ));

        // Q3: Urban or Rural?
        questions.add(new QuestionnaireQuestionDto(
                "propertyType",
                "Is the property urban or rural / agricultural?",
                "Determines whether municipal plan sanctions, local body cess, or agricultural Patta passbooks apply.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("URBAN", "Urban Property (Municipal Corporation / Municipality / Approved Layout)", "Flats, apartments, residential houses, commercial buildings, or DTCP/APCRDA approved layout plots."),
                        new QuestionOptionDto("RURAL_AGRICULTURAL", "Rural / Agricultural Land (Gram Panchayat / Revenue Village)", "Agricultural farm land, dry/wet crop parcels, or rural village residential sites.")
                )
        ));

        // Q4: Status of existing documents
        questions.add(new QuestionnaireQuestionDto(
                "existingDocumentsStatus",
                "Do you have the existing property title and link documents?",
                "A clean unbroken chain of parent documents spanning at least 30 years is required for marketable title.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("DOCS_AVAILABLE", "Complete Original Title Deeds & 30-Year Link Chain", "All prior sale deeds, settlement deeds, and current tax/patta records are readily available."),
                        new QuestionOptionDto("DOCS_PARTIAL", "Partial Link Documents / Some Missing Prior Deeds", "Core deed is available but intermediate links are missing; certified copies needed from SRO."),
                        new QuestionOptionDto("DOCS_NEED_SEARCH", "Need to Search & Retrieve Records from Scratch", "Do not have document copies; need to inspect SRO Index II or revenue records to verify title.")
                )
        ));

        // Q5: Has sale deed already been registered?
        questions.add(new QuestionnaireQuestionDto(
                "isSaleDeedRegistered",
                "Has the sale deed already been registered at the Sub-Registrar Office (SRO)?",
                "Registration records the conveyance; ownership in government land revenue records requires subsequent mutation.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("DEED_NOT_REGISTERED", "Not Yet Registered (Planning SRO Registration)", "Deed requires drafting, circle rate computation, e-Stamping, and SRO slot booking."),
                        new QuestionOptionDto("DEED_REGISTERED_NEED_MUTATION", "Already Registered at SRO (Awaiting Revenue Mutation)", "SRO registration completed; need mutation in Village/Mandal revenue records (RoR-1B/Patta)."),
                        new QuestionOptionDto("DEED_NOT_APPLICABLE", "Not Applicable (Seeking EC, Record Search, or Correction Only)", "Non-conveyancing service inquiry.")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        String requirement = answers.getOrDefault("serviceRequirement", "SALE_PURCHASE_REGISTRATION");
        String isAp = answers.getOrDefault("isAndhraPradesh", "AP_YES");
        String propertyType = answers.getOrDefault("propertyType", "URBAN");
        String docStatus = answers.getOrDefault("existingDocumentsStatus", "DOCS_AVAILABLE");
        String deedRegistered = answers.getOrDefault("isSaleDeedRegistered", "DEED_NOT_REGISTERED");

        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();
        result.setOfficialPortalUrl("https://registration.ap.gov.in/");

        boolean isAndhra = "AP_YES".equalsIgnoreCase(isAp);
        boolean isUrban = "URBAN".equalsIgnoreCase(propertyType);
        boolean isAlreadyRegistered = "DEED_REGISTERED_NEED_MUTATION".equalsIgnoreCase(deedRegistered);

        // Standard statutory guidance banner
        result.setEmigrationStatus("OnePath Regulatory Guidance (Information only, not formal legal advice or an official eligibility determination. Verify all deeds and title searches with licensed conveyancers and SRO).");

        // 1. Out-of-State Property
        if (!isAndhra) {
            result.setPathwayCode("OUT_OF_STATE_PROPERTY");
            result.setPathwayTitle("Out-of-State Property Notice (State Jurisdiction Requirement)");
            result.setBadgeText("State Jurisdiction Mandate");
            result.setMatchReason("Based on your answers: The property is located outside Andhra Pradesh. Under Section 28 of the Registration Act 1908, immovable property documents must be presented for registration strictly within the jurisdiction of the Sub-Registrar Office where the property is physically located.");
            result.setEstimatedTimeline("Varies by State");
            result.setCalculatedFee("Refer to Respective State Portal");
            result.setFeeBreakdown("State stamp duty and registration rules vary. The AP Registration & Stamps Department cannot register or mutate properties situated in other States/UTs.");
            result.setPoliceVerificationMode("Respective State Registration Authority");

            result.setSteps(Arrays.asList(
                    new ServiceStepDto(1L, 1, "Identify Local Sub-Registrar Office", "Locate the specific Sub-Registrar Office (SRO) in the state and district where the property is physically situated.", "1 Day", "Use the state's official registration and stamp department portal."),
                    new ServiceStepDto(2L, 2, "Check State Guidance Values & Stamp Duty", "Compute the minimum circle rate / guidance value applicable in that specific state.", "1 Day", "Different states have different stamp duty rates (typically 4% to 8%)."),
                    new ServiceStepDto(3L, 3, "Draft Deed & Procure State E-Stamps", "Engage a local document writer/advocate to draft the deed conforming to that state's stamp laws.", "2–3 Days", "Procure e-Stamps through authorized SHCIL or state treasury vendor."),
                    new ServiceStepDto(4L, 4, "Register Deed & Apply for Local Mutation", "Attend local SRO with parties and witnesses; subsequently apply for mutation in local land revenue records.", "1–30 Days", "Collect Index II and proceed with Jamabandi/Patta mutation.")
            ));

            result.setDocuments(Arrays.asList(
                    new ServiceRequiredDocumentDto(1L, "Original Title Deeds of Property", "Parent documents tracing unbroken title for minimum 30 years.", true, "Originals + 1 Set Photocopies", "Procure from seller or financial institution."),
                    new ServiceRequiredDocumentDto(2L, "Identity & Address Proofs of Parties", "Aadhaar Card, PAN Card, Voter ID of buyer, seller, and two credible adult witnesses.", true, "Original Photo ID + 1 self-attested copy", "Carry originals to SRO."),
                    new ServiceRequiredDocumentDto(3L, "Drafted Conveyance Deed", "Deed drafted as per state-specific format printed on legal stamp paper.", true, "Physical Deed + Annexures", "Prepare through licensed document writer.")
            ));

            result.setCriticalTips(Arrays.asList(
                    "Jurisdictional Restriction: Property registration cannot take place in Andhra Pradesh if the land or building is in another state.",
                    "Section 28 Compliance: Any deed registered in a wrong jurisdiction is legally void and confers no marketable title.",
                    "Always conduct a 30-year Encumbrance Certificate (EC) search in the respective state's Sub-Registrar records prior to executing deeds."
            ));

            return result;
        }

        // 2. Encumbrance Certificate (EC) Pathway
        if ("ENCUMBRANCE_CERTIFICATE".equalsIgnoreCase(requirement)) {
            result.setPathwayCode("PROPERTY_ENCUMBRANCE_CERTIFICATE_AP");
            result.setPathwayTitle("Encumbrance Certificate (EC) Search & Verification (Form 15 / Form 16)");
            result.setBadgeText("Title Lien Verification");
            result.setMatchReason("Based on your answers: You require an Encumbrance Certificate (EC) to verify registered transactions, prior mortgages, court attachments, or verify that the property has a clean and unencumbered title.");
            result.setEstimatedTimeline("Instant (Online Search) / 24–48 Hours (Certified Signed EC)");
            result.setCalculatedFee("₹25 – ₹200 (Nominal User Charge)");
            result.setFeeBreakdown("Online view/inspection of Form 15/16 is nominal on AP IGRS portal (https://registration.ap.gov.in/). Digitally signed certified Encumbrance Certificates via MeeSeva cost ~₹25 to ₹200 depending on the search duration (minimum 30 years recommended).");
            result.setPoliceVerificationMode("Sub-Registrar Digital Record Search (Index II)");
            result.setValidity("Valid up to current search timestamp (30-Year search recommended)");

            result.setSteps(Arrays.asList(
                    new ServiceStepDto(1L, 1, "Access AP IGRS Portal or MeeSeva", "Visit official Andhra Pradesh Registration & Stamps portal (https://registration.ap.gov.in/) or MeeSeva portal (https://ap.meeseva.gov.in/).", "5–10 Minutes", "Keep property survey number, house number, or prior registered document number ready."),
                    new ServiceStepDto(2L, 2, "Enter Property Identification Parameters", "Search by Document Number / Year OR by Property Details (District, Mandal, Village, Survey No., Plot/House No., and Boundaries).", "10–15 Minutes", "Select search period: Enter at least a 30-year window (e.g., from 1995 to current year)."),
                    new ServiceStepDto(3L, 3, "Inspect Statement of Encumbrances (Form 15 vs 16)", "Review generated report. Form 15 lists all registered conveyances, gift deeds, partitions, and mortgages; Form 16 (Nil Encumbrance) certifies no transactions are recorded.", "15–30 Minutes", "Carefully check each transaction entry to confirm seller's title and absence of bank attachments."),
                    new ServiceStepDto(4L, 4, "Download Digitally Signed Certified EC", "Pay the nominal MeeSeva user fee to generate a QR-coded digitally signed EC for bank loans or legal title audits.", "Instant – 24 Hours", "Signed EC carries statutory legal evidentiary value in civil courts and bank mortgage approvals.")
            ));

            result.setDocuments(Arrays.asList(
                    new ServiceRequiredDocumentDto(1L, "Property Identification Particulars", "Survey number, sub-division, plot/flat number, boundary schedule, and village/mandal name.", true, "Details matching latest revenue / municipal records", "Extract from prior sale deed or property tax receipt."),
                    new ServiceRequiredDocumentDto(2L, "Previous Registered Document Number (if known)", "Registration number, volume, and year of the last registered transaction at SRO.", false, "Number / Year notation (e.g., Doc 1234/2018 of SRO Vijayawada)", "Found on back page or stamp endorsement of old deed."),
                    new ServiceRequiredDocumentDto(3L, "Applicant Aadhaar Card", "Identity proof of person applying for certified EC copy via MeeSeva.", true, "Aadhaar Card / e-Aadhaar", "Download from uidai.gov.in.")
            ));

            result.setCriticalTips(Arrays.asList(
                    "30-Year Rule: Always perform an unbroken EC search spanning at least 30 years to uncover ancestral mortgages or prior registered court decrees.",
                    "Form 15 vs Form 16: If any transaction is registered on the property, the portal issues Form 15. If zero transactions are recorded in that window, Form 16 (Nil Encumbrance) is generated.",
                    "Notice on Unregistered Liens: An EC only reflects documents REGISTERED at the Sub-Registrar Office; oral partitions, unregistered family disputes, or revenue tax dues may not appear on an EC and require revenue/court checks."
            ));

            return result;
        }

        // 3. Land / Property Record Search
        if ("LAND_RECORD_SEARCH".equalsIgnoreCase(requirement)) {
            result.setPathwayCode("PROPERTY_LAND_RECORDS_SEARCH_AP");
            result.setPathwayTitle("Land & Property Record Search (Meebhoomi, Webland & Certified Deeds)");
            result.setBadgeText("Official Record Search");
            result.setMatchReason("Based on your answers: You need to search and verify official land records, Adangal/Pahani crop extracts, 1B Records of Rights (RoR), survey village maps, or certified copies of registered deeds in Andhra Pradesh.");
            result.setEstimatedTimeline("Instant (Online Verification) / 1–3 Working Days (Certified Extract)");
            result.setCalculatedFee("Free Online / ₹25 – ₹50 (Certified Signed Copy)");
            result.setFeeBreakdown("Public search on AP Meebhoomi portal is completely free. Certified digitally signed copies of 1B (RoR) or Adangal via MeeSeva carry a nominal fee of ₹25–₹50.");
            result.setPoliceVerificationMode("Village Revenue Officer (VRO) / Tahsildar Database");
            result.setValidity("Reflects current revenue ledger status");

            result.setSteps(Arrays.asList(
                    new ServiceStepDto(1L, 1, "Access Official AP Meebhoomi Portal", "Visit official Andhra Pradesh land records portal (meebhoomi.ap.gov.in) or MeeSeva.", "5–10 Minutes", "Select whether you are searching by Survey Number, Khata Number, Pattadar Name, or Aadhaar."),
                    new ServiceStepDto(2L, 2, "Search Adangal / Pahani & RoR-1B Extract", "Select District, Mandal, Village, and enter Survey / Khata No. to view current cultivator details, nature of land (patta vs government), and extent.", "10 Minutes", "Carefully check if the land is classified as Private Patta, D-Pattadar, Inam, or Government-assigned."),
                    new ServiceStepDto(3L, 3, "Check Section 22A Prohibited Property Status", "Verify on the AP IGRS portal (registration.ap.gov.in) under Prohibited Property Search to confirm parcel is clear.", "10 Minutes", "Lands under 22A(1)(a) to (e) cannot be sold or registered under AP law."),
                    new ServiceStepDto(4L, 4, "Obtain Certified Copies of Registered Deeds", "If missing prior deeds, apply for Certified Copy (CC) of registered deed on AP IGRS or MeeSeva by providing Doc No. and Year.", "1–3 Working Days", "Certified copies carry certified true copy seals and serve as valid link documents.")
            ));

            result.setDocuments(Arrays.asList(
                    new ServiceRequiredDocumentDto(1L, "Survey Number / Khata Number", "Revenue land identification number assigned during settlement / resurvey.", true, "Revenue reference string", "Found on old passbook, tax receipt, or village map."),
                    new ServiceRequiredDocumentDto(2L, "Village & Mandal Identification", "Exact revenue village, gram panchayat, and mandal name within the district.", true, "Standard revenue jurisdiction", "Obtain from local VRO office or village secretariat."),
                    new ServiceRequiredDocumentDto(3L, "Applicant Photo ID", "Aadhaar Card of the applicant requesting certified records.", true, "Aadhaar Card", "Download from uidai.gov.in.")
            ));

            result.setCriticalTips(Arrays.asList(
                    "Check 1B vs Adangal: 1B Record of Rights certifies legal ownership/title, while Adangal/Pahani records physical possession, soil type, and current agricultural crop.",
                    "Missing Link Documents: If intermediate link documents are lost, apply for Certified Copies (CC) from the jurisdictional SRO; these are legally valid secondary evidence.",
                    "Village Secretariat Assistance: Citizens can also visit their local Grama/Ward Sachivalayam to obtain digitally signed MeeSeva land record extracts."
            ));

            return result;
        }

        // 4. Mutation / Ownership Record Update (or Deed Registered needing Mutation)
        if ("MUTATION_RECORD_UPDATE".equalsIgnoreCase(requirement) || isAlreadyRegistered) {
            result.setPathwayCode("PROPERTY_MUTATION_AP");
            result.setPathwayTitle("Post-Registration Revenue Mutation & Title Passbook Update (Webland / RoR-1B)");
            result.setBadgeText("Revenue Mutation Workflow");
            result.setMatchReason("Based on your answers: The sale deed has already been registered at the Sub-Registrar Office, and you now need to mutate ownership in the revenue land management records (Webland / RoR-1B / Pattadar Passbook) or municipal property tax assessment.");
            result.setEstimatedTimeline("15 – 30 Working Days (Statutory Public Notice Period)");
            result.setCalculatedFee("Nominal User Fee (~₹35 – ₹100 via MeeSeva)");
            result.setFeeBreakdown("In Andhra Pradesh, auto-mutation is initiated via IGRS-Webland integration for registered deeds. Independent mutation petitions via MeeSeva carry a nominal citizen service charge (~₹35–₹100). No stamp duty is charged for revenue mutation.");
            result.setPoliceVerificationMode("Field Inspection & Notice by Tahsildar / VRO");
            result.setValidity("Permanent until subsequent conveyance or partition");

            result.setSteps(Arrays.asList(
                    new ServiceStepDto(1L, 1, "Verify Auto-Mutation Trigger from SRO", "Post-registration, check whether the Sub-Registrar has transmitted the deed details electronically to Webland via the IGRS-Revenue gateway.", "24–48 Hours", "Check application status on Webland or MeeSeva using Registered Doc No."),
                    new ServiceStepDto(2L, 2, "Submit Formal Mutation Application (if not auto-triggered)", "If auto-mutation is pending, submit a formal Mutation Petition through MeeSeva or Village Secretariat with registered deed scan.", "1 Day", "Upload registered sale deed, parent deeds, and Aadhaar card."),
                    new ServiceStepDto(3L, 3, "Physical Field Inspection by VRO & Revenue Inspector", "Village Revenue Officer (VRO) inspects physical possession, verifies survey boundary stones, and prepares field report.", "7–15 Days", "Ensure seller or adjacent landholders confirm boundary demarcation without disputes."),
                    new ServiceStepDto(4L, 4, "Statutory 15-Day Public Notice & Objection Period", "Tahsildar issues statutory public notice (Form VIII) inviting objections from interested parties or family members.", "15 Days", "If no legal objections or court injunctions are received within 15 days, Tahsildar approves mutation."),
                    new ServiceStepDto(5L, 5, "Issuance of New RoR-1B & Digital Pattadar Passbook", "Tahsildar enters buyer's name into Webland RoR-1B and issues new Electronic Pattadar Passbook / Title Deed (e-PDS).", "5–7 Days", "Download digitally signed 1B extract from Meebhoomi or collect physical passbook.")
            ));

            result.setDocuments(Arrays.asList(
                    new ServiceRequiredDocumentDto(1L, "Original Registered Sale Deed", "Registered document endorsed by Sub-Registrar with Book 1 volume and page number.", true, "Original registered booklet + self-attested copies", "Collected from SRO after registration."),
                    new ServiceRequiredDocumentDto(2L, "Seller's Existing Pattadar Passbook / Title Deed", "Old passbook/title deed of seller for cancellation/sub-division of sold extent.", true, "Original passbook / 1B extract", "Handed over by seller upon sale completion."),
                    new ServiceRequiredDocumentDto(3L, "Buyer & Seller Aadhaar Cards", "Biometric identity proofs for digital KYC verification in Webland.", true, "Aadhaar Card / e-Aadhaar", "Download from uidai.gov.in."),
                    new ServiceRequiredDocumentDto(4L, "FMB (Field Measurement Book) Sketch", "Cadastral map showing sub-division of survey parcel if partial land was purchased.", isUrban ? false : true, "Signed FMB sub-division sketch", "Prepared by Mandal Surveyor upon inspection.")
            ));

            result.setCriticalTips(Arrays.asList(
                    "Crucial Distinction: Registration at SRO only records the transfer transaction; government revenue ownership is NOT updated until the Tahsildar executes mutation in Webland.",
                    "Statutory 15-Day Notice: By law, a 15-day public notice window must elapse before the Tahsildar can approve mutation and issue an updated RoR-1B.",
                    "Municipal Property Tax Mutation: For urban buildings/flats, complete municipal assessment title transfer through the AP Town Planning / Municipal Corporation (CDMA) portal."
            ));

            return result;
        }

        // 5. Land Record Correction / Grievance
        if ("LAND_RECORD_CORRECTION".equalsIgnoreCase(requirement)) {
            result.setPathwayCode("PROPERTY_RECORD_CORRECTION_AP");
            result.setPathwayTitle("Land & Revenue Record Rectification (Survey / Extent / Name Correction)");
            result.setBadgeText("Revenue Record Rectification");
            result.setMatchReason("Based on your answers: You need to resolve discrepancies in revenue records, such as incorrect survey numbers, extent mismatches between registered deed and Webland, boundary overlap, or name misspellings.");
            result.setEstimatedTimeline("30 – 60 Working Days (Subject to Mandal Survey)");
            result.setCalculatedFee("Statutory User Fee (~₹50 – ₹100 via MeeSeva)");
            result.setFeeBreakdown("Nominal MeeSeva grievance fee (~₹50). If physical re-survey / FMB re-measurement is required, government surveyor challan fees (~₹100–₹500 per survey mark) apply via CFMS.");
            result.setPoliceVerificationMode("Joint Inspection by Mandal Surveyor & Tahsildar");
            result.setValidity("Updated permanent record in Webland");

            result.setSteps(Arrays.asList(
                    new ServiceStepDto(1L, 1, "Compile Error Documentation & Link Chain", "Identify exact mismatch between registered title deed and online Webland 1B / Adangal record.", "1–2 Days", "Collect certified copies of registered deeds and old passbook."),
                    new ServiceStepDto(2L, 2, "Lodge Correction Petition via MeeSeva or Spandana", "Submit formal grievance petition under AP Record of Rights Act at MeeSeva or Spandana citizen portal.", "1 Day", "Specify nature of error: Extent mismatch, wrong survey sub-division, or name spelling."),
                    new ServiceStepDto(3L, 3, "Mandal Surveyor Field Measurement & Mahazar", "Mandal surveyor conducts Total Station / DGPS field survey in presence of adjacent pattadars and draws mahazar.", "15–30 Days", "Verify boundary stones and ensure neighbouring landholders sign boundary consent."),
                    new ServiceStepDto(4L, 4, "Tahsildar / RDO Order & Webland Correction", "Revenue Divisional Officer (RDO) or Tahsildar passes formal correction order; computer operator rectifies Webland record.", "15 Days", "Verify corrected extent and survey number on Meebhoomi portal.")
            ));

            result.setDocuments(Arrays.asList(
                    new ServiceRequiredDocumentDto(1L, "Original Registered Title Deed & Link Documents", "Registered conveyance deeds establishing legal title and correct boundary extent.", true, "Original deed + self-attested copies", "Procured from owner's custody or SRO certified copy."),
                    new ServiceRequiredDocumentDto(2L, "Existing Erroneous RoR-1B / Adangal Printout", "Printout of the current online record displaying the clerical or extent error.", true, "Meebhoomi web extract", "Download from meebhoomi.ap.gov.in."),
                    new ServiceRequiredDocumentDto(3L, "Applicant Aadhaar & Mobile Number", "Identity proof of pattadar for digital KYC and SMS status updates.", true, "Aadhaar Card", "Download from uidai.gov.in.")
            ));

            result.setCriticalTips(Arrays.asList(
                    "Appellate Authority: If Tahsildar rejects correction without valid grounds, an appeal can be filed before the Revenue Divisional Officer (RDO) under the AP Rights in Land and Pattadar Passbooks Act.",
                    "YSR Jagananna Saswatha Bhoo Hakku (Resurvey): For villages undergoing drone/DGPS resurvey, register grievances directly at the village camp office before final RoR publication."
            ));

            return result;
        }

        // 6. Other Property Service (Gift / Lease / Mortgage)
        if ("OTHER_PROPERTY_SERVICE".equalsIgnoreCase(requirement)) {
            result.setPathwayCode("PROPERTY_OTHER_DEED_AP");
            result.setPathwayTitle("Gift Deed, Family Settlement, Lease & Mortgage Registration");
            result.setBadgeText("Special Instrument Registration");
            result.setMatchReason("Based on your answers: You are registering an instrument other than a standard sale deed, such as a Gift Deed to family members, Relinquishment/Release Deed, Long-Term Lease (>11 months), or Equitable Mortgage.");
            result.setEstimatedTimeline("Same-Day (1–2 Hours at SRO)");
            result.setCalculatedFee("Determined by Deed Category & Relationship Slabs");
            result.setFeeBreakdown("In Andhra Pradesh, Gift Deeds to specified family members (spouse, children, parents, siblings) enjoy concessional stamp duty (1%–2% vs standard 5%). Long-term leases and mortgages have slab rates based on annual rent or loan amount. Verify exact duty on: https://registration.ap.gov.in/");
            result.setPoliceVerificationMode("Sub-Registrar Document & Witness Verification");
            result.setValidity("As per instrument terms (Perpetual for Gift, Specific tenure for Lease)");

            result.setSteps(Arrays.asList(
                    new ServiceStepDto(1L, 1, "Determine Applicable Instrument & Duty Slab", "Identify whether deed is Gift (blood relative vs non-relative), Release Deed among co-owners, or Lease.", "1 Day", "Check concessional family duty slabs on AP IGRS."),
                    new ServiceStepDto(2L, 2, "Obtain Encumbrance Certificate (EC Form 15)", "Verify that donor has clear unencumbered title free from mortgage attachments.", "1 Day", "Search on AP IGRS portal."),
                    new ServiceStepDto(3L, 3, "Draft Deed with Specific Legal Clauses", "Draft deed including essential statutory clauses: voluntary gift without consideration, unconditional acceptance, and property schedule.", "1–2 Days", "Draft through licensed document writer or advocate."),
                    new ServiceStepDto(4L, 4, "Pay E-Stamp Duty & Book SRO Slot", "Purchase e-Stamp paper or generate CFMS e-Challan; book physical execution slot at SRO.", "1 Day", "Make online payment on AP IGRS / CFMS."),
                    new ServiceStepDto(5L, 5, "Execute Deed at SRO with 2 Adult Witnesses", "Donor, donee (or lessor/lessee) and two adult witnesses attend SRO with original documents for biometric capture.", "1–2 Hours", "Donee must physically accept the gift during donor's lifetime."),
                    new ServiceStepDto(6L, 6, "Collect Registered Document & Apply for Mutation", "Receive endorsed registered deed; proceed with revenue/municipal mutation to record donee's ownership.", "Same Day", "Endorsement recorded in SRO Index II.")
            ));

            result.setDocuments(Arrays.asList(
                    new ServiceRequiredDocumentDto(1L, "Donor / Executant Original Title Deeds", "Original registered deed through which donor/lessor acquired the property.", true, "Original deed", "Owner's custody."),
                    new ServiceRequiredDocumentDto(2L, "Proof of Family Relationship (for Concessional Gift)", "Family Member Certificate, Ration Card, or Birth Certificate establishing blood relation with donor.", true, "Government issued relationship certificate", "Obtain from MeeSeva / Tahsildar."),
                    new ServiceRequiredDocumentDto(3L, "Identity & Address Proofs of Parties & Witnesses", "Aadhaar Card and PAN Card of executant, claimant, and two independent witnesses.", true, "Originals + self-attested copies", "Carry to SRO."),
                    new ServiceRequiredDocumentDto(4L, "Encumbrance Certificate (EC)", "Lien-free certificate for minimum 30 years.", true, "EC Form 15", "Download from registration.ap.gov.in.")
            ));

            result.setCriticalTips(Arrays.asList(
                    "Irrevocability of Gift: Once a gift deed is registered and accepted by the donee, it cannot be unilaterally cancelled by the donor under Section 126 of the Transfer of Property Act.",
                    "Relationship Certificate Mandate: To avail concessional 1% stamp duty for gifts between family members, a valid government-issued Family Member Certificate is strictly inspected by the SRO."
            ));

            return result;
        }

        // 7. Standard Sale / Purchase Registration (Urban or Rural)
        result.setPathwayCode(isUrban ? "PROPERTY_SALE_REGISTRATION_URBAN_AP" : "PROPERTY_SALE_REGISTRATION_RURAL_AP");
        result.setPathwayTitle(isUrban
                ? "Urban Property Sale / Purchase Conveyancing & SRO Registration (AP IGRS)"
                : "Rural / Agricultural Land Sale Deed Registration & Auto-Mutation (AP IGRS)");
        result.setBadgeText(isUrban ? "Urban Conveyancing Standard" : "Agricultural Land Conveyancing");
        result.setMatchReason("Based on your answers: You are executing a " + (isUrban ? "urban residential/commercial property" : "rural/agricultural land")
                + " sale in Andhra Pradesh. Complete conveyancing requires 30-year title verification, Section 22A check, market value calculation, SRO deed execution, and subsequent Webland mutation.");
        result.setEstimatedTimeline("SRO Execution: Same-Day (1–3 Hours) | Revenue Mutation: 15–30 Days post-registration");
        result.setCalculatedFee("Market Value (Circle Rate) Based Duty");
        result.setFeeBreakdown("In Andhra Pradesh, Stamp Duty is typically ~5% of gross market value or consideration (whichever is higher), Registration Fee is ~1%, plus applicable local body transfer duty/cess. Transactions >= ₹50 Lakhs require mandatory 1% TDS deduction under Section 194-IA via Form 26QB. Calculate exact duty at: https://registration.ap.gov.in/");
        result.setPoliceVerificationMode("SRO Biometric & Document Verification");
        result.setValidity("Perpetual Title Transfer (Registered Conveyance Deed)");

        result.setSteps(Arrays.asList(
                new ServiceStepDto(1L, 1, "Identify Property & Conduct Preliminary Due Diligence", "Verify clear ownership, boundaries, physical possession, and survey sub-division of the parcel.", "1–3 Days", "Ensure seller is recorded as sole owner in 1B/revenue records."),
                new ServiceStepDto(2L, 2, "Check Section 22A Prohibited Property Register on AP IGRS", "Search AP IGRS portal (registration.ap.gov.in) to verify land is NOT prohibited (assigned land, ceiling surplus, or wakf/endowments).", "10–15 Minutes", "Transactions involving 22A lands are strictly barred by law."),
                new ServiceStepDto(3L, 3, "Verify 30-Year Encumbrance Certificate (EC Form 15)", "Obtain and inspect an unbroken 30-year EC to confirm zero existing bank mortgages, court attachments, or disputes.", "1 Day", "Search on AP IGRS or obtain signed copy via MeeSeva."),
                new ServiceStepDto(4L, 4, "Draft Sale Deed with Boundary Schedule & Survey Particulars", "Draft the sale deed with licensed conveyancer; include survey no., plot boundaries (N/S/E/W), and consideration schedule.", "1–2 Days", "Ensure seller's name exactly matches prior title deeds and Aadhaar."),
                new ServiceStepDto(5L, 5, "Compute Market Value, Stamp Duty & Registration Fees", "Use AP IGRS Market Value Calculator to determine circle rate. Compute stamp duty (~5%) and registration fee (~1%).", "15–30 Minutes", "Duty is payable on higher of actual sale price or guidance circle rate."),
                new ServiceStepDto(6L, 6, "Section 194-IA 1% TDS Payment via Form 26QB (if >= ₹50L)", "If sale consideration is ₹50 Lakhs or above, buyer must deduct 1% TDS and deposit via Form 26QB on Income Tax e-filing portal.", "1 Day", "SRO will demand Form 26QB challan acknowledgement before registration."),
                new ServiceStepDto(7L, 7, "Procure E-Stamps & Book SRO Appointment Slot", "Generate e-Stamp certificate via SHCIL or state CFMS e-Challan; book appointment slot at jurisdictional SRO.", "1 Day", "Select date and time slot convenient for buyer, seller, and witnesses."),
                new ServiceStepDto(8L, 8, "Physical Appearance, Biometrics & Execution at SRO", "Buyer, seller, and two adult witnesses attend SRO counter with originals. Complete thumbprint scanning and webcam photo capture.", "1–3 Hours", "Sub-Registrar verifies identification and formally endorses the deed numbers."),
                new ServiceStepDto(9L, 9, "Collect Registered Document & Verify Auto-Mutation in Webland", "Collect original registered sale deed with Book 1 Index II endorsement; confirm auto-mutation trigger to Tahsildar.", "Same Day", "Tahsildar will issue updated RoR-1B within 15–30 days.")
        ));

        List<ServiceRequiredDocumentDto> docs = new ArrayList<>();
        long docId = 1L;

        docs.add(new ServiceRequiredDocumentDto(
                docId++,
                "Identity Proofs (Aadhaar & PAN) of Buyer, Seller & 2 Witnesses",
                "Mandatory biometric and identity verification documents for all parties appearing before the Sub-Registrar.",
                true,
                "Original Aadhaar & PAN Card + 1 self-attested photocopy each",
                "Carry original cards physically to SRO counter."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                docId++,
                "Chain of Parent / Link Deeds (Minimum 30 Years)",
                "Original title deeds establishing unbroken chronological sequence of ownership leading to the seller.",
                true,
                "Original deeds + complete photocopy sets",
                "Procure from seller or financial institution release."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                docId++,
                "Drafted Sale Deed on Legal Stamp Paper / E-Stamp Certificate",
                "Conveyance deed incorporating property schedule, boundaries, consideration, and witness attestations.",
                true,
                "Physical deed with e-Stamp certificate / CFMS challan",
                "Drafted through licensed deed writer or legal counsel."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                docId++,
                "Encumbrance Certificate (EC Form 15 covering 30 Years)",
                "Official search certificate proving title is unencumbered by mortgages, liens, or court attachments.",
                true,
                "Digitally signed Form 15 extract",
                "Download from registration.ap.gov.in or MeeSeva."
        ));

        if (isUrban) {
            docs.add(new ServiceRequiredDocumentDto(
                    docId++,
                    "Approved Building Sanction Plan / DTCP Layout Approval",
                    "Competent authority layout or building construction approval (APCRDA / DTCP / Municipal Corporation).",
                    true,
                    "Approved blueprint / sanction order copy",
                    "Obtain from builder or municipal town planning wing."
            ));

            docs.add(new ServiceRequiredDocumentDto(
                    docId++,
                    "Latest Property Tax Receipts & Municipal Assessment Extract",
                    "Receipt confirming property tax paid up to date for the current financial year.",
                    true,
                    "Original / online tax payment receipt",
                    "Download from CDMA AP portal (cdma.ap.gov.in)."
            ));
        } else {
            docs.add(new ServiceRequiredDocumentDto(
                    docId++,
                    "Latest Pattadar Passbook / RoR-1B Extract & Adangal",
                    "Official revenue records proving seller's registered ownership and physical possession of agricultural parcel.",
                    true,
                    "Original e-Pattadar Passbook + Meebhoomi 1B extract",
                    "Download from meebhoomi.ap.gov.in or MeeSeva."
            ));
        }

        docs.add(new ServiceRequiredDocumentDto(
                docId++,
                "Section 194-IA 1% TDS Payment Challan (Form 26QB)",
                "Statutory income tax compliance certificate required for all property sales valued at ₹50 Lakhs or above.",
                false,
                "Form 26QB acknowledgement challan printout",
                "File online on Income Tax e-filing portal (eportal.incometax.gov.in)."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                docId++,
                "Registered Power of Attorney (PoA / GPA)",
                "Required only if the deed is being executed by an authorized attorney/agent on behalf of buyer or seller.",
                false,
                "Original registered PoA + Live Life Certificate of principal",
                "Must be registered at SRO under Section 32/33 of Registration Act."
        ));

        result.setDocuments(docs);

        result.setCriticalTips(Arrays.asList(
                "Statutory Legal Notice: This guidance is provided for general informational assistance across Andhra Pradesh registration workflows. It does not constitute legal counsel, title certification, or a guarantee of deed acceptance by the registering officer.",
                "Section 22A Prohibited Property Check: Before paying advances or signing agreements, verify on AP IGRS that the survey number is NOT listed under Section 22A prohibited categories (assigned lands, ceiling surplus, or wakf/temple lands).",
                "30-Year Encumbrance Search: Obtain EC Form 15 for a minimum unbroken period of 30 years to verify absence of subsisting bank mortgages, private hypothecations, or litigation attachments.",
                "Registration vs. Mutation: SRO registration only records the deed; ownership in government land revenue records requires subsequent mutation entry (RoR-1B/Pattadar Passbook) by the Tahsildar.",
                "Section 194-IA TDS Compliance: For property purchases valued at ₹50 Lakhs or more, buyer must deduct 1% TDS from consideration and remit via Form 26QB to avoid penal interest and SRO holds."
        ));

        return result;
    }
}
