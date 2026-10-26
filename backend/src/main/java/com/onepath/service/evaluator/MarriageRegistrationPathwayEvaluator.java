package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Master Pathway Evaluator for Marriage Registration in India,
 * specifically tailored for Andhra Pradesh Registration & Stamps Department (IGRS AP),
 * Hindu Marriage Act 1955, Special Marriage Act 1954, and Anand Marriage Act.
 */
@Component
public class MarriageRegistrationPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String MARRIAGE_SLUG = "marriage-registration";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return MARRIAGE_SLUG.equalsIgnoreCase(s)
                || "marriage".equalsIgnoreCase(s)
                || "marriage-certificate".equalsIgnoreCase(s)
                || "marriage-certificate-registration".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(MARRIAGE_SLUG);
        dto.setServiceName(service != null ? service.getName() : "Marriage Registration");
        dto.setTitle("Marriage Registration Pathway & Eligibility Checker");
        dto.setDescription("Answer a few quick questions regarding your marriage ceremony status, religious or civil framework, statutory age, residence, and prior marital history to discover your exact registration procedure, competent Sub-Registrar Office, witness requirements, and document checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Has the marriage ceremony already taken place?
        questions.add(new QuestionnaireQuestionDto(
                "ceremonyStatus",
                "Has the marriage ceremony already taken place?",
                "Distinguishes registration of an existing solemnized marriage from direct civil solemnization before the Marriage Officer.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("ALREADY_SOLEMNIZED", "Yes, ceremony has already taken place", "Marriage was solemnized according to religious rites (Hindu, Sikh, Christian, etc.) or customary rituals.", "Registration of Existing Marriage"),
                        new QuestionOptionDto("INTENDED_FUTURE", "No, planning to marry / Civil Court Marriage", "Couple intends to solemnize and register their marriage directly before the Marriage Officer (requires 30-day notice).", "Solemnization & Registration (SMA)")
                )
        ));

        // Q2: Applicable Act / Legal Framework
        questions.add(new QuestionnaireQuestionDto(
                "applicableAct",
                "Which registration pathway applies to your marriage?",
                "Marriage laws in India depend on religion, personal laws, or civil choice.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("HMA_1955", "Hindu Marriage Act, 1955 (Hindus, Buddhists, Jains, Sikhs)", "Applicable when both parties are Hindu, Buddhist, Jain, or Sikh and ceremony is already solemnized. Requires 2-3 witnesses. No public notice period.", "HMA Track (2 Witnesses)"),
                        new QuestionOptionDto("SMA_1954", "Special Marriage Act, 1954 (Civil / Inter-Faith / Any Citizens)", "Applicable to any two Indian citizens regardless of religion. Requires mandatory 30-day public notice on SRO board and 3 witnesses.", "SMA Civil Track (3 Witnesses)"),
                        new QuestionOptionDto("ANAND_2012", "Anand Marriage Act (Sikh Anand Karaj Ceremony)", "Specific registration for Sikh marriages solemnized via Anand Karaj rites. Requires 2 witnesses and Granthi / Gurudwara certificate.", "Anand Marriage Track"),
                        new QuestionOptionDto("CHRISTIAN_PARSI", "Christian or Parsi Marriage Acts", "Solemnized in a registered Church or Fire Temple. Requires church/priest certificate and 2 witnesses.", "Personal Law Track"),
                        new QuestionOptionDto("UNSURE", "Unsure / Need Guidance on Legal Framework", "We will guide you based on your religious background, ceremony status, and documentation.", "Guided Advisory")
                )
        ));

        // Q3: Legal Age Eligibility
        questions.add(new QuestionnaireQuestionDto(
                "legalAge",
                "Are both parties at or above the statutory legal minimum age?",
                "Prevailing statutory rule: Groom must be minimum 21 years old and Bride minimum 18 years old on the marriage/application date.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("LEGAL_AGE_MET", "Yes, Groom is ≥ 21 and Bride is ≥ 18", "Both parties fully meet the statutory age requirements under Indian law.", "Fully Age Eligible"),
                        new QuestionOptionDto("UNDERAGE_ALERT", "No, one or both parties are below minimum statutory age", "Under Indian law, marriage cannot be registered if either party is below legal age (Groom < 21 or Bride < 18).", "Statutory Age Warning")
                )
        ));

        // Q4: State and District of Residence / Registration Jurisdiction
        questions.add(new QuestionnaireQuestionDto(
                "residenceState",
                "What is your State and intended registration jurisdiction?",
                "Under Indian law, at least one party must reside in the Sub-Registrar jurisdiction for over 30 days, or the wedding must have occurred there.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("ANDHRA_PRADESH", "Andhra Pradesh (Resident or Marriage Solemnized in AP)", "Register through AP Registration & Stamps Department (IGRS AP) / MeeSeva and local SRO.", "AP IGRS Jurisdiction"),
                        new QuestionOptionDto("OTHER_INDIAN_STATE", "Other Indian State / Union Territory", "Register through the respective state's Inspector General of Registration (IGR) / e-District portal.", "State Specific SRO")
                )
        ));

        // Q5: Marital History & Prior Marriages
        questions.add(new QuestionnaireQuestionDto(
                "maritalStatus",
                "What is the prior marital status of both parties?",
                "Neither party must have a living spouse at the time of marriage. Prior marriages must be lawfully dissolved.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("BOTH_UNMARRIED", "Both parties are unmarried (Bachelor / Spinster)", "Standard first-time marriage registration. Self-affidavits required.", "Standard Registration"),
                        new QuestionOptionDto("DIVORCEE", "Either party is legally divorced", "Requires certified copy of Divorce Decree Absolute issued by a competent Family Court.", "Divorce Decree Required"),
                        new QuestionOptionDto("WIDOWED", "Either party is widowed (Spouse deceased)", "Requires certified copy of official Death Certificate of the deceased spouse.", "Death Certificate Required"),
                        new QuestionOptionDto("SEPARATED_WITHOUT_DECREE", "Separated without formal legal divorce decree", "A second marriage without an official decree absolute is void under law and cannot be registered.", "Legal Impediment Alert")
                )
        ));

        // Q6: Special Circumstances
        questions.add(new QuestionnaireQuestionDto(
                "specialCircumstances",
                "Are there any special circumstances involved?",
                "Certain circumstances require specialized documentation such as Embassy NOCs or customary affidavits.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("NONE", "None (Both are Indian citizens residing locally)", "Standard statutory documentation applies.", "Standard Processing"),
                        new QuestionOptionDto("FOREIGN_NATIONAL_NRI", "One party is a Foreign Citizen / NRI / OCI", "Requires Embassy No-Objection Certificate (NOC) / Single Status Certificate and valid Passport/Visa.", "Embassy NOC Required"),
                        new QuestionOptionDto("INTER_FAITH", "Inter-Faith / Inter-Religious Marriage", "Special Marriage Act, 1954 is the designated legal civil route allowing marriage without religious conversion.", "Special Marriage Act Recommended"),
                        new QuestionOptionDto("PROHIBITED_DEGREE_CUSTOM", "Parties are related within prohibited degrees by custom", "Sworn joint affidavit required demonstrating valid established customary exception in community.", "Customary Affidavit Required")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();

        String ceremonyStatus = answers.getOrDefault("ceremonyStatus", "ALREADY_SOLEMNIZED");
        String applicableAct = answers.getOrDefault("applicableAct", "HMA_1955");
        String legalAge = answers.getOrDefault("legalAge", "LEGAL_AGE_MET");
        String residenceState = answers.getOrDefault("residenceState", "ANDHRA_PRADESH");
        String maritalStatus = answers.getOrDefault("maritalStatus", "BOTH_UNMARRIED");
        String specialCircumstances = answers.getOrDefault("specialCircumstances", "NONE");

        // Step list
        List<ServiceStepDto> steps = new ArrayList<>();
        List<ServiceRequiredDocumentDto> documents = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        // Age violation check
        if ("UNDERAGE_ALERT".equalsIgnoreCase(legalAge)) {
            result.setPathwayCode("UNDERAGE_STATUTORY_BAR");
            result.setPathwayTitle("Statutory Age Restriction - Registration Barred");
            result.setBadgeText("Statutory Age Ineligible");
            result.setEstimatedTimeline("Not Applicable until legal age is achieved");
            result.setCalculatedFee("₹0.00");
            result.setFeeBreakdown("No fee can be accepted for an invalid statutory registration");
            result.setMatchReason("Under Section 5(iii) of the Hindu Marriage Act and Section 4(c) of the Special Marriage Act, the groom must have completed 21 years of age and the bride must have completed 18 years. An underage marriage cannot be registered by any Sub-Registrar and is legally invalid.");
            result.setOfficialPortalUrl("https://registration.ap.gov.in");

            steps.add(new ServiceStepDto(1, "Statutory Legal Requirement", "Wait until both parties attain statutory legal age (Groom: 21, Bride: 18) before solemnizing or registering.", "Immediate", "No registration authority has the discretion to waive minimum age."));
            steps.add(new ServiceStepDto(2, "Consult Legal Counsel", "If you require legal advice on personal rights or civil standing, consult a registered Family Law Advocate.", "Advisory", "Beware of unauthorized agents offering fake registration."));

            documents.add(new ServiceRequiredDocumentDto(null, "Birth Certificate / Matriculation Certificate", "Proof that both parties meet statutory age on the date of marriage.", true, "Original Certificate", "Obtain from Municipal Corporation or Education Board."));
            
            tips.add("Legal age criteria in India is strict: Groom ≥ 21, Bride ≥ 18.");
            tips.add("Underage marriage registration cannot be processed by any Sub-Registrar Office.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // Separated without decree check
        if ("SEPARATED_WITHOUT_DECREE".equalsIgnoreCase(maritalStatus)) {
            result.setPathwayCode("LEGAL_IMPEDIMENT_BIGAMY");
            result.setPathwayTitle("Pending Legal Dissolution - Prior Marriage Active");
            result.setBadgeText("Legal Impediment: Prior Marriage Active");
            result.setEstimatedTimeline("Dependent on Family Court proceedings");
            result.setCalculatedFee("₹0.00");
            result.setFeeBreakdown("Cannot proceed to registration before decree absolute");
            result.setMatchReason("Under Indian law (Section 5(i) HMA and Section 4(a) SMA), neither party must have a spouse living at the time of marriage. Living separately does not dissolve a marriage. Registering a second marriage without a certified Divorce Decree Absolute is bigamous and void.");
            result.setOfficialPortalUrl("https://registration.ap.gov.in");

            steps.add(new ServiceStepDto(1, "Obtain Certified Divorce Decree", "Complete formal divorce proceedings in the competent Family Court and obtain the certified final decree.", "Court Process", "A pending divorce petition is not sufficient."));
            steps.add(new ServiceStepDto(2, "Post-Decree Appeal Window", "Ensure the statutory appeal period (normally 90 days) has elapsed without an appeal being filed before entering a new marriage.", "90 Days Window", "Sub-Registrars verify decree finality."));

            documents.add(new ServiceRequiredDocumentDto(null, "Certified Copy of Divorce Decree Absolute", "Final judgment and decree order with court seal from competent Family Court.", true, "Certified Court Copy", "Obtain from Family Court copying department."));
            
            tips.add("Bigamy is a criminal offense under Section 494 of the Indian Penal Code / BNS.");
            tips.add("Always secure a certified true copy of the final divorce decree absolute from the Family Court.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // Case 1: Special Marriage Act 1954 (Civil / Inter-Faith / Intended Marriage)
        if ("SMA_1954".equalsIgnoreCase(applicableAct)
                || "INTENDED_FUTURE".equalsIgnoreCase(ceremonyStatus)
                || "INTER_FAITH".equalsIgnoreCase(specialCircumstances)) {

            result.setPathwayCode("SPECIAL_MARRIAGE_ACT_1954");
            result.setPathwayTitle("Special Marriage Act, 1954 (Civil Court Marriage & 30-Day Notice)");
            result.setBadgeText("Special Marriage Act (3 Witnesses + 30-Day Notice)");
            result.setEstimatedTimeline("30 to 45 Days (Mandatory 30-Day Public Notice Period)");
            result.setCalculatedFee("₹150 to ₹500");
            result.setFeeBreakdown("Notice Fee: ₹150 | Solemnization & Certificate: ₹150 - ₹350 | SRO User Charges: ₹50 - ₹100");
            result.setMatchReason("The Special Marriage Act, 1954 provides a civil registration framework for any two Indian citizens regardless of religion. It requires filing a Notice of Intended Marriage with the Sub-Registrar in whose jurisdiction at least one party has resided for > 30 days. After a mandatory 30-day public notice on the office notice board without valid objections, the marriage is solemnized and registered in the presence of 3 mandatory witnesses.");
            result.setOfficialPortalUrl("https://registration.ap.gov.in");

            steps.add(new ServiceStepDto(1, "Verify 30-Day Jurisdictional Residence", "Confirm that at least one party has continuously resided in the Sub-Registrar's jurisdiction for minimum 30 days immediately prior to giving notice.", "Prerequisite", "Provide residence proof like Aadhaar, electricity bill, or registered rent agreement."));
            steps.add(new ServiceStepDto(2, "Submit Notice of Intended Marriage (Form-II)", "Fill Notice of Intended Marriage online via AP IGRS (registration.ap.gov.in) or submit physically at the SRO with passport photos and age/address proofs.", "Day 1", "Pay statutory notice fee (~₹150). Prescribe intended solemnization date."));
            steps.add(new ServiceStepDto(3, "Mandatory 30-Day Public Notice Display", "The Marriage Officer publishes the notice on the official notice board of the SRO for 30 days to invite legal objections (regarding age or existing marriage).", "30 Days Window", "If no valid legal objection is received within 30 days, the marriage may be solemnized."));
            steps.add(new ServiceStepDto(4, "Appearance for Solemnization with 3 Witnesses", "Both parties and 3 credible witnesses appear before the Marriage Officer at the SRO. Bring original identity and address proofs for all 5 persons.", "Scheduled Day", "All 3 witnesses must be adults (18+) with valid Aadhaar/PAN."));
            steps.add(new ServiceStepDto(5, "Declarations & Solemnization before Marriage Officer", "Bride, Groom, and 3 witnesses sign the statutory declarations in Form-III before the Marriage Officer. Biometric fingerprints and photos are captured.", "1 Hour", "The ceremony involves a simple statutory declaration of consent in any language understood by the parties."));
            steps.add(new ServiceStepDto(6, "Marriage Certificate Issuance & DigiLocker Delivery", "Marriage Officer enters the record into the Marriage Certificate Book (Form-IV). Certificate with digital signature and QR code is issued immediately.", "Same Day", "Certificate can be downloaded from the state portal and stored in DigiLocker."));

            populateSmaDocuments(documents, maritalStatus, specialCircumstances);

            tips.add("SMA requires 3 mandatory witnesses who must be physically present with original ID proofs.");
            tips.add("Continuous residence in the SRO jurisdiction for at least 30 days prior to filing notice is strictly audited.");
            tips.add("No religious ceremony or conversion is required under the Special Marriage Act.");

        } else if ("ANAND_2012".equalsIgnoreCase(applicableAct)) {
            // Case 2: Anand Marriage Act (Sikh)
            result.setPathwayCode("ANAND_MARRIAGE_ACT");
            result.setPathwayTitle("Anand Marriage Act (Sikh Anand Karaj Solemnization)");
            result.setBadgeText("Anand Marriage Act (Sikh Rites)");
            result.setEstimatedTimeline("7 to 15 Working Days (Direct SRO Appointment)");
            result.setCalculatedFee("₹100 to ₹250");
            result.setFeeBreakdown("Statutory Registration: ₹100 | SRO User Charges: ₹100 - ₹150");
            result.setMatchReason("Statutory framework specifically recognizing Sikh marriage ceremonies performed through Anand Karaj rites. The marriage must have already been solemnized. No public notice period is required. Registration requires 2 credible witnesses who attended the ceremony and Gurudwara/Granthi certification.");
            result.setOfficialPortalUrl("https://registration.ap.gov.in");

            populateHmaSteps(steps, "Anand Marriage Act", 2);
            populateHmaDocuments(documents, maritalStatus, specialCircumstances, true);

            tips.add("Anand Marriage Act requires Gurudwara/Granthi Anand Karaj certificate.");
            tips.add("2 credible adult witnesses who attended the ceremony must accompany the couple.");

        } else {
            // Case 3: Hindu Marriage Act, 1955 (Standard Already Solemnized)
            result.setPathwayCode("HINDU_MARRIAGE_ACT_1955");
            result.setPathwayTitle("Hindu Marriage Act, 1955 (Registration of Solemnized Marriage)");
            result.setBadgeText("Hindu Marriage Act (2 Witnesses - Direct SRO)");
            result.setEstimatedTimeline("7 to 15 Working Days (Often Same-Day Post SRO Appointment)");
            result.setCalculatedFee("₹100 to ₹200");
            result.setFeeBreakdown("Statutory Registration Fee: ₹100 | MeeSeva / SRO Biometric Charges: ₹50 - ₹100");
            result.setMatchReason("Applicable when both husband and wife are Hindus, Buddhists, Jains, or Sikhs and the marriage has already been solemnized according to religious rites (Saptapadi, Kanyadanam, etc.). There is NO 30-day notice period. The couple books an appointment online via AP IGRS / MeeSeva and appears before the Sub-Registrar with 2 credible witnesses, wedding photographs, and marriage invitation.");
            result.setOfficialPortalUrl("https://registration.ap.gov.in");

            populateHmaSteps(steps, "Hindu Marriage Act, 1955", 2);
            populateHmaDocuments(documents, maritalStatus, specialCircumstances, false);

            tips.add("No 30-day notice period is required under the Hindu Marriage Act.");
            tips.add("2 witnesses who attended the religious ceremony must be present with original Aadhaar/PAN.");
            tips.add("Wedding card, ceremony photos showing rituals, and joint passport photos are mandatory.");
        }

        result.setSteps(steps);
        result.setDocuments(documents);
        result.setCriticalTips(tips);
        return result;
    }

    private void populateHmaSteps(List<ServiceStepDto> steps, String actName, int witnessCount) {
        steps.add(new ServiceStepDto(1, "Identify Jurisdictional Sub-Registrar Office (SRO)", "Registration can be completed at the SRO in whose jurisdiction: (a) Marriage was solemnized, (b) Husband resides, or (c) Wife resides (> 30 days). In Andhra Pradesh, find your SRO via registration.ap.gov.in.", "Step 1", "Jurisdictional mismatch is the #1 reason for application rejection."));
        steps.add(new ServiceStepDto(2, "Online Form Entry & Appointment Slot Booking", "Register on the AP Registration & Stamps portal (IGRS AP) or visit your local MeeSeva centre. Complete Memorandum of Marriage with couple and witness details, and book a physical appointment slot.", "30 Minutes", "Ensure names match your Aadhaar, 10th marksheet, and wedding card."));
        steps.add(new ServiceStepDto(3, "Document Scans Upload & Fee Payment", "Upload scanned copies of age proofs, address proofs, wedding invitation/temple receipt, joint passport photos, and wedding ritual photos. Pay statutory fee online (~₹100-₹200).", "15 Minutes", "Preserve the payment receipt and appointment acknowledgement slip."));
        steps.add(new ServiceStepDto(4, "Prepare " + witnessCount + " Credible Witnesses", "Arrange " + witnessCount + " adult witnesses who personally attended the wedding ceremony. Witnesses must possess original Aadhaar Card / PAN Card and passport photos.", "Before Visit", "Parents, relatives, or close friends who witnessed the rites can serve as witnesses."));
        steps.add(new ServiceStepDto(5, "Physical Appearance at Sub-Registrar Office (SRO)", "Husband, Wife, and both witnesses must appear in person before the Sub-Registrar on the appointed date with original documents and photocopies.", "1-2 Hours", "Biometrics (fingerprints & webcam photographs) of the couple and witnesses are recorded."));
        steps.add(new ServiceStepDto(6, "Verification, Register Entry & Certificate Issuance", "The Sub-Registrar verifies original records, enters the marriage into the state Marriage Register, and issues the official Marriage Certificate with QR verification.", "Same Day / 7 Days", "The certificate is digitally signed and automatically pushed to citizen DigiLocker accounts."));
    }

    private void populateHmaDocuments(List<ServiceRequiredDocumentDto> docs, String maritalStatus, String specialCircumstances, boolean isSikh) {
        docs.add(new ServiceRequiredDocumentDto(null, "Application Form / Memorandum of Marriage", "Statutory application duly signed by both husband and wife with passport photos affixed.", true, "Duly signed printed form", "Generated from AP IGRS / MeeSeva portal after online entry."));
        docs.add(new ServiceRequiredDocumentDto(null, "Age Proof of Groom & Bride", "Statutory proof confirming Groom ≥ 21 years and Bride ≥ 18 years on the date of marriage.", true, "Birth Certificate / SSC Marks Sheet / Passport", "Issued by Municipal Corporation, Secondary Education Board, or MEA."));
        docs.add(new ServiceRequiredDocumentDto(null, "Residence Proof of Husband & Wife", "Proof establishing residence within the Sub-Registrar's jurisdiction for more than 30 days.", true, "Aadhaar Card / Voter ID / Gas or Electricity Bill / Registered Lease", "UIDAI or state government utility bill."));
        docs.add(new ServiceRequiredDocumentDto(null, isSikh ? "Gurudwara Anand Karaj Certificate / Wedding Card" : "Wedding Card / Temple Marriage Certificate", "Evidence of solemnized religious ceremony (Original wedding invitation card, temple receipt, or priest certificate).", true, "Original Wedding Card or Temple/Gurudwara Certificate", "Preserve original physical wedding invitation."));
        docs.add(new ServiceRequiredDocumentDto(null, "Passport-Size Joint Photos & Wedding Ritual Photos", "4 to 6 joint passport-size photos + 2 clear 4x6 ceremony photos showing wedding rituals (garland exchange, mangalsutra / saptapadi).", true, "Original color photographic prints", "Studio prints clearly showing faces of both bride and groom during rites."));
        docs.add(new ServiceRequiredDocumentDto(null, "Sworn Notarized Affidavits of Marital Status", "Separate affidavits affirmed before a Notary Public declaring date & place of marriage, DOB, marital status at wedding, and non-prohibited relationship.", true, "Notarized Affidavit on Non-Judicial Stamp Paper", "Prepared on ₹20/₹50 stamp paper and notarized."));
        docs.add(new ServiceRequiredDocumentDto(null, "Witness Identity & Residence Proofs (2 Witnesses)", "Valid Aadhaar Card and PAN Card of two adult witnesses who attended the marriage ceremony. Witnesses must appear in person.", true, "Original ID + Self-attested copies", "Aadhaar Card / Passport / Voter ID."));

        if ("DIVORCEE".equalsIgnoreCase(maritalStatus)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Certified Copy of Divorce Decree Absolute", "Certified final judgment and decree from Family Court proving legal dissolution of prior marriage.", true, "Certified Court Copy with seal", "Obtained from the registry of the Family Court that granted divorce."));
        }
        if ("WIDOWED".equalsIgnoreCase(maritalStatus)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Certified Death Certificate of Deceased Spouse", "Official death certificate issued by Municipal Corporation or Registrar of Births & Deaths.", true, "Original / Certified Municipal Copy", "Issued by local municipal corporation or panchayat."));
        }
        if ("FOREIGN_NATIONAL_NRI".equalsIgnoreCase(specialCircumstances)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Embassy NOC / Single Status Certificate & Passport", "Certificate of No Impediment / Single Status Certificate issued by Embassy of foreign country + valid Passport & Indian Visa.", true, "Official Embassy Document & Passport Copy", "Obtain from foreign embassy / high commission in India."));
        }
    }

    private void populateSmaDocuments(List<ServiceRequiredDocumentDto> docs, String maritalStatus, String specialCircumstances) {
        docs.add(new ServiceRequiredDocumentDto(null, "Notice of Intended Marriage (Form-II)", "Statutory notice of intended civil solemnization signed by both parties.", true, "Prescribed Form-II signed by both parties", "Submitted online via AP IGRS or at SRO notice counter."));
        docs.add(new ServiceRequiredDocumentDto(null, "Age Proof of Groom & Bride", "Proof verifying Groom ≥ 21 years and Bride ≥ 18 years on the date of notice.", true, "Birth Certificate / Matriculation Certificate / Passport", "Official municipal or board certificate."));
        docs.add(new ServiceRequiredDocumentDto(null, "Jurisdictional Residence Proof (> 30 Days)", "Proof that at least one party has resided in the SRO area for minimum 30 days immediately preceding notice.", true, "Aadhaar Card / Registered Rent Agreement / Utility Bill", "Must clearly demonstrate local residential address."));
        docs.add(new ServiceRequiredDocumentDto(null, "Individual Passport Photographs (4 Each)", "Recent passport-size color photographs of bride and groom with white background.", true, "4 Passport Photos per applicant", "Recent passport studio prints."));
        docs.add(new ServiceRequiredDocumentDto(null, "Witness Identity & Address Proofs (3 Witnesses Mandatory)", "Aadhaar Card and PAN Card for three adult witnesses. All 3 witnesses must be physically present at solemnization.", true, "Original ID proofs + Photocopies", "Three adult citizens with valid government ID."));
        docs.add(new ServiceRequiredDocumentDto(null, "Statutory Declaration (Form-III)", "Sworn declaration before the Marriage Officer affirming mental capacity, consent, and absence of legal impediments.", true, "Form-III signed in presence of Marriage Officer", "Administered by the Marriage Officer on wedding day."));

        if ("DIVORCEE".equalsIgnoreCase(maritalStatus)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Certified Copy of Divorce Decree Absolute", "Certified final decree from Family Court proving legal dissolution of prior marriage.", true, "Certified Court Copy", "Obtained from Family Court."));
        }
        if ("WIDOWED".equalsIgnoreCase(maritalStatus)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Death Certificate of Late Spouse", "Statutory death certificate of former husband/wife.", true, "Certified Municipal Certificate", "Issued by Registrar of Births and Deaths."));
        }
        if ("FOREIGN_NATIONAL_NRI".equalsIgnoreCase(specialCircumstances)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Embassy NOC / Certificate of No Impediment", "Official certificate from Embassy/Consulate confirming no existing marriage or impediment under foreign law + valid Passport & Visa.", true, "Apostilled / Embassy Attested Certificate", "Issued by foreign embassy in New Delhi or consulate."));
        }
    }
}
