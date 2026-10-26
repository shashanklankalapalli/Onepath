package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Master Pathway Evaluator for JoSAA, CSAB, and State Engineering Admissions Counselling
 * based on official Joint Seat Allocation Authority (JoSAA), Central Seat Allocation Board (CSAB),
 * and Andhra Pradesh State Council of Higher Education (APSCHE / AP EAPCET) regulations.
 */
@Component
public class JosaaCounsellingPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String JOSAA_SLUG = "josaa-state-admissions-counselling";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return JOSAA_SLUG.equalsIgnoreCase(s)
                || "josaa-counselling".equalsIgnoreCase(s)
                || "josaa".equalsIgnoreCase(s)
                || "josaa-state-college-admissions-counselling".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(JOSAA_SLUG);
        dto.setServiceName(service != null ? service.getName() : "JoSAA & State College Admissions Counselling");
        dto.setTitle("Counselling Eligibility & Route Navigator");
        dto.setDescription("Answer a few quick questions regarding your entrance examination, category, Class 12 board aggregate, and home state to determine your exact counselling pathways, eligible quotas, and personalized document checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Qualifying Entrance Examination
        questions.add(new QuestionnaireQuestionDto(
                "targetExam",
                "Which engineering entrance examination(s) did you take?",
                "Select your qualifying examination to determine central vs state institutional eligibility.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("JEE_ADVANCED", "JEE (Advanced) Qualified (Targeting 23 IITs)", "Qualified in JEE Advanced; eligible for all 23 IITs and all NIT+ institutions.", "Eligible for 23 IITs"),
                        new QuestionOptionDto("JEE_MAIN", "JEE (Main) Paper 1 Qualified (NITs, IIITs & GFTIs)", "Qualified in JEE Main B.E./B.Tech; eligible for 31 NITs, 26 IIITs, IIEST Shibpur, and 38 GFTIs.", "Central NIT+ System"),
                        new QuestionOptionDto("AP_EAPCET", "AP EAPCET Qualified (Andhra Pradesh State Engineering)", "Qualified in Andhra Pradesh state engineering entrance test for JNTUK, JNTUA, AU, and state colleges.", "AP State Universities"),
                        new QuestionOptionDto("BOTH_JEE_AND_EAPCET", "Both JEE and AP EAPCET (Dual Counselling)", "Holding qualifying scores in both national (JEE) and Andhra Pradesh state exams; evaluating both routes.", "Dual Track National & State")
                )
        ));

        // Q2: Candidate Category & Reservation
        questions.add(new QuestionnaireQuestionDto(
                "category",
                "What is your candidate category for seat reservation?",
                "Determines category rank cutoffs, seat reservation percentages, and certificate verification rules.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("GEN", "General / Open (CRL)", "Unreserved seats allocated strictly based on Common Rank List merit.", "Open / Unreserved"),
                        new QuestionOptionDto("GEN_EWS", "General-EWS (10% Central Quota)", "Economically Weaker Section with annual family income below ₹8 Lakhs. Requires Central format certificate.", "Strict April 1 Validity"),
                        new QuestionOptionDto("OBC_NCL", "OBC-NCL (27% Central Quota)", "Other Backward Classes Non-Creamy Layer in Central List. Mandatory certificate issued ON or AFTER April 1.", "Strict April 1 Validity"),
                        new QuestionOptionDto("SC", "SC (Scheduled Caste - 15% Quota)", "Permanent community certificate issued by competent statutory revenue authority.", "Statutory Category"),
                        new QuestionOptionDto("ST", "ST (Scheduled Tribe - 7.5% Quota)", "Permanent tribe certificate issued by competent statutory authority.", "Statutory Category"),
                        new QuestionOptionDto("PWD", "PwD (Persons with Benchmark Disability - 5% Quota)", "Unique Disability ID (UDID) or Medical Board certificate showing ≥ 40% disability.", "5% Horizontal Quota")
                )
        ));

        // Q3: Class 12th Board Marks Aggregate
        questions.add(new QuestionnaireQuestionDto(
                "boardScore",
                "What is your Class 12th Board examination aggregate score?",
                "Mandatory dual eligibility requirement calculated across 5 compulsory subjects: Physics, Math, Chemistry/Vocational, Language, and 5th subject.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("ABOVE_75", "≥ 75% Aggregate in Class 12 (or Top 20 Percentile in Board)", "Fully satisfies central qualifying standards for all 23 IITs, 31 NITs, IIITs, and GFTIs.", "100% Eligible for IITs/NITs"),
                        new QuestionOptionDto("BETWEEN_65_75", "65.0% to 74.9% Aggregate in Class 12", "Meets central cutoff for SC, ST, and PwD candidates (65% rule), and meets all State Engineering criteria.", "SC/ST/PwD Central Cutoff"),
                        new QuestionOptionDto("BETWEEN_45_65", "45.0% to 64.9% Aggregate (PCM)", "Meets Andhra Pradesh State Engineering (AP EAPCET) minimum cutoff (≥ 45% for OC, ≥ 40% for Reserved).", "Eligible for AP State Colleges"),
                        new QuestionOptionDto("BELOW_45", "Below 45% Aggregate in Class 12", "Does not satisfy minimum statutory technical degree admission standards; requires improvement exam.", "Eligibility Alert")
                )
        ));

        // Q4: State Code of Eligibility / Domicile
        questions.add(new QuestionnaireQuestionDto(
                "homeState",
                "What is your State Code of Eligibility (State where you passed Class 12)?",
                "Crucial for determining 50% Home State (HS) quota in NITs and 85% Local Area quota in Andhra Pradesh.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("ANDHRA_PRADESH", "Andhra Pradesh (Passed Class 12 in AP)", "Qualifies for 50% Home State quota at NIT Andhra Pradesh (Tadepalligudem) and 85% Local Quota in AU/SVU regions.", "Home State AP Quota"),
                        new QuestionOptionDto("TELANGANA", "Telangana (Passed Class 12 in TS)", "Qualifies for 50% Home State quota at NIT Warangal and local quota in Telangana state counselling.", "Home State TS Quota"),
                        new QuestionOptionDto("OTHER_STATE", "Other Indian State / Union Territory", "Qualifies for Home State quota at your respective state NIT and 50% Other State quota across all national NITs.", "All-India Other State Quota")
                )
        ));

        // Q5: Preferred Counselling Route
        questions.add(new QuestionnaireQuestionDto(
                "preferredRoute",
                "Which counselling route do you wish to prioritize?",
                "Select your focus to receive tailored procedural guidance and step-by-step reporting instructions.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("JOSAA_CENTRAL", "JoSAA Central Counselling (IITs, NITs, IIITs, GFTIs)", "6 centralized allocation rounds covering 118 premier national technical institutes.", "National JoSAA Track"),
                        new QuestionOptionDto("AP_EAPCET_STATE", "Andhra Pradesh State Engineering Web Counselling (APSCHE)", "Centralized state web counselling for JNTU Kakinada, JNTU Anantapur, Andhra University, and affiliated colleges.", "AP State Convenor Track"),
                        new QuestionOptionDto("CSAB_SPECIAL", "CSAB Special / Spot Allocation Rounds", "2 post-JoSAA special rounds filling vacant seats in NITs, IIITs, and GFTIs using JEE Main CRL Rank.", "Post-JoSAA Vacant Seats"),
                        new QuestionOptionDto("DUAL_COUNSELLING", "Dual Track: Both JoSAA Central and AP State Counselling", "Holistic guidance tracking national JoSAA choices and AP State options simultaneously.", "Comprehensive Dual Track")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();

        String targetExam = answers.getOrDefault("targetExam", "JEE_MAIN");
        String category = answers.getOrDefault("category", "GEN");
        String boardScore = answers.getOrDefault("boardScore", "ABOVE_75");
        String homeState = answers.getOrDefault("homeState", "ANDHRA_PRADESH");
        String preferredRoute = answers.getOrDefault("preferredRoute", "JOSAA_CENTRAL");

        boolean isIitEligible = "JEE_ADVANCED".equalsIgnoreCase(targetExam) && ("ABOVE_75".equalsIgnoreCase(boardScore) || (("SC".equalsIgnoreCase(category) || "ST".equalsIgnoreCase(category) || "PWD".equalsIgnoreCase(category)) && "BETWEEN_65_75".equalsIgnoreCase(boardScore)));
        boolean isNitEligible = ("JEE_MAIN".equalsIgnoreCase(targetExam) || "JEE_ADVANCED".equalsIgnoreCase(targetExam) || "BOTH_JEE_AND_EAPCET".equalsIgnoreCase(targetExam)) && ("ABOVE_75".equalsIgnoreCase(boardScore) || (("SC".equalsIgnoreCase(category) || "ST".equalsIgnoreCase(category) || "PWD".equalsIgnoreCase(category)) && "BETWEEN_65_75".equalsIgnoreCase(boardScore)));
        boolean isApStateEligible = ("AP_EAPCET".equalsIgnoreCase(targetExam) || "BOTH_JEE_AND_EAPCET".equalsIgnoreCase(targetExam) || "ANDHRA_PRADESH".equalsIgnoreCase(homeState)) && !"BELOW_45".equalsIgnoreCase(boardScore);
        boolean isCsabRoute = "CSAB_SPECIAL".equalsIgnoreCase(preferredRoute);
        boolean isDualTrack = "DUAL_COUNSELLING".equalsIgnoreCase(preferredRoute) || "BOTH_JEE_AND_EAPCET".equalsIgnoreCase(targetExam);

        // 1. Determine Pathway Code, Title, and Match Reason
        if (isIitEligible && !"AP_EAPCET_STATE".equalsIgnoreCase(preferredRoute)) {
            result.setPathwayCode("JOSAA_IIT_ADVANCED");
            result.setPathwayTitle("JoSAA Central Counselling — IIT & Premier Technical Institutes Pathway");
            result.setBadgeText("IITs & Premier National Institutes");
            result.setMatchReason("Based on your answers: You have qualified in JEE (Advanced) and satisfy the Class 12 board aggregate requirement (≥ 75% or Top 20 percentile). You are fully eligible to fill choices for all 23 IITs, 31 NITs, 26 IIITs, IIEST Shibpur, and 38 GFTIs in JoSAA 6 Regular Allocation Rounds.");
        } else if (isNitEligible && !"AP_EAPCET_STATE".equalsIgnoreCase(preferredRoute)) {
            if (isCsabRoute) {
                result.setPathwayCode("CSAB_SPECIAL_SPOT");
                result.setPathwayTitle("CSAB Special Rounds — Post-JoSAA Vacant Seat Spot Allocation");
                result.setBadgeText("CSAB Special Spot Rounds");
                result.setMatchReason("Based on your answers: You hold a valid JEE Main CRL Rank and satisfy the Class 12 board aggregate requirement. You are targeting vacant seats in the NIT+ system (NITs, IIITs, GFTIs) after the conclusion of JoSAA Round 6.");
            } else if (isDualTrack) {
                result.setPathwayCode("DUAL_JOSAA_AP_EAPCET");
                result.setPathwayTitle("Dual Track Counselling — JoSAA Central & Andhra Pradesh EAPCET Web Counselling");
                result.setBadgeText("National & State Dual Track");
                result.setMatchReason("Based on your answers: You qualify for both Central technical admissions through JoSAA (NIT+ System) and Andhra Pradesh State Engineering Admissions (AP EAPCET) for university colleges (JNTUK, JNTUA, AU, SVU). You can participate in both systems concurrently without conflict.");
            } else {
                result.setPathwayCode("JOSAA_NIT_PLUS");
                result.setPathwayTitle("JoSAA Central Counselling — NITs, IIITs & Other-GFTIs Pathway");
                result.setBadgeText("NIT+ System (NITs, IIITs, GFTIs)");
                result.setMatchReason("Based on your answers: You hold a valid JEE Main Paper 1 rank and satisfy the 75% Class 12 board marks standard (or 65% for reserved category). You are eligible for 31 NITs, 26 IIITs, IIEST Shibpur, and 38 GFTIs across 6 JoSAA rounds.");
            }
        } else if (isApStateEligible) {
            result.setPathwayCode("AP_EAPCET_STATE");
            result.setPathwayTitle("Andhra Pradesh State Engineering Web Counselling (APSCHE / AP EAPCET)");
            result.setBadgeText("AP State Engineering Convenor Quota");
            result.setMatchReason("Based on your answers: You are participating in Andhra Pradesh state engineering admissions. You qualify for 85% Local Area seats (AU / SVU regions) across premier state universities including JNTU Kakinada, JNTU Anantapur, Andhra University College of Engineering, and state government/aided institutes.");
        } else {
            result.setPathwayCode("COUNSELLING_GUIDANCE_GENERAL");
            result.setPathwayTitle("Engineering Admissions Counselling & Eligibility Advisory");
            result.setBadgeText("Admissions Advisory");
            result.setMatchReason("Based on your answers: Review your qualification and board scores against statutory technical degree criteria. Central institutes (IITs/NITs) mandate ≥ 75% aggregate in 5 compulsory Class 12 subjects (65% for SC/ST/PwD). State technical degrees require ≥ 45% aggregate in PCM (40% for reserved categories).");
        }

        // 2. Validity & Scope
        result.setValidity("Valid for the Current Academic Session (One-time seat allotment lifecycle across 6 JoSAA rounds + 2 CSAB rounds / 3 AP State rounds)");

        // 3. Quota & Domicile Distribution
        String quotaNote;
        if ("ANDHRA_PRADESH".equalsIgnoreCase(homeState)) {
            quotaNote = "Andhra Pradesh Home State Quota (50% seats reserved at NIT Andhra Pradesh, Tadepalligudem) + 50% All-India Other State Quota across remaining 30 NITs + 85% Local Area Quota (AU / SVU regions) in AP State Engineering Colleges.";
        } else if ("TELANGANA".equalsIgnoreCase(homeState)) {
            quotaNote = "Telangana Home State Quota (50% seats reserved at NIT Warangal) + 50% All-India Other State Quota across remaining 30 NITs + State Counselling Quota in Telangana.";
        } else {
            quotaNote = "Home State Quota (50% seats reserved at your state's respective NIT) + 50% Other State Quota across all national NITs + All-India Open Seats in IITs and IIITs.";
        }
        result.setEmigrationStatus("Quota Allocation Split: " + quotaNote + " (Plus mandatory 20% Female-Only Supernumerary Pool across IITs and NITs).");

        // 4. Verification Mode & Reporting Protocol
        result.setPoliceVerificationMode("Online Document Verification & Query Resolution (Mandatory 24-Hour SLA)");
        result.setPoliceVerificationDetails("Verification is conducted online by Virtual Reporting Centres (VRC). In case of discrepancy, Document Verification Officers raise online queries that MUST be answered within 24 hours. Physical reporting and original certificate submission occurs at the allotted institute upon final round conclusion.");

        // 5. Fees & Financial Structure
        boolean isReserved = "SC".equalsIgnoreCase(category) || "ST".equalsIgnoreCase(category) || "PWD".equalsIgnoreCase(category);
        String calculatedFee;
        String feeBreakdown;

        if ("AP_EAPCET_STATE".equalsIgnoreCase(result.getPathwayCode())) {
            calculatedFee = isReserved ? "₹600.00 (Counselling Fee)" : "₹1,200.00 (Counselling Fee)";
            feeBreakdown = "AP EAPCET Web Counselling Processing Fee: " + (isReserved ? "₹600 for SC/ST" : "₹1,200 for OC/BC") + ". College tuition fee is payable at the allotted institute (eligible students receive fee reimbursement under state welfare schemes).";
        } else if ("CSAB_SPECIAL_SPOT".equalsIgnoreCase(result.getPathwayCode())) {
            calculatedFee = isReserved ? "₹25,000.00 (Special Round Participation)" : "₹45,000.00 (Special Round Participation)";
            feeBreakdown = "CSAB Special Round Participation Fee: " + (isReserved ? "₹25,000 (includes ₹5,000 non-refundable processing charge)" : "₹45,000 (includes ₹5,000 non-refundable processing charge)") + ". The balance is fully adjusted toward institute tuition upon seat allotment.";
        } else {
            calculatedFee = isReserved ? "₹15,000.00 (Seat Acceptance Fee)" : "₹35,000.00 (Seat Acceptance Fee)";
            feeBreakdown = "Seat Acceptance Fee (SAF): " + (isReserved ? "₹15,000 for SC, ST, PwD" : "₹35,000 for GEN, GEN-EWS, OBC-NCL") + ". Portal registration and choice filling are completely free. SAF is payable ONLY after a seat is allotted, and is 100% adjusted toward the student's first-year institute tuition fee.";
        }

        result.setCalculatedFee(calculatedFee);
        result.setFeeBreakdown(feeBreakdown);
        result.setEstimatedTimeline("Registration to Final Allotment: Approximately 4 to 6 Weeks (6 JoSAA Central Rounds + 2 CSAB Special Rounds / 3 AP State Rounds)");
        result.setOfficialPortalUrl("https://josaa.nic.in");

        // 6. Tailored Step-by-Step Guidance
        List<ServiceStepDto> steps = new ArrayList<>();
        steps.add(new ServiceStepDto(
                1,
                "Portal Authentication & Profile Verification",
                "Log into official JoSAA portal (https://josaa.nic.in) or AP EAPCET portal (https://cets.apsche.ap.gov.in) using your JEE Application Number / EAPCET Hall Ticket and Password. Carefully verify state code of eligibility, gender, and category details.",
                "15–20 Minutes",
                "Ensure your state code of eligibility matches the state where you actually appeared for Class 12, not your permanent residential address."
        ));

        steps.add(new ServiceStepDto(
                2,
                "Strategic Choice Filling & Priority Arrangement",
                "Fill college and branch preferences in STRICT order of your authentic personal preference. You have UNLIMITED choices. Put your dream programs at the top, realistic choices in the middle, and reliable backup options at the bottom.",
                "2–3 Days",
                "Golden Rule: Never place a lower-preferred option above a higher-preferred option simply because of previous year cutoffs. The system always evaluates choices from top (Choice #1) downwards."
        ));

        steps.add(new ServiceStepDto(
                3,
                "Mock Allotment Review & Mandatory Choice Locking",
                "Review the two official Mock Allotment results published to understand indicative seat trends. Make necessary adjustments. Ensure you LOCK your choices before the announced deadline timestamp.",
                "1 Hour",
                "If not manually locked, system auto-locks your last saved preferences at the deadline. Locked choices cannot be modified in subsequent regular rounds."
        ));

        steps.add(new ServiceStepDto(
                4,
                "Seat Allotment Result & Mandatory Willingness Selection",
                "Upon announcement of allotment results, log in immediately. If a seat is allotted, you MUST select one of three mandatory willingness options:\n" +
                        "• FREEZE: You accept the allotted seat and exit all subsequent rounds. No further upgrading.\n" +
                        "• FLOAT: You accept the allotted seat but wish to upgrade to a higher-preference academic program at ANY institute in next rounds.\n" +
                        "• SLIDE: You accept the allotted seat but wish to upgrade to a higher-preference academic program WITHIN THE SAME institute in next rounds.",
                "Same Day",
                "Selecting Float or Slide guarantees your current seat while keeping you in contention for higher choices."
        ));

        steps.add(new ServiceStepDto(
                5,
                "Seat Acceptance Fee (SAF) Payment & Document Upload",
                "Pay the mandatory Seat Acceptance Fee online (₹35,000 for Gen/OBC/EWS, ₹15,000 for SC/ST/PwD) via Net Banking, UPI, or Card. Upload clear scans of required certificates: Class 10 (DOB), Class 12, Medical Certificate (Annexure-7), Category/PwD Proof, and Cancelled Cheque.",
                "1–2 Hours",
                "CRITICAL WARNING: If a seat is allotted and you fail to pay SAF or upload documents within the round deadline, your allotted seat is CANCELLED and you are PERMANENTLY DEBARRED from all subsequent JoSAA rounds."
        ));

        steps.add(new ServiceStepDto(
                6,
                "Online Document Verification & Query Resolution (24-Hour SLA)",
                "Virtual Reporting Centre (VRC) officers verify uploaded certificates. If an officer finds a discrepancy (e.g. unclear scan, category certificate issue), an online query is raised on your portal. You MUST respond and provide clarification within 24 hours.",
                "24 Hours Max",
                "Check your portal twice daily during reporting windows. Failure to resolve queries results in seat cancellation."
        ));

        steps.add(new ServiceStepDto(
                7,
                "Subsequent Rounds & CSAB Special Round Participation",
                "Monitor rounds 2 through 6. Your seat may upgrade automatically based on your Float/Slide selection. After Round 6, if you still seek a better branch or vacant seat in NITs/IIITs/GFTIs, register for CSAB Special Rounds (csab.nic.in) using your JEE Main CRL rank.",
                "1–2 Weeks",
                "Seats secured in JoSAA can be retained while exploring upgrades in CSAB Special Rounds."
        ));

        steps.add(new ServiceStepDto(
                8,
                "Physical Reporting & Institutional Admission",
                "At the conclusion of the counselling cycle, download the Final Provisional Seat Allotment Letter. Pay remaining institutional admission tuition fees online, and report physically to the allotted campus with all original documents for final verification and hostel allocation.",
                "Designated Dates",
                "Carry at least 3 sets of self-attested photocopies, 6 passport-size photographs, and medical certificate."
        ));

        result.setSteps(steps);

        // 7. Comprehensive Document Verification Checklist
        List<ServiceRequiredDocumentDto> docs = new ArrayList<>();

        docs.add(new ServiceRequiredDocumentDto(
                "Class 10 Certificate / Marks Sheet",
                "Statutory proof of candidate date of birth (DOB) and candidate name matching entrance application exactly.",
                true,
                "Original PDF scan (under 500 KB)",
                "Issued by CBSE / ICSE / State Secondary Board. DigiLocker verified copy is acceptable."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Class 12 Marksheet & Passing Certificate",
                "Proof of qualifying examination and aggregate percentage across 5 compulsory subjects (Physics, Math, Chem/Vocational, Language, 5th subject).",
                true,
                "Original PDF scan (under 500 KB)",
                "Must clearly show individual subject marks and aggregate percentage. If appearing for improvement, carry both marksheets."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Entrance Examination Admit Card & Rank Card",
                "JEE Main, JEE Advanced, or AP EAPCET Admit Card and official Rank Score Card showing CRL and Category Rank.",
                true,
                "Original PDF download",
                "Download directly from NTA (jeemain.nta.ac.in), JEE Adv (jeeadv.ac.in), or APSCHE (cets.apsche.ap.gov.in)."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Medical Fitness Certificate (Mandatory Annexure-7 Format)",
                "Official JoSAA Annexure-7 medical certificate verifying physical fitness, visual acuity, color perception, and general health.",
                true,
                "Original signed & stamped by MBBS Doctor",
                "Download official Annexure-7 template from josaa.nic.in. Must be certified by a Registered Medical Practitioner (holding MBBS degree or higher) with registration number and stamp."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Valid Government Photo Identity Card",
                "Aadhaar Card, Passport, Voter ID, or School/College Photo ID proving candidate identity.",
                true,
                "Original color PDF scan",
                "Download mask-free e-Aadhaar from uidai.gov.in."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Provisional Seat Allotment Letter & SAF Fee Payment Receipt",
                "Official allotment letter generated by JoSAA / CSAB / AP EAPCET portal after seat allocation, together with payment acknowledgement receipt.",
                true,
                "Portal generated PDF",
                "Download from candidate dashboard immediately after paying the Seat Acceptance Fee."
        ));

        // Category-Specific Documents
        if ("OBC_NCL".equalsIgnoreCase(category)) {
            docs.add(new ServiceRequiredDocumentDto(
                    "OBC-NCL Certificate (STRICT: Issued ON or AFTER April 1)",
                    "Mandatory Central Government format OBC Non-Creamy Layer certificate. Crucial Rule: Must be issued ON or AFTER April 1 of the admission year. If issued prior, category status is forfeited and candidate is permanently converted to General (Open) category.",
                    true,
                    "Central DoPT Format PDF Scan",
                    "Obtain from Tahsildar / Revenue Divisional Officer (RDO) / Sub-Divisional Magistrate (SDM). Must cite Central OBC List notifications."
            ));
        } else if ("GEN_EWS".equalsIgnoreCase(category)) {
            docs.add(new ServiceRequiredDocumentDto(
                    "General-EWS Income & Asset Certificate (STRICT: Issued ON or AFTER April 1)",
                    "Statutory EWS certificate issued by competent revenue authority. Crucial Rule: Must be issued ON or AFTER April 1 of the admission year valid for the current financial year. Older certificates are strictly rejected.",
                    true,
                    "Central DoPT Format PDF Scan",
                    "Issued by Tehsildar / MRO / SDM. Must confirm gross family annual income below ₹8 Lakhs and statutory property thresholds."
            ));
        } else if ("SC".equalsIgnoreCase(category) || "ST".equalsIgnoreCase(category)) {
            docs.add(new ServiceRequiredDocumentDto(
                    "SC / ST Community & Tribe Certificate",
                    "Permanent caste/community certificate issued in the prescribed Central format by authorized revenue officer (Tahsildar / MRO).",
                    true,
                    "Original PDF scan",
                    "Issued by revenue department / MeeSeva with verifiable digital certificate number and government seal."
            ));
        } else if ("PWD".equalsIgnoreCase(category)) {
            docs.add(new ServiceRequiredDocumentDto(
                    "PwD Benchmark Disability Certificate / UDID Card",
                    "Disability certificate issued by authorized government Medical Board confirming minimum 40% benchmark disability.",
                    true,
                    "Unique Disability ID (UDID) Card or Form V/VI/VII",
                    "Swavlamban UDID card or certificate from District Chief Medical Officer (CMO)."
            ));
        }

        docs.add(new ServiceRequiredDocumentDto(
                "State Code of Eligibility Proof / Domicile Certificate",
                "Class 12 examination passing certificate or intermediate college study certificate proving the state where Class 12 was completed (for 50% Home State NIT quota and 85% AP local quota).",
                true,
                "College Study / Bonafide / Passing Certificate",
                "Class 12 school leaving / study certificate from intermediate college or residence certificate."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Crossed Bank Cancelled Cheque / Passbook Photocopy",
                "Cancelled bank cheque or front page of passbook showing Account Holder Name, Account Number, and IFSC code (student's or parent's account).",
                true,
                "Original scan of cancelled cheque",
                "Mandatory for verification in case of seat cancellation, round withdrawal, or refund of excess seat acceptance fee."
        ));

        docs.add(new ServiceRequiredDocumentDto(
                "Transfer Certificate (TC), Conduct Certificate & Migration Certificate",
                "Certificates issued by junior college / intermediate board required at the time of physical institutional admission.",
                false,
                "Original physical certificates",
                "Obtain from principal's office upon completion of Class 12 / Intermediate."
        ));

        result.setDocuments(docs);

        // 8. Critical Tips & System Error Flags
        List<String> tips = new ArrayList<>();
        tips.add("MANDATORY REPORTING SLA: If a seat is allotted in ANY JoSAA round, you MUST complete online reporting (payment of SAF + uploading documents + selecting Freeze/Float/Slide). Failing to do so within the specified window automatically cancels your seat and PERMANENTLY DEBARS you from all subsequent JoSAA rounds.");
        tips.add("FREEZE vs FLOAT vs SLIDE: Choose 'Float' if you are happy with the allotted college but wish to upgrade to a higher-preference branch at ANY college. Choose 'Slide' if you wish to upgrade to a higher branch ONLY WITHIN THE SAME college. Choose 'Freeze' only if you are 100% satisfied and want no further changes.");
        tips.add("OBC-NCL & EWS STRICT FINANCIAL YEAR RULE: Central counselling authorities mandate that OBC-NCL and EWS certificates MUST be issued ON or AFTER April 1 of the current admission year. Certificates dated on or before March 31 are strictly invalid for central quotas and will result in automatic category cancellation.");
        tips.add("24-HOUR QUERY RESOLUTION WINDOW: During each round's online reporting period, Virtual Reporting Centre (VRC) officers inspect your uploaded documents. Check your candidate dashboard twice daily. If a query is raised, you must respond with clarification within 24 hours to prevent seat forfeiture.");
        tips.add("STRATEGIC CHOICE ORDERING: Choice filling is unlimited. Place your dream branches at the top, realistic choices in the middle, and solid backups below. Never arrange choices based on predicted cutoffs; always arrange based on your true personal preference.");
        tips.add("FEMALE-ONLY SUPERNUMERARY POOL: 20% supernumerary seats are reserved exclusively for female candidates in IITs and NITs. If eligible, your rank is automatically considered across both gender-neutral and female-only pools, significantly enhancing your admission chances.");
        tips.add("CSAB SPECIAL ROUND VACANT SEAT SPOT: If you do not secure a preferred seat in JoSAA Round 6, you can participate in CSAB Special Rounds (csab.nic.in). CSAB evaluates purely on JEE Main CRL Rank and fills all residual vacancies across 31 NITs, 26 IIITs, and 38 GFTIs.");

        result.setCriticalTips(tips);

        return result;
    }
}
