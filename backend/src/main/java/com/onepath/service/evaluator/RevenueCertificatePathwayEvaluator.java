package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Intelligent Pathway Evaluator for EWS, Family Income, and Caste / Community Certificates
 * based on Ministry of Personnel, Public Grievances and Pensions (DoPT) Memorandum No. 36039/1/2019-Estt (Res),
 * Central & State Right to Services (RTS / Sakala / Lok Seva) Acts, and State Revenue Department standards.
 */
@Component
public class RevenueCertificatePathwayEvaluator implements ServicePathwayEvaluator {

    public static final String PRIMARY_SLUG = "ews-income-caste-certificate";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return PRIMARY_SLUG.equalsIgnoreCase(s)
                || "ews-certificate".equalsIgnoreCase(s)
                || "income-certificate".equalsIgnoreCase(s)
                || "caste-certificate".equalsIgnoreCase(s)
                || "revenue-certificates".equalsIgnoreCase(s)
                || "ews-income-and-caste-certificate-issuance".equalsIgnoreCase(s)
                || "ews-income-caste-certificate-issuance".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(PRIMARY_SLUG);
        dto.setServiceName(service != null ? service.getName() : "EWS / Income & Caste Certificate Issuance");
        dto.setTitle("Revenue Certificate Navigator (EWS, Income & Caste)");
        dto.setDescription("Answer a few guided questions to identify which certificate you need, verify statutory eligibility (including EWS asset and income rules), determine your state portal and fees, and generate your tailored document checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Certificate Required
        questions.add(new QuestionnaireQuestionDto(
                "certificateType",
                "Which certificate do you require?",
                "Select whether you need an Economically Weaker Section (EWS) reservation certificate, an annual Family Income certificate, or a Caste/Community category certificate.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("EWS_CERTIFICATE", "Economically Weaker Section (EWS) Certificate", "10% reservation in central/state educational admissions (UPSC, NEET, JEE, CUET) & government recruitment for unreserved citizens. Valid for 1 Financial Year.", "10% General Quota"),
                        new QuestionOptionDto("INCOME_CERTIFICATE", "Family Income Certificate", "Official revenue assessment of annual family income for fee reimbursement, pre/post-matric scholarships, welfare schemes, and subsidies. Valid 1–3 FYs.", "Fee Concession & Scholarship"),
                        new QuestionOptionDto("CASTE_CERTIFICATE", "Caste / Community Certificate (SC, ST, OBC, SEBC)", "Constitutional community certification for SC, ST, and OBC reservation quotas, age relaxation, and fee concessions in exams and public appointments.", "Constitutional Quota")
                )
        ));

        // Q2: State / Union Territory
        questions.add(new QuestionnaireQuestionDto(
                "stateJurisdiction",
                "Which State or Union Territory are you applying in?",
                "Revenue certificates are issued under respective State Right to Services (RTS) / Lok Seva Guarantee Acts through designated state e-District portals.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("MAHARASHTRA", "Maharashtra (Aaple Sarkar Portal)", "Official Govt Fee: ₹20.00 | CSC: ₹33.60 | SLA: EWS 15 Days / Caste 45 Days", "Aaple Sarkar"),
                        new QuestionOptionDto("DELHI", "Delhi NCR (e-District Delhi)", "Official Govt Fee: NIL (Free) | SLA: OBC 14 Days / SC-ST 60 Days / Income 14 Days", "e-District Delhi"),
                        new QuestionOptionDto("KARNATAKA", "Karnataka (Nadakacheri / Seva Sindhu)", "Official Govt Fee: ₹25.00 | CSC: ₹15.00 | SLA: 21 Days under Sakala Act", "Nadakacheri"),
                        new QuestionOptionDto("UTTAR_PRADESH", "Uttar Pradesh (eDistrict UP / Jan Seva)", "Official Govt Fee: ₹15.00 (Self) / ₹30.00 (Jan Seva) | SLA: 15–20 Working Days", "eDistrict UP"),
                        new QuestionOptionDto("TAMIL_NADU", "Tamil Nadu (TN e-Sevai Portal)", "Official Govt Fee: ₹60.00 | SLA: 15 to 30 Days", "TN e-Sevai"),
                        new QuestionOptionDto("BIHAR", "Bihar (RTPS Bihar Portal)", "Official Govt Fee: NIL (Free) | CSC: ₹10–₹15 | SLA: 10 to 15 Working Days", "RTPS Bihar"),
                        new QuestionOptionDto("TELANGANA_AP", "Telangana / Andhra Pradesh (MeeSeva Portal)", "Official Govt Fee: ₹45.00 | CSC: ₹15.00 | SLA: 30 Days", "MeeSeva"),
                        new QuestionOptionDto("OTHER_STATE", "Other State / UT (State e-District / National ServicePlus)", "Governed by state portal (typically ₹15–₹50; 15–30 working days)", "National ServicePlus")
                )
        ));

        // Q3: First time vs Renewal / Update
        questions.add(new QuestionnaireQuestionDto(
                "applicationNature",
                "What is the nature of your application?",
                "Choose whether you are obtaining a fresh certificate for the first time or renewing/updating an expired certificate for the current financial year.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("FIRST_TIME", "First-Time Applicant (New / Fresh Certificate)", "Never obtained this certificate before or family has never been certified by the revenue authority.", "Fresh Issuance"),
                        new QuestionOptionDto("RENEWAL_UPDATE", "Renewal / Annual Re-issue for Current Financial Year", "Hold a previous certificate from a past financial year and need an updated valid certificate for ongoing admissions or jobs.", "Annual Renewal")
                )
        ));

        // Q4: Purpose of Certificate
        questions.add(new QuestionnaireQuestionDto(
                "applicationPurpose",
                "What is the primary purpose for obtaining this certificate?",
                "Identifies whether you require Central Government format (DoPT standard with QR verification) or State-specific quota format.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("CENTRAL_ADMISSIONS_JOBS", "Central Government Admissions & Recruitment", "UPSC, NEET, JEE, CUET, IITs, NITs, Central Universities, SSC, IBPS, Railways. Requires strict DoPT 2019 Central format.", "Central DoPT Format"),
                        new QuestionOptionDto("STATE_ADMISSIONS_JOBS", "State Government College Admissions & State PSC Recruitment", "State university counselling, CET, NEET state quota, State Public Service Commission exams.", "State Format"),
                        new QuestionOptionDto("SCHOLARSHIPS_FEE_WAIVER", "Scholarships, Fee Concessions & Education Welfare", "National Scholarship Portal (NSP), State Post-Matric Scholarships (PMS), tuition fee reimbursement.", "Scholarship Welfare"),
                        new QuestionOptionDto("GOVT_SUBSIDIES_HOUSING", "Government Subsidies, Housing & Ration Entitlements", "PMAY housing, food subsidies, or state welfare schemes.", "General Welfare")
                )
        ));

        // Q5: EWS Community / Caste Category Check (Conditional on EWS)
        questions.add(new QuestionnaireQuestionDto(
                "ewsCasteCategory",
                "What is your community / caste category?",
                "Under the 103rd Constitutional Amendment Act, Central EWS is reserved EXCLUSIVELY for General / Unreserved candidates not covered by SC, ST, or OBC reservation.",
                "certificateType=EWS_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("GENERAL_UNRESERVED", "General / Unreserved (Open Category)", "Not covered under any constitutional reservation for SC, ST, or OBC.", "EWS Eligible Category"),
                        new QuestionOptionDto("OBC_NON_CREAMY", "Other Backward Class (OBC) - Non-Creamy Layer", "Covered under 27% OBC quota; statutorily excluded from EWS reservation.", "Statutorily Excluded from EWS"),
                        new QuestionOptionDto("OBC_CREAMY", "Other Backward Class (OBC) - Creamy Layer", "Exceeds OBC-NCL income threshold; excluded from central EWS under statutory rules.", "Statutorily Excluded from EWS"),
                        new QuestionOptionDto("SCHEDULED_CASTE", "Scheduled Caste (SC)", "Covered under 15% SC constitutional quota; statutorily excluded from EWS reservation.", "Statutorily Excluded from EWS"),
                        new QuestionOptionDto("SCHEDULED_TRIBE", "Scheduled Tribe (ST)", "Covered under 7.5% ST constitutional quota; statutorily excluded from EWS reservation.", "Statutorily Excluded from EWS")
                )
        ));

        // Q6: EWS Gross Family Income (Conditional on EWS)
        questions.add(new QuestionnaireQuestionDto(
                "ewsGrossFamilyIncome",
                "What is your family's gross annual income for the preceding Financial Year?",
                "Gross family income includes salary, agriculture, business, profession, and other sources of Applicant, Parents, Spouse, Minor Children (<18), and Minor Siblings (<18).",
                "certificateType=EWS_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("INCOME_BELOW_8_LAKH", "Gross Family Income is strictly below ₹8,00,000 (< ₹8 LPA)", "Meets the statutory income ceiling under DoPT memorandum No. 36039/1/2019-Estt (Res).", "Meets Income Threshold"),
                        new QuestionOptionDto("INCOME_ABOVE_8_LAKH", "Gross Family Income is ₹8,00,000 or more (≥ ₹8 LPA)", "Exceeds Central EWS statutory income limit; disqualifies applicant from EWS quota.", "Exceeds Income Ceiling")
                )
        ));

        // Q7: EWS Asset Exclusion Thresholds (Conditional on EWS)
        questions.add(new QuestionnaireQuestionDto(
                "ewsAssetExclusion",
                "Does your family own ANY of the statutorily excluded assets listed below?",
                "Excluded assets: (1) Agricultural land ≥ 5 Acres, (2) Residential flat ≥ 1,000 sq ft, (3) Residential plot ≥ 100 sq yds in notified municipality, (4) Residential plot ≥ 200 sq yds in non-notified areas. (All family properties across locations are clubbed).",
                "certificateType=EWS_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("NO_EXCLUDED_ASSETS", "No — Family owns NONE of these excluded properties", "Family land and residential properties are strictly within the permitted statutory limits.", "Meets Asset Threshold"),
                        new QuestionOptionDto("HAS_EXCLUDED_ASSETS", "Yes — Family owns one or more of these excluded properties", "Disqualifies applicant from EWS reservation under DoPT Office Memorandum 31.01.2019.", "Asset Disqualified")
                )
        ));

        // Q8: Caste Community Type (Conditional on CASTE_CERTIFICATE)
        questions.add(new QuestionnaireQuestionDto(
                "casteCommunityType",
                "Which caste / community category do you belong to?",
                "Determines statutory quota verification, required ancestral records, and whether Non-Creamy Layer (NCL) income evaluation is needed.",
                "certificateType=CASTE_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("OBC_COMMUNITY", "Other Backward Class (OBC)", "Requires verification of state/central OBC list and Non-Creamy Layer (NCL) income certification.", "27% Central/State Quota"),
                        new QuestionOptionDto("SC_COMMUNITY", "Scheduled Caste (SC)", "Requires paternal lineage and pre-1950 ancestral proof. Lifetime caste status validity.", "15% Constitutional Quota"),
                        new QuestionOptionDto("ST_COMMUNITY", "Scheduled Tribe (ST)", "Requires paternal tribal lineage and customary residence proof. Lifetime validity.", "7.5% Constitutional Quota"),
                        new QuestionOptionDto("SEBC_EBC_STATE", "Socially & Economically Backward Class (SEBC / EBC / VJNT / NT)", "State-notified backward communities governed by state gazettes.", "State Specific Quota")
                )
        ));

        // Q9: Paternal Caste Lineage Proof (Conditional on CASTE_CERTIFICATE)
        questions.add(new QuestionnaireQuestionDto(
                "castePaternalProof",
                "Do you possess paternal lineage caste documentation?",
                "Under Supreme Court guidelines (Madhuri Patil case), caste is inherited from the father and requires paternal documentary evidence.",
                "certificateType=CASTE_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("PATERNAL_CERTIFICATE_AVAILABLE", "Father's or Paternal Relative's Caste Certificate Available", "Direct paternal proof (father, paternal uncle, or grandfather) issued by authorized Tehsildar/SDM.", "Direct Paternal Proof"),
                        new QuestionOptionDto("PATERNAL_SLC_OR_LAND", "Father's / Grandfather's School Leaving Certificate or Land Record with Caste", "Historical documentary evidence indicating caste status without a formal certificate.", "Historical Document"),
                        new QuestionOptionDto("NO_PATERNAL_RECORD", "No Paternal Records Available (Applicant's documents only)", "Requires executive magistrate relationship affidavit, ancestral inquiry, and local panchnama.", "Requires Inquest / Panchnama")
                )
        ));

        // Q10: Annual Family Income Range (Conditional on INCOME_CERTIFICATE)
        questions.add(new QuestionnaireQuestionDto(
                "incomeCertAnnualRange",
                "What is your total annual family gross income from all sources?",
                "Total income from salary, daily wages, agriculture, pension, or business across all earning members.",
                "certificateType=INCOME_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("BELOW_1_LAKH", "Below ₹1,00,000 per annum (BPL / Priority Tier)", "Eligible for 100% full tuition fee reimbursement, government hostel welfare, and priority subsidies.", "BPL Priority"),
                        new QuestionOptionDto("BETWEEN_1_AND_2_5_LAKH", "₹1,00,000 to ₹2,50,000 per annum (Scholarship Tier)", "Eligible for Post-Matric Scholarship (PMS), state educational fee concessions, and EBC schemes.", "Scholarship Eligible"),
                        new QuestionOptionDto("BETWEEN_2_5_AND_8_LAKH", "₹2,50,000 to ₹8,00,000 per annum (Standard Tier)", "Standard family income certification for general admissions, fee concessions, and state welfare.", "Standard Tier"),
                        new QuestionOptionDto("ABOVE_8_LAKH", "Above ₹8,00,000 per annum (Non-Subsidized Tier)", "General income certificate for official financial declarations (not eligible for economic subsidies).", "High Income Tier")
                )
        ));

        // Q11: Income Documentation Type (Conditional on INCOME_CERTIFICATE)
        questions.add(new QuestionnaireQuestionDto(
                "incomeProofType",
                "What income documentation is available for verification?",
                "Revenue department requires documentary evidence to compute official family income.",
                "certificateType=INCOME_CERTIFICATE",
                Arrays.asList(
                        new QuestionOptionDto("SALARY_SLIP_ITR_FORM16", "Salary Slips, Form 16, or Income Tax Returns (ITR)", "Organized sector proof; fastest desk verification.", "Fast-Track Verification"),
                        new QuestionOptionDto("VILLAGE_OFFICER_TALATHI", "Gram Panchayat / Village Revenue Officer / Talathi / Patwari Report", "Rural / unorganized sector income certification based on village land & harvest records.", "Field Survey Verification"),
                        new QuestionOptionDto("SELF_AFFIDAVIT", "Self-Declaration Notarized Affidavit on Stamp Paper", "For daily wage earners or informal sector without formal pay slips.", "Requires Field Verification")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        String certType = answers.getOrDefault("certificateType", "EWS_CERTIFICATE");
        String state = answers.getOrDefault("stateJurisdiction", "MAHARASHTRA");
        String nature = answers.getOrDefault("applicationNature", "FIRST_TIME");
        String purpose = answers.getOrDefault("applicationPurpose", "CENTRAL_ADMISSIONS_JOBS");

        // EWS parameters
        String ewsCategory = answers.getOrDefault("ewsCasteCategory", "GENERAL_UNRESERVED");
        String ewsIncome = answers.getOrDefault("ewsGrossFamilyIncome", "INCOME_BELOW_8_LAKH");
        String ewsAssets = answers.getOrDefault("ewsAssetExclusion", "NO_EXCLUDED_ASSETS");

        // Caste parameters
        String casteComm = answers.getOrDefault("casteCommunityType", "OBC_COMMUNITY");
        String paternalProof = answers.getOrDefault("castePaternalProof", "PATERNAL_CERTIFICATE_AVAILABLE");

        // Income parameters
        String incomeRange = answers.getOrDefault("incomeCertAnnualRange", "BETWEEN_1_AND_2_5_LAKH");
        String incomeProof = answers.getOrDefault("incomeProofType", "SALARY_SLIP_ITR_FORM16");

        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();

        // State Portal, Fee & SLA Lookup
        StateConfig stateCfg = getStateConfig(state, certType);
        result.setOfficialPortalUrl(stateCfg.portalUrl);
        result.setEstimatedTimeline(stateCfg.slaDays);
        result.setCalculatedFee(stateCfg.calculatedFee);
        result.setFeeBreakdown(stateCfg.feeBreakdown);

        // Common Verification & Issuing Authority
        result.setPoliceVerificationMode("Revenue Inspector Desk Verification & Talathi / Patwari Field Survey");
        result.setPoliceVerificationDetails("Verification Protocol: Revenue Inspector (RI) verifies uploaded Aadhaar and property/income data against municipal/land databases. If records are unindexed or informal, Village Revenue Officer (VRO / Talathi / Patwari) conducts spot physical inspection. The certificate is digitally signed by the competent Tehsildar, Sub-Divisional Magistrate (SDM), or Revenue Officer (RO) using PKI Class-3 Digital Signature.");

        long docIdCounter = 1L;
        List<ServiceRequiredDocumentDto> documents = new ArrayList<>();
        List<ServiceStepDto> steps = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        // ====================================================
        // BRANCH 1: EWS CERTIFICATE PATHWAYS
        // ====================================================
        if ("EWS_CERTIFICATE".equalsIgnoreCase(certType)) {

            // Condition 1: Check Category Exclusion (SC/ST/OBC are excluded from EWS)
            if (!"GENERAL_UNRESERVED".equalsIgnoreCase(ewsCategory)) {
                result.setPathwayCode("EWS_INELIGIBLE_CATEGORY_MISMATCH");
                result.setPathwayTitle("Ineligible for EWS — Covered Under Constitutional Quota (" + getCategoryLabel(ewsCategory) + ")");
                result.setBadgeText("Statutory Category Exclusion");
                result.setMatchReason("Under the 103rd Constitutional Amendment Act (Articles 15(6) and 16(6) of the Constitution of India), Economically Weaker Section (EWS) reservation is strictly reserved for General / Unreserved candidates. Citizens belonging to Scheduled Castes (15%), Scheduled Tribes (7.5%), or Other Backward Classes (OBC - 27%) already possess dedicated constitutional reservations and are statutorily ineligible for EWS.");
                result.setValidity("Not Applicable (Category Ineligible)");
                result.setCalculatedFee("₹0");
                result.setFeeBreakdown("No fee applicable — application cannot be processed for EWS.");
                result.setEstimatedTimeline("Ineligible for EWS");

                steps.add(new ServiceStepDto(1L, 1, "Switch to Dedicated Community Reservation", "As an applicant belonging to " + getCategoryLabel(ewsCategory) + ", switch your application to a Caste / Community Certificate or OBC Non-Creamy Layer Certificate to claim your constitutional reservation benefits.", "Immediate", "Constitutional Amendment 103"));

                tips.add("Constitutional Rule: SC, ST, and OBC candidates cannot apply for EWS certificates. Benefit must be availed exclusively through their community's quota.");
                tips.add("Recommended Action: Return to the OnePath Navigator and select 'Caste / Community Certificate' to generate your valid community pathway.");

                result.setSteps(steps);
                result.setCriticalTips(tips);
                return result;
            }

            // Condition 2: Check Income Ceiling (Strictly below ₹8 LPA)
            if ("INCOME_ABOVE_8_LAKH".equalsIgnoreCase(ewsIncome)) {
                result.setPathwayCode("EWS_INELIGIBLE_INCOME_EXCEEDED");
                result.setPathwayTitle("Ineligible for EWS — Family Income Exceeds ₹8,00,000 Ceiling");
                result.setBadgeText("Income Limit Exceeded");
                result.setMatchReason("Under Department of Personnel and Training (DoPT) Office Memorandum No. 36039/1/2019-Estt (Res) dated 31.01.2019, a family's gross annual income from all sources (Salary, Agriculture, Business, Profession) for the financial year prior to application must be strictly below ₹8,00,000. Your declared family income is ₹8,00,000 or more.");
                result.setValidity("Not Applicable (Income Exceeded)");
                result.setCalculatedFee("₹0");
                result.setFeeBreakdown("No fee applicable — application exceeds the statutory ceiling.");
                result.setEstimatedTimeline("Ineligible for EWS");

                steps.add(new ServiceStepDto(1L, 1, "Income Assessment Advisory", "Your family's gross annual income exceeds ₹8,00,000. Candidates with family income of ₹8 LPA or above compete under the Open / General Merit category without EWS reservation.", "Statutory threshold", "DoPT Memo 31.01.2019"));

                tips.add("Family Income Definition: Income includes earnings of applicant, parents, spouse, minor children, and minor siblings. Adult siblings or married siblings' income is not clubbed.");
                tips.add("Submitting false income declarations without ITR alignment will lead to rejection under IPC Section 199 and cancellation of admissions.");

                result.setSteps(steps);
                result.setCriticalTips(tips);
                return result;
            }

            // Condition 3: Check Asset Exclusion (Land, Flat, Plots)
            if ("HAS_EXCLUDED_ASSETS".equalsIgnoreCase(ewsAssets)) {
                result.setPathwayCode("EWS_INELIGIBLE_ASSET_EXCLUDED");
                result.setPathwayTitle("Ineligible for EWS — Ownership of Statutorily Excluded Assets");
                result.setBadgeText("Asset Exclusion Disqualification");
                result.setMatchReason("Although your family income is below ₹8 LPA, under DoPT regulations, a person whose family owns any of the following excluded properties is disqualified from EWS benefits: (1) 5 acres or more of agricultural land, (2) residential flat of 1,000 sq ft or more, (3) municipal plot of 100 sq yds or more, or (4) non-notified plot of 200 sq yds or more. Land across different locations is legally clubbed together.");
                result.setValidity("Not Applicable (Excluded Assets)");
                result.setCalculatedFee("₹0");
                result.setFeeBreakdown("No fee applicable — disqualified due to asset threshold.");
                result.setEstimatedTimeline("Ineligible for EWS");

                steps.add(new ServiceStepDto(1L, 1, "Asset Ceiling Advisory", "Your family owns real estate assets exceeding the statutory EWS threshold. Revenue officials cross-reference municipal property tax records and digital land registries (e-Bhulekh) during scrutiny.", "Statutory restriction", "DoPT Rule 2(b)"));

                tips.add("Clubbing Rule: Properties held by the family across different towns, villages, and states are clubbed together for measuring the 5-acre or flat square footage limits.");

                result.setSteps(steps);
                result.setCriticalTips(tips);
                return result;
            }

            // Condition 4: ELIGIBLE EWS PATHWAYS
            boolean isCentral = "CENTRAL_ADMISSIONS_JOBS".equalsIgnoreCase(purpose);
            boolean isRenewal = "RENEWAL_UPDATE".equalsIgnoreCase(nature);

            if (isRenewal) {
                result.setPathwayCode("EWS_RENEWAL_NEW_FY");
                result.setPathwayTitle("Annual EWS Certificate Re-issue / Renewal (Current Financial Year)");
                result.setBadgeText("Annual FY Re-issue");
                result.setMatchReason("Based on your answers, you appear to meet the stated criteria: You belong to the General/Unreserved category, your gross family income is below ₹8 LPA, and your family owns no excluded property. An EWS certificate is valid for strictly ONE Financial Year (1st April to 31st March). You are updating your certificate for the current financial year.");
            } else if (isCentral) {
                result.setPathwayCode("EWS_CENTRAL_ELIGIBLE");
                result.setPathwayTitle("Central EWS Certificate (DoPT Memorandum Format — 10% Quota)");
                result.setBadgeText("Central 10% Quota");
                result.setMatchReason("Based on your answers, you appear to meet the stated criteria: You are in the General/Unreserved category, gross family income is strictly below ₹8,00,000, and your family owns no excluded assets. You are applying for Central admissions (UPSC, NEET, JEE, CUET, IITs) requiring the mandatory DoPT Annexure-I format.");
            } else {
                result.setPathwayCode("EWS_STATE_ELIGIBLE");
                result.setPathwayTitle("State EWS Certificate (State Educational & Job Quota)");
                result.setBadgeText("State 10% Quota");
                result.setMatchReason("Based on your answers, you appear to meet the stated criteria: General category, income below ₹8 LPA, and no excluded property. Valid for State engineering/medical counselling, state university admissions, and State PSC recruitment.");
            }

            result.setValidity("1 Financial Year (Valid until 31st March of current fiscal year; requires annual renewal)");

            // Documents for EWS
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Proof of Identity (Aadhaar / Voter ID / PAN)", "Applicant's official biometric photo identity document.", true, "Clear PDF / JPEG scan (under 200 KB)", "Download mask-free e-Aadhaar from uidai.gov.in."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Proof of Present Residential Address", "Ration Card, Electricity Bill, Water Bill, or Domicile / Residence Certificate.", true, "Original scanned copy", "Download electricity bill from DISCOM portal or attach residence certificate."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Family Income Evidence (Form 16 / ITR / Salary Slips)", "ITR acknowledgement, Form 16, or salary certificate for the preceding financial year. For agriculture/informal, Tahsildar / VRO income certificate.", true, "Income Tax Return (ITR-V) or Form 16", "Download ITR acknowledgement from e-filing portal (incometax.gov.in)."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Immovable Property & Land Records (7/12, Khatauni, Patta)", "Digital land ownership record or Municipal house tax assessment receipt establishing absence of excluded assets.", true, "Digital Record of Rights (RoR)", "Download certified digital 7/12 or Khatauni from your state's Bhulekh portal (e.g. Mahabhulekh, UP Bhulekh, Bhoomi Karnataka)."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Self-Declaration Affidavit on Non-Judicial Stamp Paper", "Statutory affidavit affirming General category status, family income < ₹8 LPA, and declaration of all family properties.", true, "Original notarized affidavit", "Download official format from state e-District portal; execute on ₹10/₹20 stamp paper and notarize."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Recent Passport Size Color Photograph", "White background photograph (3.5 × 4.5 cm).", true, "JPEG image under 50 KB", "Studio passport photograph scanned cleanly."));

            if (isRenewal) {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Previous Financial Year EWS Certificate Copy", "Scanned copy of previously issued EWS certificate with certificate reference number.", false, "PDF / JPEG copy", "Required for expedited renewal on state e-District portal."));
            }

            // Steps for EWS
            steps.add(new ServiceStepDto(1L, 1, "User Profile & Registration on State e-District Portal", "Visit the official portal (" + stateCfg.portalUrl + ") or nearest Common Service Centre (CSC) / Citizen Service Centre. Create citizen login with mobile and Aadhaar verification.", "10–15 Minutes", "Ensure name matches character-by-character with 10th marksheet and Aadhaar"));
            steps.add(new ServiceStepDto(2L, 2, "Form Filling & Asset Declaration", "Select 'Issuance of EWS Certificate'. Fill personal details, family members' details (applicant, parents, spouse, minor siblings/children), declare gross income across all heads, and itemize residential flat/land holdings.", "20 Minutes", "Declare all properties held across different districts to avoid rejection"));
            steps.add(new ServiceStepDto(3L, 3, "Upload Documents & Pay Statutory Charges", "Upload scanned PDF copies of Aadhaar, address proof, ITR/Form 16, land records (7/12 / Khatauni), and signed self-declaration affidavit. Pay prescribed fee (" + stateCfg.calculatedFee + ") online via Net Banking/UPI.", "10 Minutes", "Download Payment Receipt and save the 15-digit Application Reference Number (ARN)"));
            steps.add(new ServiceStepDto(4L, 4, "Desk Verification & Field Inspection", "Revenue Inspector (RI) verifies uploaded records against land and municipal tax databases. If unindexed, Village Level Worker (VRO / Talathi / Patwari) conducts spot physical inspection of premises.", "5–10 Working Days", "Keep physical original documents available for inspection if summoned"));
            steps.add(new ServiceStepDto(5L, 5, "Approval by Tehsildar / SDM & Digital Download", "Upon favorable verification report, the Sub-Divisional Magistrate (SDM) or Tehsildar signs the certificate using a PKI Class-3 Digital Signature. Download digitally signed certificate with QR code from portal.", stateCfg.slaDays, "Central admissions require DoPT Memo 2019 format carrying digital QR code"));

            // Tips for EWS
            tips.add("Central DoPT Format Warning: For Central admissions (NEET, JEE, UPSC, Central Universities), verify that the issued certificate follows Annexure-I format of DoPT Office Memorandum No. 36039/1/2019-Estt (Res) dated 31.01.2019. State-only formats without the central QR validation clause will be rejected during national counselling.");
            tips.add("Strict Financial Year Validity: An EWS certificate is valid only for the financial year of issue. For admission cycles spanning multiple years, you must ensure the certificate corresponds to the qualifying FY specified by the examining body.");
            tips.add("Clubbing of Land: Never conceal agricultural land or flats owned in other ancestral villages. Revenue authorities cross-verify PAN-linked property purchases and Aadhaar-seeded land registries.");

            result.setDocuments(documents);
            result.setSteps(steps);
            result.setCriticalTips(tips);
            return result;
        }

        // ====================================================
        // BRANCH 2: CASTE / COMMUNITY CERTIFICATE PATHWAYS
        // ====================================================
        if ("CASTE_CERTIFICATE".equalsIgnoreCase(certType)) {

            boolean isObc = "OBC_COMMUNITY".equalsIgnoreCase(casteComm) || "SEBC_EBC_STATE".equalsIgnoreCase(casteComm);
            boolean isSc = "SC_COMMUNITY".equalsIgnoreCase(casteComm);
            boolean isSt = "ST_COMMUNITY".equalsIgnoreCase(casteComm);
            boolean hasPaternalDirect = "PATERNAL_CERTIFICATE_AVAILABLE".equalsIgnoreCase(paternalProof);
            boolean hasPaternalHistoric = "PATERNAL_SLC_OR_LAND".equalsIgnoreCase(paternalProof);
            boolean noPaternal = "NO_PATERNAL_RECORD".equalsIgnoreCase(paternalProof);

            if (isObc) {
                // OBC Non-Creamy Layer vs Creamy Layer check
                // OBC Creamy Layer is when family income is >= 8 LPA
                boolean isCreamyLayer = "INCOME_ABOVE_8_LAKH".equalsIgnoreCase(ewsIncome) || "ABOVE_8_LAKH".equalsIgnoreCase(incomeRange);

                if (isCreamyLayer) {
                    result.setPathwayCode("CASTE_OBC_CREAMY_LAYER");
                    result.setPathwayTitle("OBC Creamy Layer Advisory — General Quota Status");
                    result.setBadgeText("Creamy Layer Exclusion");
                    result.setMatchReason("Based on your answers: Your family's gross annual income is ₹8,00,000 or above, placing you in the 'Creamy Layer' of the Other Backward Classes under DoPT / Mandal Commission guidelines. Creamy Layer candidates are statutorily excluded from the 27% OBC reservation in central/state admissions and recruitment and compete under the General / Unreserved merit category.");
                    result.setValidity("Caste status valid; NCL reservation NOT applicable");
                    result.setCalculatedFee(stateCfg.calculatedFee);
                    result.setFeeBreakdown(stateCfg.feeBreakdown);
                    result.setEstimatedTimeline(stateCfg.slaDays);

                    steps.add(new ServiceStepDto(1L, 1, "Caste Verification vs NCL Distinction", "You may obtain a basic Community Certificate certifying your OBC caste, but you CANNOT be issued an OBC Non-Creamy Layer (NCL) certificate required for claiming reservation quotas in UPSC, NEET, or JEE.", "Statutory advisory", "DoPT Creamy Layer OM"));

                    tips.add("Creamy Layer Rule: Candidates in Creamy Layer do not receive reservation benefits. However, if your family income falls below ₹8 LPA for 3 consecutive financial years, you regain NCL eligibility.");

                } else {
                    result.setPathwayCode("CASTE_OBC_NON_CREAMY");
                    result.setPathwayTitle("OBC Non-Creamy Layer (NCL) Certificate");
                    result.setBadgeText("27% OBC Quota");
                    result.setMatchReason("Based on your answers, you appear to meet the stated criteria: You belong to a recognized Other Backward Class community, your gross annual family income is strictly below ₹8,00,000 (Non-Creamy Layer), and you possess paternal lineage documentation.");
                    result.setValidity("1 to 3 Financial Years for NCL certificate (State rules vary; Central jobs require certificate issued within preceding 1 year)");
                }
            } else if (isSc) {
                result.setPathwayCode("CASTE_SC_CERTIFICATE");
                result.setPathwayTitle("Scheduled Caste (SC) Certificate (Constitutional Quota)");
                result.setBadgeText("15% SC Constitutional Quota");
                result.setMatchReason("Based on your answers: You belong to a recognized Scheduled Caste community with paternal lineage proof. Entitled to 15% reservation, fee concessions, and age relaxation across central and state public services.");
                result.setValidity("Permanent / Lifetime Validity (Caste status is immutable)");
            } else if (isSt) {
                result.setPathwayCode("CASTE_ST_CERTIFICATE");
                result.setPathwayTitle("Scheduled Tribe (ST) Certificate (Constitutional Quota)");
                result.setBadgeText("7.5% ST Constitutional Quota");
                result.setMatchReason("Based on your answers: You belong to a recognized Scheduled Tribe community with paternal lineage proof. Entitled to 7.5% reservation, examination fee waivers, and constitutional protections.");
                result.setValidity("Permanent / Lifetime Validity (Caste status is immutable)");
            } else {
                result.setPathwayCode("CASTE_SEBC_CERTIFICATE");
                result.setPathwayTitle("SEBC / EBC State Backward Class Certificate");
                result.setBadgeText("State Backward Quota");
                result.setMatchReason("Based on your answers: You belong to a state-notified backward community (SEBC/EBC/VJNT) recognized in your state gazette for state educational quotas and competitive exams.");
                result.setValidity("1 Financial Year for NCL component; Lifetime for community status");
            }

            // Documents for Caste Certificate
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Applicant's Identity & School Leaving Certificate", "Aadhaar Card and Primary/Secondary School Leaving Certificate (SLC / TC) stating caste.", true, "Original scanned copies", "Obtain duplicate SLC/TC from school headmaster if misplaced."));

            if (hasPaternalDirect) {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Father's / Paternal Relative's Caste Certificate", "Father's, paternal uncle's, or grandfather's official caste certificate issued by an Executive Magistrate / Tehsildar.", true, "Certified copy", "Download digital copy from state e-District portal or attach certified photocopy."));
            } else if (hasPaternalHistoric) {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Paternal Historical Records (Pre-Cutoff Year)", "Father's or grandfather's school admission register entry, birth register, or ancestral land record prior to cutoff year (1950 for SC/ST; 1961/1967 for OBC).", true, "Certified archival record", "Obtain certified copy from Zilla Parishad education office or District Archives (Abhilekhagar)."));
            } else {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Genealogical Family Tree & Sworn Community Affidavit", "Vanshavali (family tree) attested by Village Sarpanch / Councilor and sworn affidavit before Executive Magistrate affirming paternal lineage.", true, "Notarized affidavit on stamp paper", "Draft genealogy tree from great-grandfather; execute before Executive Magistrate."));
            }

            if (isObc) {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "3-Year Family Income Evidence / ITR (for OBC-NCL)", "Form 16, ITR acknowledgements, or Tehsildar income certificate for the last 3 financial years proving Non-Creamy Layer status (< ₹8 LPA).", true, "3-Year ITR or Salary Slips", "Download ITRs from income tax portal; non-salaried submit VRO income report."));
            }

            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Proof of Continuous State Residence / Domicile", "Ration card, voter ID, electricity bills, or Domicile Certificate proving ancestral residence in the state.", true, "Original scanned copy", "Attach State Domicile Certificate or 10+ years utility bills."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Affidavit Sworn Before Executive Magistrate", "Affidavit declaring caste name, religion, sub-caste, and that applicant has not converted religion.", true, "Stamp paper affidavit", "Format available on state e-District portal."));

            // Steps for Caste Certificate
            steps.add(new ServiceStepDto(1L, 1, "Online Application on State Revenue Portal (" + stateCfg.portalUrl + ")", "Register with Aadhaar OTP on the state portal. Select 'Application for Caste / Community Certificate'. Enter applicant and father's community details.", "15 Minutes", "Ensure spelling of community strictly matches State/Central Gazette list"));
            steps.add(new ServiceStepDto(2L, 2, "Upload Paternal Documents & Community Affidavit", "Upload applicant's School Leaving Certificate, father's caste proof, residential records, and sworn magistrate affidavit.", "15 Minutes", "Missing paternal proof will trigger extended inquiry and delay SLA"));
            steps.add(new ServiceStepDto(3L, 3, "Fee Payment & ARN Generation", "Pay statutory fee (" + stateCfg.calculatedFee + ") online. Save 15-digit Application Reference Number for tracking.", "5 Minutes", "Print acknowledgement slip with trackable barcode"));
            steps.add(new ServiceStepDto(4L, 4, "Field Verification & Vigilance Inquiry", "Revenue Inspector (RI) and Talathi / Patwari verify family lineage in the village records. For SC/ST applications, Scrutiny Committee / Vigilance Cell verification may be initiated.", "10–25 Working Days", "Ensure local neighbours and village elders can attest to family ancestry"));
            steps.add(new ServiceStepDto(5L, 5, "Approval by Competent Authority & Digital Delivery", "Sub-Divisional Magistrate (SDM), Tehsildar, or Revenue Officer issues digitally signed Caste Certificate with secure QR code.", stateCfg.slaDays, "Central admissions require Central format mentioning Government of India Resolution number"));

            tips.add("Madhuri Patil Doctrine: Caste is determined strictly by paternal birth lineage (father's side). Maternal caste cannot be claimed under Indian law.");
            tips.add("Central vs State OBC List: Confirm that your community is notified in the Central List of OBCs maintained by NCBC (ncbc.nic.in) before applying for central institutions (IIT, AIIMS, Central Universities).");
            tips.add("Cutoff Year Evidence: For SC/ST certification, maintain documentary evidence proving ancestral residence in the state prior to 10th August 1950 (Presidential Order).");

            result.setDocuments(documents);
            result.setSteps(steps);
            result.setCriticalTips(tips);
            return result;
        }

        // ====================================================
        // BRANCH 3: INCOME CERTIFICATE PATHWAYS
        // ====================================================
        if ("INCOME_CERTIFICATE".equalsIgnoreCase(certType)) {

            boolean isBpl = "BELOW_1_LAKH".equalsIgnoreCase(incomeRange);
            boolean isScholarship = "BETWEEN_1_AND_2_5_LAKH".equalsIgnoreCase(incomeRange);
            boolean isStandard = "BETWEEN_2_5_AND_8_LAKH".equalsIgnoreCase(incomeRange);
            boolean isAbove8 = "ABOVE_8_LAKH".equalsIgnoreCase(incomeRange);

            if (isBpl) {
                result.setPathwayCode("INCOME_CERT_PRIORITY_BPL");
                result.setPathwayTitle("Family Income Certificate — Priority BPL / Fee Waiver Tier (< ₹1,00,000)");
                result.setBadgeText("BPL / Priority Tier");
                result.setMatchReason("Based on your answers, you appear to meet the stated criteria: Your declared family income is below ₹1,00,000 per annum, qualifying for 100% full tuition fee reimbursement, government hostel welfare, and priority subsidies.");
                result.setValidity("1 Financial Year (Requires annual renewal for educational fee waivers)");
            } else if (isScholarship) {
                result.setPathwayCode("INCOME_CERT_SCHOLARSHIP_TIER");
                result.setPathwayTitle("Family Income Certificate — Post-Matric Scholarship Tier (₹1,00,000 – ₹2,50,000)");
                result.setBadgeText("Scholarship Tier");
                result.setMatchReason("Based on your answers, you appear to meet the stated criteria: Your family income falls in the ₹1,00,000 – ₹2,50,000 bracket, making you eligible for National Scholarship Portal (NSP) schemes, State Post-Matric Scholarships (PMS), and fee concessions.");
                result.setValidity("1 Financial Year (Valid until March 31st of current fiscal year)");
            } else if (isStandard) {
                result.setPathwayCode("INCOME_CERT_STANDARD");
                result.setPathwayTitle("Family Income Certificate — Standard Educational & Welfare Tier (₹2.5L – ₹8L)");
                result.setBadgeText("Standard Tier");
                result.setMatchReason("Based on your answers, you appear to meet the stated criteria: Official certification of annual family income between ₹2,50,000 and ₹8,00,000 for university admissions, state quotas, and institutional financial validation.");
                result.setValidity("1 Financial Year / 3 Years (Depending on state RTS rules)");
            } else {
                result.setPathwayCode("INCOME_CERT_ABOVE_8_LPA");
                result.setPathwayTitle("Family Income Certificate — Non-Subsidized Assessment (≥ ₹8,00,000)");
                result.setBadgeText("Non-Subsidized Assessment");
                result.setMatchReason("Official certification of family income of ₹8,00,000 or above for formal legal declarations, bank collateral verification, or non-subsidized admission requirements.");
                result.setValidity("1 Financial Year");
            }

            // Documents for Income Certificate
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Proof of Identity of Family Head & Applicant", "Aadhaar Card, Voter ID, or PAN Card of applicant and earning parents.", true, "Clear PDF / JPEG scan", "Download mask-free e-Aadhaar from uidai.gov.in."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Proof of Present Residential Address", "Ration Card (NFSA / BPL / APL), Electricity Bill, or Municipal Tax Receipt.", true, "Original scanned copy", "Attach family Ration Card containing names of all members."));

            if ("SALARY_SLIP_ITR_FORM16".equalsIgnoreCase(incomeProof)) {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Salary Certificate / Form 16 / ITR Acknowledgement", "Latest 3 months salary slips, employer Form 16, or ITR-V filing acknowledgement.", true, "PDF document", "Download Form 16 from employer or ITR-V from incometax.gov.in."));
            } else if ("VILLAGE_OFFICER_TALATHI".equalsIgnoreCase(incomeProof)) {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Village Revenue Officer (VRO / Talathi) Income Report", "Field report evaluating agricultural harvest, livestock, or daily wages.", true, "Original signed report", "Obtain from Talathi / Village Administrative Officer (VAO)."));
            } else {
                documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Notarized Self-Declaration Income Affidavit", "Affidavit declaring annual income from all informal sources sworn before Notary / Executive Magistrate.", true, "Stamped paper affidavit", "Draft on ₹20 stamp paper stating monthly income breakdown."));
            }

            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Land Records / 7-12 / Khatauni (If Agricultural Income Declared)", "Certified extract of agricultural land holding to compute farm revenue.", false, "Digital RoR copy", "Download certified digital 7/12 or Khatauni from state Bhulekh portal."));
            documents.add(new ServiceRequiredDocumentDto(docIdCounter++, "Recent Passport Size Color Photograph", "White background passport photograph of applicant.", true, "JPEG under 50 KB", "Clear uncropped photograph."));

            // Steps for Income Certificate
            steps.add(new ServiceStepDto(1L, 1, "Application Submission on State e-District Portal (" + stateCfg.portalUrl + ")", "Log in to the state portal or visit your nearest CSC / MeeSeva centre. Select 'Application for Income Certificate'. Enter family member details and declared income sources.", "15 Minutes", "Income must include all earning family members (parents, spouse, applicant)"));
            steps.add(new ServiceStepDto(2L, 2, "Upload Documents & Pay Statutory Charges", "Upload identity proofs, address proof, income evidence (ITR/Salary slip or VRO report), and notarized affidavit. Pay statutory fee (" + stateCfg.calculatedFee + ").", "10 Minutes", "Download Payment Acknowledgement with Application Number"));
            steps.add(new ServiceStepDto(3L, 3, "Desk Verification & Field Inspection by Talathi / Patwari", "Revenue Inspector (RI) cross-verifies declared income against municipal records and ITR databases. For informal sector applicants, Talathi/Patwari visits residence for neighbour enquiry.", "5–10 Working Days", "Ensure declared income matches standard living indicators"));
            steps.add(new ServiceStepDto(4L, 4, "Digital Approval & Certificate Issuance", "Tehsildar / Sub-Divisional Magistrate digitally signs the Income Certificate with secure QR code. Citizen receives SMS with direct download link.", stateCfg.slaDays, "Download and save the PDF on DigiLocker for instant institutional verification"));

            tips.add("Mismatch Warning: Discrepancy between declared family income and ITR filings or employer Form 16 will result in immediate rejection by the Revenue Inspector.");
            tips.add("Scholarship Window: Apply for your income certificate at least 30 days before scholarship portal deadlines (e.g. NSP, MahaDBT, SSP Karnataka) to avoid last-minute portal rush.");
            tips.add("DigiLocker Integration: Once issued, link your Certificate Number with DigiLocker. Educational institutions can verify authenticity instantly without physical stamp verification.");

            result.setDocuments(documents);
            result.setSteps(steps);
            result.setCriticalTips(tips);
            return result;
        }

        // Default Fallback
        result.setPathwayCode("REVENUE_CERTIFICATE_STANDARD");
        result.setPathwayTitle("Revenue Department Certificate Issuance");
        result.setBadgeText("Standard Issuance");
        result.setMatchReason("Standard revenue certificate issuance through official state e-District portal.");
        result.setValidity("1 Financial Year");
        return result;
    }

    private StateConfig getStateConfig(String state, String certType) {
        if ("MAHARASHTRA".equalsIgnoreCase(state)) {
            String sla = "CASTE_CERTIFICATE".equalsIgnoreCase(certType) ? "45 Working Days (Sakala / RTS Act)" : "15 Working Days (Sakala / RTS Act)";
            return new StateConfig(
                    "https://aaplesarkar.mahaonline.gov.in",
                    "₹20.00 (Online Self) / ₹53.60 (CSC Kiosk)",
                    "Statutory Govt Fee: ₹20.00 + MahaOnline CSC Kiosk Service Charge: ₹33.60 (Total: ₹53.60 if applied via CSC)",
                    sla
            );
        } else if ("DELHI".equalsIgnoreCase(state)) {
            String sla = "CASTE_CERTIFICATE".equalsIgnoreCase(certType) ? "14 Days (OBC) / 60 Days (SC-ST)" : "14 Working Days";
            return new StateConfig(
                    "https://edistrict.delhigovt.nic.in",
                    "NIL (100% Free Service)",
                    "Official Govt Fee: NIL (Free) — Delhi Govt Citizen Charter Guarantee",
                    sla
            );
        } else if ("KARNATAKA".equalsIgnoreCase(state)) {
            return new StateConfig(
                    "https://nadakacheri.karnataka.gov.in",
                    "₹25.00 (Online) / ₹40.00 (CSC)",
                    "Statutory Govt Fee: ₹25.00 + Seva Sindhu / Bangalore One Kiosk Charge: ₹15.00",
                    "21 Working Days (Guaranteed under Karnataka Sakala Act)"
            );
        } else if ("UTTAR_PRADESH".equalsIgnoreCase(state)) {
            return new StateConfig(
                    "https://edistrict.up.gov.in",
                    "₹15.00 (Citizen Self) / ₹30.00 (Jan Seva)",
                    "Statutory Govt Portal Fee: ₹15.00 (Self) | Jan Seva Kendra / CSC Fee: ₹30.00",
                    "15 to 20 Working Days (UP Lok Seva Guarantee)"
            );
        } else if ("TAMIL_NADU".equalsIgnoreCase(state)) {
            return new StateConfig(
                    "https://www.tnesevai.tn.gov.in",
                    "₹60.00",
                    "Tamil Nadu e-Governance Agency (TNeGA) e-Sevai Fee: ₹60.00",
                    "15 to 30 Working Days"
            );
        } else if ("BIHAR".equalsIgnoreCase(state)) {
            return new StateConfig(
                    "https://serviceonline.bihar.gov.in",
                    "NIL (Free Self) / ₹10–₹15 (CSC)",
                    "Official RTPS Bihar Portal Fee: NIL (Free) | CSC / Vasudha Kendra Kiosk Fee: ₹10.00 – ₹15.00",
                    "10 to 15 Working Days (Bihar Right to Public Services Act)"
            );
        } else if ("TELANGANA_AP".equalsIgnoreCase(state)) {
            return new StateConfig(
                    "https://ts.meeseva.telangana.gov.in",
                    "₹45.00 (Online) / ₹60.00 (MeeSeva)",
                    "Statutory Department Fee: ₹45.00 + MeeSeva Center User Charge: ₹15.00",
                    "30 Working Days"
            );
        } else {
            return new StateConfig(
                    "https://serviceonline.gov.in",
                    "₹15.00 – ₹50.00 (State specific)",
                    "Official State e-District portal fee (typically ₹15 to ₹50 depending on state notification)",
                    "15 to 30 Working Days"
            );
        }
    }

    private String getCategoryLabel(String code) {
        if ("OBC_NON_CREAMY".equalsIgnoreCase(code)) return "OBC (Non-Creamy Layer)";
        if ("OBC_CREAMY".equalsIgnoreCase(code)) return "OBC (Creamy Layer)";
        if ("SCHEDULED_CASTE".equalsIgnoreCase(code)) return "Scheduled Caste (SC)";
        if ("SCHEDULED_TRIBE".equalsIgnoreCase(code)) return "Scheduled Tribe (ST)";
        return "Reserved Category";
    }

    private static class StateConfig {
        final String portalUrl;
        final String calculatedFee;
        final String feeBreakdown;
        final String slaDays;

        StateConfig(String portalUrl, String calculatedFee, String feeBreakdown, String slaDays) {
            this.portalUrl = portalUrl;
            this.calculatedFee = calculatedFee;
            this.feeBreakdown = feeBreakdown;
            this.slaDays = slaDays;
        }
    }
}
