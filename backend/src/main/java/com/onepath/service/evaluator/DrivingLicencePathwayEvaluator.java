package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Intelligent Pathway Evaluator for Driving Licence & Vehicle Registration
 * based on Ministry of Road Transport & Highways (MoRTH), Central Motor Vehicles Rules (CMVR),
 * and official Sarathi & Vahan Parivahan guidelines.
 */
@Component
public class DrivingLicencePathwayEvaluator implements ServicePathwayEvaluator {

    public static final String DL_SERVICE_SLUG = "driving-licence-vehicle-registration";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return DL_SERVICE_SLUG.equalsIgnoreCase(s)
                || "driving-licence".equalsIgnoreCase(s)
                || "driving-license".equalsIgnoreCase(s)
                || "vehicle-registration".equalsIgnoreCase(s)
                || "driving-licence-and-vehicle-registration".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(DL_SERVICE_SLUG);
        dto.setServiceName(service != null ? service.getName() : "Driving Licence & Vehicle Registration");
        dto.setTitle("Driving Licence & Vehicle Registration Navigator");
        dto.setDescription("Answer a few quick questions to identify your exact statutory pathway, verify age eligibility, determine official Parivahan fees, and generate your tailored document checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Service Needed
        questions.add(new QuestionnaireQuestionDto(
                "serviceNeed",
                "What service do you require?",
                "Select whether you are applying for a driving licence, renewing an existing licence, or completing vehicle registration.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("LEARNER_LICENCE", "New Learner's Licence (LL)", "Mandatory first step for new drivers or adding a vehicle class. Valid for 6 months.", "First-Time Driver"),
                        new QuestionOptionDto("PERMANENT_DL", "Permanent Driving Licence (DL)", "For applicants who have held a valid Learner's Licence for at least 30 days. Requires driving test.", "RTO Driving Test"),
                        new QuestionOptionDto("DL_RENEWAL", "Driving Licence Renewal", "For licences expiring within 1 year or recently expired. Avoid penalties by renewing within grace period.", "DL Renewal"),
                        new QuestionOptionDto("DL_DUPLICATE", "Duplicate Driving Licence", "For lost, stolen, mutilated, or torn licence booklets/smart cards. Form LLD applies.", "Duplicate DL"),
                        new QuestionOptionDto("DL_ADDRESS_CHANGE", "Change of Address or Details in DL", "Update residential address, legal name, or biometric records on your existing licence.", "Details Update"),
                        new QuestionOptionDto("NEW_VEHICLE_RC", "New Vehicle Registration (RC)", "Registration of newly purchased two-wheeler, car (LMV), or commercial vehicle through Vahan/dealership.", "New Vehicle"),
                        new QuestionOptionDto("RC_SERVICES", "Vehicle RC Renewal, Transfer, or Duplicate RC", "15-year fitness re-registration, ownership transfer (sale/purchase), or duplicate RC smart card.", "RC Lifecycle")
                )
        ));

        // Q2: Vehicle Category (Applies to DL and new vehicle RC)
        questions.add(new QuestionnaireQuestionDto(
                "vehicleCategory",
                "What vehicle category is involved?",
                "Determines statutory age requirements, competence test tracks, and official fee schedule.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("TWO_WHEELER_GEAR", "Motorcycle with Gear (MCWG) / Geared Two-Wheeler", "Standard motorcycles, geared scooters, and sport bikes. Minimum age: 18 years.", "MCWG (18+ Yrs)"),
                        new QuestionOptionDto("FOUR_WHEELER_LMV", "Light Motor Vehicle (LMV) - Car, Jeep, Small Van", "Personal cars, hatchbacks, sedans, SUVs. Minimum age: 18 years.", "LMV Car (18+ Yrs)"),
                        new QuestionOptionDto("BOTH_TWO_FOUR", "Both Two-Wheeler & Four-Wheeler (MCWG + LMV)", "Combined dual-class licence covering both geared motorcycle and personal car.", "Most Popular Combo"),
                        new QuestionOptionDto("TWO_WHEELER_NO_GEAR", "Motorcycle without Gear (MCWOG - Up to 50cc / Moped)", "Eligible from age 16 with statutory parental consent under Section 4 of MVA.", "Junior Eligible (16+)"),
                        new QuestionOptionDto("TRANSPORT_COMMERCIAL", "Transport / Commercial Vehicle (Medium / Heavy Goods or Passenger)", "Taxis, delivery trucks, buses, commercial vehicles. Requires minimum age 20 and prior 1-year LMV holding.", "Commercial / Heavy")
                )
        ));

        // Q3: Age Category
        questions.add(new QuestionnaireQuestionDto(
                "applicantAge",
                "What is your age category?",
                "Mandatory under Section 4 of the Motor Vehicles Act 1988 for determining driving eligibility and medical certification.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("UNDER_16", "Below 16 Years", "Ineligible for any motor vehicle driving licence under Indian law."),
                        new QuestionOptionDto("AGE_16_TO_18", "16 to 17 Years", "Statutorily eligible ONLY for Motorcycle without gear up to 50cc (Form 2 parental consent mandatory)."),
                        new QuestionOptionDto("AGE_18_TO_39", "18 to 39 Years", "Fully eligible for MCWG two-wheelers and LMV cars with standard self-declaration Form 1."),
                        new QuestionOptionDto("AGE_40_PLUS", "40 Years and Above", "Fully eligible. Mandatory Form 1A Medical Certificate signed by a registered medical practitioner (RMP).")
                )
        ));

        // Q4: State / Union Territory
        questions.add(new QuestionnaireQuestionDto(
                "stateCode",
                "Which State or Union Territory are you applying in?",
                "Sarathi Parivahan integrates with state transport departments. Most states support Aadhaar contactless faceless testing.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("ANDHRA_PRADESH", "Andhra Pradesh (AP)", "RTO automated driving test tracks; Sarathi Parivahan online processing."),
                        new QuestionOptionDto("TELANGANA", "Telangana (TS)", "RTA m-Wallet and integrated online transport portal."),
                        new QuestionOptionDto("TAMIL_NADU", "Tamil Nadu (TN)", "Online contactless LL and automated driving competence tracks."),
                        new QuestionOptionDto("KARNATAKA", "Karnataka (KA)", "Bangalore & regional RTO automated electronic test tracks."),
                        new QuestionOptionDto("MAHARASHTRA", "Maharashtra (MH)", "Faceless online LL test enabled across all RTOs."),
                        new QuestionOptionDto("DELHI_NCR", "Delhi (NCT) / NCR", "100% faceless contactless transport services with home dispatch."),
                        new QuestionOptionDto("OTHER_STATE", "Other State / Union Territory", "Central MoRTH Sarathi / Vahan national portal guidelines apply.")
                )
        ));

        // Q5: Learner's Licence Status (Conditional on PERMANENT_DL)
        questions.add(new QuestionnaireQuestionDto(
                "learnerLicenceStatus",
                "What is the status of your Learner's Licence (LL)?",
                "Mandatory prerequisite: Rule 15 of CMVR requires holding an active LL for at least 30 days before taking the permanent driving test.",
                "serviceNeed=PERMANENT_DL",
                Arrays.asList(
                        new QuestionOptionDto("LL_ACTIVE_OVER_30_DAYS", "Active LL held for 30+ days (within 6 months validity)", "Eligible to book the RTO practical driving test immediately.", "Eligible for DL Test"),
                        new QuestionOptionDto("LL_HELD_UNDER_30_DAYS", "Active LL, but held for less than 30 days", "Mandatory 30-day practice waiting period has not elapsed yet. You must wait before testing."),
                        new QuestionOptionDto("LL_EXPIRED", "Learner's Licence has expired (more than 6 months old)", "Expired LL cannot be converted to a permanent DL. Must apply for a fresh Learner's Licence first."),
                        new QuestionOptionDto("NO_LL", "I do not have a Learner's Licence yet", "Must apply for a Learner's Licence first. Direct permanent DL without LL is not permitted.")
                )
        ));

        // Q6: RC Lifecycle Sub-Service (Conditional on RC_SERVICES)
        questions.add(new QuestionnaireQuestionDto(
                "rcSubService",
                "Which vehicle registration lifecycle service do you need?",
                "Select the specific vehicle registration procedure under Vahan.",
                "serviceNeed=RC_SERVICES",
                Arrays.asList(
                        new QuestionOptionDto("RC_RENEWAL_15_YEAR", "Renewal of Registration Certificate (15-Year Fitness)", "Mandatory re-registration and fitness inspection after 15 years from purchase date.", "15-Yr Fitness"),
                        new QuestionOptionDto("RC_TRANSFER_OWNERSHIP", "Transfer of Vehicle Ownership (Sale / Purchase)", "Transfer ownership from seller to buyer using Form 29 (Notice) and Form 30 (Application).", "Ownership Transfer"),
                        new QuestionOptionDto("RC_DUPLICATE", "Duplicate Registration Certificate (RC)", "For lost, stolen, or damaged RC smart cards. Form 26 with police report applies.", "Duplicate RC"),
                        new QuestionOptionDto("RC_ADDRESS_CHANGE", "Change of Residential Address in RC", "Update residential address in vehicle RC within 30 days of moving using Form 33.", "RC Address Change")
                )
        ));

        // Q7: Aadhaar Authentication
        questions.add(new QuestionnaireQuestionDto(
                "aadhaarAuthAvailable",
                "Do you have an Aadhaar-linked mobile number for online OTP verification?",
                "Aadhaar authentication enables contactless/faceless online applications and home LL testing without visiting the RTO.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("AADHAAR_OTP_AVAILABLE", "Yes, Aadhaar OTP Available (Faceless / Contactless)", "Enables 100% online application and contactless home LL test without physical RTO visit.", "Faceless Contactless"),
                        new QuestionOptionDto("NON_AADHAAR_PHYSICAL", "No Aadhaar OTP / Prefer Physical RTO Visit", "Requires booking an RTO slot for physical document verification and computerized LL test at RTO.")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        String serviceNeed = answers.getOrDefault("serviceNeed", "LEARNER_LICENCE");
        String vehicleCategory = answers.getOrDefault("vehicleCategory", "TWO_WHEELER_GEAR");
        String applicantAge = answers.getOrDefault("applicantAge", "AGE_18_TO_39");
        String stateCode = answers.getOrDefault("stateCode", "ANDHRA_PRADESH");
        String llStatus = answers.getOrDefault("learnerLicenceStatus", "LL_ACTIVE_OVER_30_DAYS");
        String rcSubService = answers.getOrDefault("rcSubService", "RC_TRANSFER_OWNERSHIP");
        String aadhaarAuth = answers.getOrDefault("aadhaarAuthAvailable", "AADHAAR_OTP_AVAILABLE");

        boolean isAadhaarFaceless = "AADHAAR_OTP_AVAILABLE".equalsIgnoreCase(aadhaarAuth);
        boolean isBothClasses = "BOTH_TWO_FOUR".equalsIgnoreCase(vehicleCategory);
        boolean isLmvCar = "FOUR_WHEELER_LMV".equalsIgnoreCase(vehicleCategory);
        boolean isTransport = "TRANSPORT_COMMERCIAL".equalsIgnoreCase(vehicleCategory);
        boolean isMcwog = "TWO_WHEELER_NO_GEAR".equalsIgnoreCase(vehicleCategory);
        boolean isJuniorAge = "AGE_16_TO_18".equalsIgnoreCase(applicantAge);
        boolean isUnder16 = "UNDER_16".equalsIgnoreCase(applicantAge);
        boolean isAge40Plus = "AGE_40_PLUS".equalsIgnoreCase(applicantAge);

        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();
        result.setOfficialPortalUrl("https://sarathi.parivahan.gov.in");

        // ----------------------------------------------------
        // 1. STATUTORY AGE & ELIGIBILITY VALIDATION
        // ----------------------------------------------------
        if (isUnder16) {
            result.setPathwayCode("INELIGIBLE_UNDER_AGE");
            result.setPathwayTitle("Ineligible for Driving Licence — Under Minimum Statutory Age");
            result.setBadgeText("Statutory Ineligibility");
            result.setMatchReason("Under Section 4 of the Motor Vehicles Act 1988, no person below the age of 16 years is legally permitted to drive any motor vehicle in a public place. You must wait until reaching the age of 16 for a moped (MCWOG up to 50cc) or 18 for geared two-wheelers and cars.");
            result.setValidity("Not Applicable (Underage)");
            result.setCalculatedFee("₹0");
            result.setFeeBreakdown("No fee applicable — application cannot be processed until reaching statutory age.");
            result.setEstimatedTimeline("Ineligible");
            result.setPoliceVerificationMode("Not Applicable");
            result.setPoliceVerificationDetails("Application cannot be submitted under Motor Vehicles Act regulations.");

            List<ServiceStepDto> steps = new ArrayList<>();
            steps.add(new ServiceStepDto(1L, 1, "Age Requirement Advisory", "Wait until your 16th birthday to apply for Motorcycle without gear (up to 50cc), or your 18th birthday for standard geared motorcycles and cars.", "Statutory restriction", "Section 4 of Motor Vehicles Act 1988"));
            result.setSteps(steps);

            List<String> tips = new ArrayList<>();
            tips.add("Legal Warning: Driving a motor vehicle under the statutory age is a punishable offence with penalties up to ₹25,000 and imprisonment for parents/vehicle owners under Section 199A of the amended Motor Vehicles Act.");
            result.setCriticalTips(tips);
            return result;
        }

        if (isJuniorAge && (isLmvCar || isBothClasses || isTransport)) {
            result.setPathwayCode("INELIGIBLE_JUNIOR_CLASS_MISMATCH");
            result.setPathwayTitle("Age Restriction — 16–17 Year Olds Eligible Only for 50cc MCWOG");
            result.setBadgeText("Class Ineligibility");
            result.setMatchReason("Applicants aged 16–17 years are statutorily restricted under Section 4(1) of the Motor Vehicles Act to 'Motorcycle without Gear (up to 50cc)' with parental consent. You cannot apply for a Light Motor Vehicle (Car) or geared motorcycle until completing 18 years.");
            result.setValidity("Restricted to MCWOG (50cc)");
            result.setCalculatedFee("₹200 (if switching to MCWOG)");
            result.setFeeBreakdown("Statutory restriction: Switch your vehicle category to Motorcycle without gear (up to 50cc) to proceed.");
            result.setEstimatedTimeline("Eligible for LMV upon turning 18");
            result.setPoliceVerificationMode("Parental Consent Mandatory");

            List<ServiceStepDto> steps = new ArrayList<>();
            steps.add(new ServiceStepDto(1L, 1, "Switch to Permitted Junior Class", "Select 'Motorcycle without Gear (up to 50cc)' on Sarathi Parivahan, or wait until your 18th birthday to apply for a Car/LMV.", "Immediate", "Parental consent Form 2 is mandatory"));
            result.setSteps(steps);

            List<String> tips = new ArrayList<>();
            tips.add("Junior Category Rule: 16–17 year old applicants require statutory parent or guardian consent executed via Declaration in Form 2.");
            result.setCriticalTips(tips);
            return result;
        }

        // ----------------------------------------------------
        // 2. PATHWAY IDENTIFICATION & EVALUATION
        // ----------------------------------------------------
        if ("LEARNER_LICENCE".equalsIgnoreCase(serviceNeed)) {
            // Learner's Licence Pathways
            if (isJuniorAge && isMcwog) {
                result.setPathwayCode("LL_JUNIOR_16_18");
                result.setPathwayTitle("Junior Learner's Licence (Age 16–18: MCWOG up to 50cc)");
                result.setBadgeText("Junior Learner Stage");
                result.setMatchReason("Based on your answers: You are 16–17 years old applying for an entry-level Motorcycle without gear (up to 50cc). Parental/guardian consent under Form 2 is mandatory.");
            } else if (isBothClasses) {
                result.setPathwayCode("LL_COMBO_TWO_FOUR");
                result.setPathwayTitle("Learner's Licence — Two-Wheeler & Four-Wheeler (MCWG + LMV Combo)");
                result.setBadgeText("Dual-Class Learner Stage");
                result.setMatchReason("Based on your answers: You are an adult citizen applying for a simultaneous combined Learner's Licence for both geared motorcycle (MCWG) and car (LMV).");
            } else if (isLmvCar) {
                result.setPathwayCode("LL_FOUR_WHEELER_LMV");
                result.setPathwayTitle("Learner's Licence — Light Motor Vehicle (LMV / Car)");
                result.setBadgeText("LMV Car Learner Stage");
                result.setMatchReason("Based on your answers: You are applying for a first-time Learner's Licence for Light Motor Vehicles (personal cars, SUVs). Valid for 6 months across India.");
            } else {
                result.setPathwayCode("LL_TWO_WHEELER");
                result.setPathwayTitle("Learner's Licence — Motorcycle with Gear (MCWG / Two-Wheeler)");
                result.setBadgeText("Two-Wheeler Learner Stage");
                result.setMatchReason("Based on your answers: You are applying for a Learner's Licence for geared motorcycles, scooters, and bikes. Valid for 6 months across all Indian states.");
            }

            result.setValidity("6 Months from date of issue (Mandatory 30-day practice period required before permanent DL test)");

            int classFee = isBothClasses ? 300 : 150; // ₹150 per class (Rule 32 CMVR)
            int testFee = 50; // ₹50 one-time test fee
            int total = classFee + testFee;
            result.setCalculatedFee("₹" + total);
            result.setFeeBreakdown("Learner's Licence: ₹" + classFee + (isBothClasses ? " (2 vehicle classes × ₹150)" : " (1 vehicle class)") + " + Online LL Test Fee: ₹50 = ₹" + total + " total");
            result.setEstimatedTimeline(isAadhaarFaceless ? "Instant / Same Day (Online e-KYC Test from home)" : "1 – 3 Working Days (post-RTO computerized test)");

            result.setPoliceVerificationMode(isAadhaarFaceless ? "Faceless / Contactless Online Test (Aadhaar e-KYC)" : "Physical RTO Visit Required for LL Test");
            result.setPoliceVerificationDetails(isAadhaarFaceless ?
                    "Contactless Faceless Scheme: Using Aadhaar OTP, your identity is verified instantly. You take the 20-question online road safety test from home on laptop/mobile with front camera proctoring. Form 3 e-Learner's Licence is generated immediately upon scoring 60%+." :
                    "Physical RTO Test: Visit your selected RTO with original age & address documents. Take the computerized touch-screen LL test on road signs and traffic safety. Form 3 is issued upon passing.");

        } else if ("PERMANENT_DL".equalsIgnoreCase(serviceNeed)) {
            // Permanent Driving Licence Pathways
            if ("LL_HELD_UNDER_30_DAYS".equalsIgnoreCase(llStatus)) {
                result.setPathwayCode("DL_PERMANENT_BLOCKED_WAITING_PERIOD");
                result.setPathwayTitle("Permanent DL — Mandatory 30-Day Practice Period in Progress");
                result.setBadgeText("30-Day Practice Period");
                result.setMatchReason("Under Rule 15 of Central Motor Vehicles Rules, an applicant MUST hold an active Learner's Licence for a minimum of 30 days before booking or appearing for the permanent driving competence test. You must complete your 30-day practice period before taking the practical test.");
                result.setValidity("Valid LL active (test unlocks after 30 days)");
                result.setCalculatedFee("₹" + (isBothClasses ? "1,000" : "700") + " (upon reaching 30 days)");
                result.setFeeBreakdown("Permanent DL Competence Test: ₹" + (isBothClasses ? "600 (2 × ₹300)" : "300") + " + DL Issue Fee: ₹200 + Form 7 Smart Card Fee: ₹200");
                result.setEstimatedTimeline("Book test slot as soon as 30 days from LL issue date are completed");
                result.setPoliceVerificationMode("Practical Driving Competence Test (at RTO ADTT)");
                result.setPoliceVerificationDetails("Mandatory practical driving test on Automated Driving Test Track (ADTT) — Figure 8, Parallel Parking, and Gradient Hill test.");

            } else if ("LL_EXPIRED".equalsIgnoreCase(llStatus) || "NO_LL".equalsIgnoreCase(llStatus)) {
                result.setPathwayCode("DL_PERMANENT_BLOCKED_NO_LL");
                result.setPathwayTitle("Prerequisite Missing — Valid Learner's Licence Required First");
                result.setBadgeText("Prerequisite Required");
                result.setMatchReason("You cannot apply directly for a Permanent Driving Licence. Under Section 9 of the Motor Vehicles Act, holding a valid, non-expired Learner's Licence for at least 30 days is a mandatory statutory prerequisite.");
                result.setValidity("Must apply for Learner's Licence first");
                result.setCalculatedFee("₹" + (isBothClasses ? "350" : "200") + " for Learner's Licence");
                result.setFeeBreakdown("Apply for Learner's Licence first (₹150/class + ₹50 test), complete 30 days practice, then apply for Permanent DL.");
                result.setEstimatedTimeline("Step 1: LL (Same Day) → Step 2: 30 Days Practice → Step 3: Permanent DL (7–15 Days)");
                result.setPoliceVerificationMode("Online LL Test First");
                result.setPoliceVerificationDetails("Must obtain Form 3 Learner's Licence before permanent driving test can be scheduled.");

            } else {
                // Eligible for Permanent DL Test
                result.setPathwayCode(isBothClasses ? "DL_PERMANENT_COMBO" : "DL_PERMANENT_SINGLE");
                result.setPathwayTitle("Permanent Driving Licence — Practical Test & Smart Card Issue (" + (isBothClasses ? "MCWG + LMV" : (isLmvCar ? "LMV Car" : "Two-Wheeler")) + ")");
                result.setBadgeText("Permanent Driving Licence");
                result.setMatchReason("Based on your answers: You hold an active Learner's Licence for 30+ days. You are fully eligible to book your practical driving competence test at the RTO/Automated Driving Test Track (ADTT).");
                result.setValidity("20 Years or until age 40 (whichever is earlier for non-transport); 10 Years for age 40–55; 5 Years for age 55+");

                int testFees = isBothClasses ? 600 : 300; // ₹300 per class (Rule 32 CMVR)
                int dlIssueFee = 200;
                int smartCardFee = 200;
                int total = testFees + dlIssueFee + smartCardFee;
                result.setCalculatedFee("₹" + String.format("%,d", total));
                result.setFeeBreakdown("Driving Competence Test: ₹" + testFees + (isBothClasses ? " (2 classes × ₹300)" : " (1 class)") + " + DL Issue: ₹200 + Form 7 Polycarbonate Smart Card: ₹200 = ₹" + String.format("%,d", total) + " total");
                result.setEstimatedTimeline("7 – 15 Working Days (Smart card dispatch by Speed Post after passing RTO practical test)");
                result.setPoliceVerificationMode("RTO Practical Driving Competence Test");
                result.setPoliceVerificationDetails("Practical driving test conducted at RTO Automated Driving Test Track (ADTT). Sensors evaluate figure-of-8, parallel parking, gradient reverse stop/start, and emergency braking without human bias.");
            }

        } else if ("DL_RENEWAL".equalsIgnoreCase(serviceNeed)) {
            // DL Renewal
            result.setPathwayCode("DL_RENEWAL_STANDARD");
            result.setPathwayTitle("Driving Licence Renewal (Validity Extension & Smart Card Re-issue)");
            result.setBadgeText("DL Renewal");
            result.setMatchReason("Based on your answers: You are renewing an existing driving licence expiring within 1 year or within the statutory grace period. No driving re-test is required if renewed within 1 year of expiry.");
            result.setValidity("10 Years (for age under 50); 5 Years (for age 50–55); 3–5 Years (for senior citizens)");
            result.setCalculatedFee("₹400");
            result.setFeeBreakdown("DL Renewal Fee: ₹200 + Form 7 Polycarbonate Smart Card Fee: ₹200 = ₹400 total (Note: Delay beyond 1 year incurs additional late fee of ₹300 + ₹1,000/year of delay)");
            result.setEstimatedTimeline("7 – 10 Working Days (Speed Post delivery)");
            result.setPoliceVerificationMode("Faceless / Biometric Verification");
            result.setPoliceVerificationDetails("No driving competence test required if renewed within 1 year of expiration date. Documents verified electronically via Sarathi Parivahan.");

        } else if ("DL_DUPLICATE".equalsIgnoreCase(serviceNeed)) {
            // Duplicate DL
            result.setPathwayCode("DL_DUPLICATE_ISSUE");
            result.setPathwayTitle("Duplicate Driving Licence Issue (Lost, Stolen, Torn, or Mutilated DL)");
            result.setBadgeText("Duplicate DL");
            result.setMatchReason("Based on your answers: You are applying for a duplicate driving licence due to loss, theft, damage, or tearing of your physical licence card. Form LLD applies.");
            result.setValidity("Identical to original licence validity period");
            result.setCalculatedFee("₹400");
            result.setFeeBreakdown("Issue of Duplicate DL: ₹200 + Form 7 Smart Card Fee: ₹200 = ₹400 total");
            result.setEstimatedTimeline("7 – 12 Working Days (Speed Post delivery)");
            result.setPoliceVerificationMode("Electronic Record Verification");
            result.setPoliceVerificationDetails("Licensing Authority verifies original DL number and biometric records against the National Register (Sarathi) database.");

        } else if ("DL_ADDRESS_CHANGE".equalsIgnoreCase(serviceNeed)) {
            // Change of Address in DL
            result.setPathwayCode("DL_ADDRESS_PARTICULARS_CHANGE");
            result.setPathwayTitle("Change of Residential Address & Particulars in Driving Licence");
            result.setBadgeText("Particulars Update");
            result.setMatchReason("Based on your answers: You are updating your present residential address or personal particulars on your driving licence. Mandatory within 30 days of changing residence under Section 49 of MVA.");
            result.setValidity("Maintains existing validity period");
            result.setCalculatedFee("₹400");
            result.setFeeBreakdown("Change of Particulars / Address Fee: ₹200 + Form 7 Smart Card Fee: ₹200 = ₹400 total");
            result.setEstimatedTimeline("7 – 10 Working Days");
            result.setPoliceVerificationMode("Address Proof Verification");
            result.setPoliceVerificationDetails("RTO verifies address proof (Aadhaar e-KYC or utility/residence documents) against new jurisdiction.");

        } else if ("NEW_VEHICLE_RC".equalsIgnoreCase(serviceNeed)) {
            // New Vehicle Registration
            result.setPathwayCode("RC_NEW_VEHICLE");
            result.setPathwayTitle("New Motor Vehicle Registration (RC & High Security Plates - HSRP)");
            result.setBadgeText("New Vehicle Registration");
            result.setMatchReason("Based on your answers: You are registering a newly purchased motor vehicle. Permanent Registration Certificate (RC) and High Security Registration Plates (HSRP) are processed through Vahan.");
            result.setValidity("15 Years from registration date (Private / Non-Transport vehicles)");

            int regFee = isBothClasses || isLmvCar ? 600 : (isTransport ? 1500 : 300);
            result.setCalculatedFee("₹" + String.format("%,d", regFee) + " (plus State Road Tax)");
            result.setFeeBreakdown("Central Registration Fee (Rule 81 CMVR): ₹" + regFee + " (" + (regFee == 300 ? "Two-Wheeler" : (regFee == 600 ? "Light Motor Vehicle Car" : "Medium/Heavy Commercial")) + ") + State One-Time Road Tax (calculated on vehicle ex-showroom invoice) + HSRP plate charges.");
            result.setEstimatedTimeline("7 – 14 Working Days");
            result.setPoliceVerificationMode("Physical Vehicle Inspection by MVI");
            result.setPoliceVerificationDetails("Motor Vehicle Inspector (MVI) physically inspects vehicle at RTO/authorized dealership track to match engine number and chassis stencil against Form 21 & Form 22.");
            result.setOfficialPortalUrl("https://vahan.parivahan.gov.in");

        } else {
            // RC Lifecycle Services (Renewal, Transfer, Duplicate)
            result.setOfficialPortalUrl("https://vahan.parivahan.gov.in");
            if ("RC_RENEWAL_15_YEAR".equalsIgnoreCase(rcSubService)) {
                result.setPathwayCode("RC_15_YEAR_RENEWAL");
                result.setPathwayTitle("15-Year Vehicle Fitness & Certificate of Registration (RC) Renewal");
                result.setBadgeText("15-Yr RC Renewal");
                result.setMatchReason("Based on your answers: Your vehicle has completed 15 years from its original registration date. Under Section 41(7) of MVA, private vehicles must undergo physical fitness testing to obtain RC renewal for 5 years.");
                result.setValidity("5 Years (Extended after physical roadworthiness test)");
                int fee = isLmvCar ? 5000 : 1000;
                result.setCalculatedFee("₹" + String.format("%,d", fee) + " (Rule 81 CMVR)");
                result.setFeeBreakdown("15-Year RC Renewal Fee: ₹" + fee + " (" + (isLmvCar ? "Four-Wheeler Car: ₹5,000" : "Two-Wheeler: ₹1,000") + ") + Fitness Inspection Fee: ₹400–₹600 + Green Tax (where applicable by state)");
                result.setEstimatedTimeline("7 – 15 Working Days");
                result.setPoliceVerificationMode("RTO Physical Fitness Inspection");
                result.setPoliceVerificationDetails("RTO Automated Testing Station / MVI inspection of vehicle brakes, steering, suspension, emissions (valid PUC), and headlight alignment.");

            } else if ("RC_TRANSFER_OWNERSHIP".equalsIgnoreCase(rcSubService)) {
                result.setPathwayCode("RC_TRANSFER_OWNERSHIP");
                result.setPathwayTitle("Vehicle Ownership Transfer (Sale / Purchase / Succession)");
                result.setBadgeText("Ownership Transfer");
                result.setMatchReason("Based on your answers: You are transferring vehicle ownership to a new buyer. Requires Form 29 (Notice of Transfer) and Form 30 (Application for Transfer).");
                result.setValidity("Preserves remaining validity of existing RC");
                int fee = isLmvCar ? 300 : 150;
                result.setCalculatedFee("₹" + fee);
                result.setFeeBreakdown("Ownership Transfer Fee (50% of registration fee): ₹" + fee + " (" + (isLmvCar ? "Car: ₹300" : "Two-Wheeler: ₹150") + ") + Smart Card Fee: ₹200 = ₹" + (fee + 200) + " total");
                result.setEstimatedTimeline("7 – 14 Working Days");
                result.setPoliceVerificationMode("Buyer & Seller Identity Verification");
                result.setPoliceVerificationDetails("Both seller and buyer execute transfer forms with Aadhaar OTP authentication or physical signature match on Vahan.");

            } else if ("RC_DUPLICATE".equalsIgnoreCase(rcSubService)) {
                result.setPathwayCode("RC_DUPLICATE_ISSUE");
                result.setPathwayTitle("Duplicate Registration Certificate (RC) Issue");
                result.setBadgeText("Duplicate RC");
                result.setMatchReason("Based on your answers: You are applying for a duplicate RC smart card due to loss, theft, damage, or destruction of the original certificate. Form 26 applies.");
                result.setValidity("Maintains original vehicle registration expiry");
                int fee = isLmvCar ? 300 : 150;
                result.setCalculatedFee("₹" + (fee + 200));
                result.setFeeBreakdown("Duplicate RC Fee: ₹" + fee + " + Smart Card Issue: ₹200 = ₹" + (fee + 200) + " total");
                result.setEstimatedTimeline("7 – 10 Working Days");
                result.setPoliceVerificationMode("Police Lost Report Verification");
                result.setPoliceVerificationDetails("Requires police Non-Cognizable Report (NCR) / Missing Article Report and chassis pencil print.");

            } else {
                result.setPathwayCode("RC_ADDRESS_CHANGE");
                result.setPathwayTitle("Change of Residential Address in Vehicle Registration (RC)");
                result.setBadgeText("RC Address Change");
                result.setMatchReason("Based on your answers: You are updating your address in vehicle records. Must be filed within 30 days of moving under Section 49 of MVA.");
                result.setValidity("Preserves existing RC validity");
                int fee = isLmvCar ? 300 : 150;
                result.setCalculatedFee("₹" + (fee + 200));
                result.setFeeBreakdown("Address Modification Fee: ₹" + fee + " + Updated Smart Card: ₹200 = ₹" + (fee + 200) + " total");
                result.setEstimatedTimeline("7 – 10 Working Days");
                result.setPoliceVerificationMode("Residential Address Verification");
                result.setPoliceVerificationDetails("Verified through Aadhaar e-KYC or new residential proof under local RTO jurisdiction.");
            }
        }

        // ----------------------------------------------------
        // 3. REQUIRED DOCUMENTS CHECKLIST
        // ----------------------------------------------------
        List<ServiceRequiredDocumentDto> docs = new ArrayList<>();

        if (serviceNeed.startsWith("RC") || "NEW_VEHICLE_RC".equalsIgnoreCase(serviceNeed)) {
            // Vehicle Registration Documents
            if ("NEW_VEHICLE_RC".equalsIgnoreCase(serviceNeed)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 20 (Application for Registration of a Motor Vehicle)",
                        "Statutory application form specifying chassis number, engine number, seating capacity, and fuel type.",
                        true,
                        "Generated online from Vahan portal / Signed printout",
                        "Auto-generated on Vahan portal during application or provided by automobile dealer."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 21 (Vehicle Sale Certificate)",
                        "Official sales invoice certificate issued by the vehicle manufacturer/authorized dealer showing chassis & engine details.",
                        true,
                        "Original from vehicle dealership",
                        "Obtain directly from the authorized automobile dealership at delivery."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 22 / 22A (Roadworthiness & Pollution Compliance Certificate)",
                        "Manufacturer's statutory certificate verifying compliance with Bharat Stage (BS-VI) emission and safety standards.",
                        true,
                        "Original signed by vehicle manufacturer",
                        "Provided inside vehicle documentation folder by authorized dealer."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Valid Motor Insurance Certificate",
                        "Mandatory third-party or comprehensive motor insurance policy certificate covering the vehicle.",
                        true,
                        "Digital policy document / printout",
                        "Issued by insurance company (e.g. 5-year policy for 2-wheelers, 3-year policy for cars)."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Temporary Registration Certificate (CRTM)",
                        "Dealer-issued temporary registration certificate (valid for 30 days until permanent RC issue).",
                        false,
                        "Dealer copy / printout",
                        "Provided by dealer if vehicle was driven from showroom before permanent number plate fitment."
                ));
            } else if ("RC_TRANSFER_OWNERSHIP".equalsIgnoreCase(rcSubService)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 29 (Notice of Transfer of Ownership) & Form 30 (Application for Transfer)",
                        "Statutory twin forms executed in duplicate by both registered seller and buyer.",
                        true,
                        "Original signed by Seller and Buyer (Form 29 & 30)",
                        "Download from parivahan.gov.in under Forms section; both parties sign."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Original Registration Certificate (RC Smart Card)",
                        "Original physical RC smart card booklet of the vehicle for endorsement/cancellation.",
                        true,
                        "Original physical smart card",
                        "Must be handed over by seller upon sale."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Valid PUC (Pollution Under Control) Certificate",
                        "Valid emission compliance certificate from an authorized testing station.",
                        true,
                        "Original / DigiLocker digital copy",
                        "Obtain from any petrol pump automated emission testing kiosk."
                ));
            } else if ("RC_DUPLICATE".equalsIgnoreCase(rcSubService)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 26 (Application for Duplicate Certificate of Registration)",
                        "Statutory application form declaring loss, destruction, or mutilation of original RC.",
                        true,
                        "Signed Form 26 + Pencil Stencil of Chassis Number",
                        "Download from parivahan.gov.in; trace chassis stencil with pencil on form."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Police Lost / Missing Article Report (NCR)",
                        "Non-Cognizable Report filed with local police station or online police lost report portal.",
                        true,
                        "Original / Certified online police acknowledgement",
                        "File instantly on state police citizen portal (e.g. AP Police / Delhi Police lost report app)."
                ));
            } else if ("RC_RENEWAL_15_YEAR".equalsIgnoreCase(rcSubService)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 25 (Application for Renewal of Certificate of Registration)",
                        "Application for renewal of private vehicle registration after 15 years.",
                        true,
                        "Signed Form 25 with chassis pencil stencil",
                        "Download from parivahan.gov.in; carry vehicle physically for inspection."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Original 15-Year RC & Up-to-Date Road Tax Receipts",
                        "Original RC booklet and evidence of clearance of state taxes.",
                        true,
                        "Original documents",
                        "Retrieve from personal records; verify tax status on Vahan."
                ));
            }

            // Common Identity & Address for Vehicle
            docs.add(new ServiceRequiredDocumentDto(
                    "Proof of Present Residential Address of Vehicle Owner",
                    "Aadhaar Card, Electricity Bill, Bank Passbook, or Registered Lease Agreement.",
                    true,
                    "Original + 1 self-attested photocopy",
                    "Download mask-free e-Aadhaar from uidai.gov.in."
            ));
            docs.add(new ServiceRequiredDocumentDto(
                    "PAN Card or Form 60",
                    "Statutory income tax identification for vehicle asset registration.",
                    true,
                    "Original PAN Card + photocopy",
                    "Download e-PAN from NSDL / UTIITSL if physical card missing."
            ));

        } else {
            // Driving Licence Documents
            docs.add(new ServiceRequiredDocumentDto(
                    "Proof of Date of Birth & Age",
                    "Aadhaar Card, 10th Standard School Marksheet, Birth Certificate, or Passport.",
                    true,
                    "Original + 1 self-attested photocopy",
                    "Download e-Aadhaar from uidai.gov.in or 10th marksheet via DigiLocker."
            ));

            docs.add(new ServiceRequiredDocumentDto(
                    "Proof of Present Residential Address",
                    "Aadhaar Card, Voter ID, Passport, Electricity Bill, or Student Bonafide with Hostel Certificate.",
                    true,
                    "Original + 1 self-attested photocopy",
                    "Students residing in campus/PG can use Bonafide + Hostel Warden Certificate."
            ));

            docs.add(new ServiceRequiredDocumentDto(
                    "Form 1 (Self-Declaration as to Physical Fitness)",
                    "Statutory declaration declaring visual acuity, color perception, and absence of epilepsy/disabilities.",
                    true,
                    "Auto-generated on Sarathi Parivahan portal during application",
                    "Answer the 6 medical questions online during form submission; electronically signed."
            ));

            if (isAge40Plus || isTransport) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 1A (Medical Certificate by Registered Medical Practitioner)",
                        "Mandatory statutory medical fitness certificate with passport photo signed by an MBBS doctor (RMP).",
                        true,
                        "Original Form 1A signed & stamped by Registered Medical Practitioner",
                        "Download Form 1A from Sarathi; get examined by any registered government/private MBBS doctor."
                ));
            }

            if (isJuniorAge && isMcwog) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Form 2 (Parent / Guardian Consent Declaration)",
                        "Statutory parental consent declaring permission for minor (16–17) to drive moped up to 50cc.",
                        true,
                        "Original signed by parent/guardian in presence of Licensing Authority / online declaration",
                        "Generated as part of Form 2 application on Sarathi Parivahan."
                ));
            }

            if ("PERMANENT_DL".equalsIgnoreCase(serviceNeed)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Valid Form 3 Learner's Licence (LL)",
                        "Original or electronic Form 3 Learner's Licence showing 30+ days of completion.",
                        true,
                        "Printed Form 3 or DigiLocker / mParivahan digital copy",
                        "Download from Sarathi Parivahan portal under 'Print Learner Licence' section."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Original Vehicle for Practical Test with 'L' Board & Valid Documents",
                        "A vehicle matching applied class (MCWG / LMV) with valid RC, Insurance, PUC, and prominent red 'L' plate.",
                        true,
                        "Physical vehicle presented at RTO test track on appointment day",
                        "Ensure vehicle has working headlights, brake lights, indicators, and valid insurance."
                ));
            }

            if ("DL_RENEWAL".equalsIgnoreCase(serviceNeed) || "DL_DUPLICATE".equalsIgnoreCase(serviceNeed) || "DL_ADDRESS_CHANGE".equalsIgnoreCase(serviceNeed)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Existing Original Driving Licence (Smart Card / Booklet)",
                        "Physical driving licence card for endorsement, renewal, or replacement.",
                        !"DL_DUPLICATE".equalsIgnoreCase(serviceNeed),
                        "Original licence card",
                        "Mandatory for renewal/address change. For lost duplicate, submit copy if available."
                ));
            }

            if ("DL_DUPLICATE".equalsIgnoreCase(serviceNeed)) {
                docs.add(new ServiceRequiredDocumentDto(
                        "Police Non-Cognizable Report (NCR) / Online Lost Property Report",
                        "Official police acknowledgment proving driving licence was lost or stolen.",
                        true,
                        "Police report copy / online acknowledgment slip",
                        "File online lost report on State Police portal (takes 5 minutes)."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        "Form LLD (Intimation of Loss/Destruction of Driving Licence)",
                        "Application declaration detailing circumstances of licence loss or destruction.",
                        true,
                        "Generated online on Sarathi portal",
                        "Filled online during duplicate licence application."
                ));
            }

            docs.add(new ServiceRequiredDocumentDto(
                    "Passport Size Color Photograph & Digital Signature",
                    "Recent passport photograph (white background) and signature on white paper for digital upload.",
                    true,
                    "Scanned image files (JPEG/PNG under 200 KB)",
                    "Take clear photo of signature on plain white paper using mobile phone."
            ));
        }

        result.setDocuments(docs);

        // ----------------------------------------------------
        // 4. STEP-BY-STEP APPLICATION PROCEDURE
        // ----------------------------------------------------
        List<ServiceStepDto> steps = new ArrayList<>();
        int stepIdx = 1;

        if ("LEARNER_LICENCE".equalsIgnoreCase(serviceNeed)) {
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Access Sarathi Parivahan & Select State",
                    "1. Visit the official Ministry of Road Transport portal (https://sarathi.parivahan.gov.in).\n2. Select your State (" + stateCode.replace("_", " ") + ") from the drop-down menu.\n3. Click on 'Apply for Learner Licence'.",
                    "5 Minutes",
                    "Always ensure you are on the official .gov.in domain. Beware of fake phishing fee portals."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Aadhaar e-KYC & Online Form 2 Submission",
                    "1. Choose 'Applicant holding Aadhaar Number' for contactless faceless processing without RTO visit.\n2. Authenticate via Aadhaar OTP.\n3. Verify your pre-filled name, DOB, and address.\n4. Select your vehicle class: " + (isBothClasses ? "MCWG (Bike) + LMV (Car)" : (isLmvCar ? "LMV (Car)" : "MCWG (Two-Wheeler)")) + ".\n5. Complete Form 1 physical fitness declaration.",
                    "15 Minutes",
                    "Aadhaar authentication exempts physical document submission at the RTO."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Upload Documents & Digital Signature",
                    "1. Upload age proof (10th marksheet or Aadhaar) and address proof.\n2. Upload clear signature on plain white paper (under 200 KB).\n3. Save your Application Reference Number.",
                    "10 Minutes",
                    "Ensure scans are sharp and uncropped to prevent rejection by the automated verification system."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Online Fee Payment (" + result.getCalculatedFee() + ")",
                    "1. Click 'Fee Payments' on Sarathi portal.\n2. Pay prescribed fee: " + result.getCalculatedFee() + " via UPI, Net Banking, or Debit Card.\n3. Download and print the E-Payment Receipt.",
                    "5 Minutes",
                    "Keep the payment transaction reference number saved for test authentication."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Online Learner's Licence Test (Road Safety & Traffic Signs)",
                    isAadhaarFaceless ?
                            "1. Complete the mandatory 10-minute online Road Safety Tutorial video on Sarathi.\n2. Click 'Online LL Test (STALL)'.\n3. Enter Application Number and OTP.\n4. Answer 15–20 multiple-choice questions on road signs, right-of-way, and traffic rules on your laptop/phone with front camera enabled.\n5. Minimum score required: 60% (e.g. 12 out of 20 correct)." :
                            "1. Book test appointment slot at your regional RTO.\n2. Visit RTO on scheduled date with originals.\n3. Appear for touch-screen computerized LL test at the RTO testing lab.",
                    "20 Minutes",
                    "Take mock tests on parivahan.gov.in beforehand. Mandatory facial proctoring requires adequate room lighting."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Instant Form 3 e-Learner's Licence Download",
                    "1. Immediately upon passing, your Form 3 Learner's Licence is approved electronically.\n2. Download and print your Form 3 Learner's Licence with QR code.\n3. Valid for 6 months across India.\n4. RULE: Display red 'L' symbol and drive accompanied by a licensed driver.",
                    "Immediate upon passing",
                    "Must complete 30 days of holding LL before applying for permanent driving licence test."
            ));

        } else if ("PERMANENT_DL".equalsIgnoreCase(serviceNeed)) {
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Verify 30-Day Mandatory Practice Completion",
                    "Verify that at least 30 days have elapsed from your Learner's Licence issue date, and your LL is within its 6-month validity window.",
                    "Prerequisite check",
                    "Rule 15 of CMVR strictly prohibits testing before 30 days."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Online Form 4 Application on Sarathi Parivahan",
                    "1. Visit https://sarathi.parivahan.gov.in and select State.\n2. Click 'Apply for Driving Licence'.\n3. Enter your active Learner's Licence number and Date of Birth.\n4. Select vehicle classes for permanent testing: " + (isBothClasses ? "MCWG + LMV" : "Applied Class") + ".",
                    "10–15 Minutes",
                    "Confirm your present residential address is up to date."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Fee Payment (" + result.getCalculatedFee() + ") & Test Slot Booking",
                    "1. Pay government driving test and Smart Card fee: " + result.getCalculatedFee() + " online.\n2. Go to 'Appointments' -> 'Slot Booking DL Test'.\n3. Choose your preferred RTO / Automated Driving Test Track (ADTT) and select an open date and morning/afternoon time slot.\n4. Print Appointment Slip.",
                    "10 Minutes",
                    "Book slots early as automated test tracks have limited daily capacity."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "RTO Test Day: Practical Driving Competence Test (ADTT)",
                    "Arrive at the RTO track 30 minutes before your slot with original LL, vehicle matching the applied class with 'L' plate, and valid vehicle RC/Insurance/PUC.\n" +
                            "• Two-Wheeler Test: Ride through the designated Figure-of-8 track without putting foot down or crossing borders; perform balance stop and emergency brake.\n" +
                            "• Four-Wheeler Car Test: Automated sensors evaluate Figure-8 track, Parallel Parking, H-Track, and Gradient (Hill stop & start without rolling back > 12 inches).\n" +
                            "• Biometrics: Fingerprint and live photo captured for Polycarbonate Smart Card.",
                    "1–2 Hours at RTO Track",
                    "Ensure vehicle is in perfect mechanical condition. Re-test requires 7-day waiting period and test fee re-payment."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Approval, Smart Card Printing & Speed Post Dispatch",
                    "1. Motor Vehicle Inspector (MVI) approves test results electronically into Sarathi National Register.\n2. Polycarbonate QR-coded Smart Card DL is queued for secure printing.\n3. Dispatched exclusively via Speed Post to your registered address with SMS tracking.\n4. Immediate access: Digital DL available instantly on DigiLocker and mParivahan apps.",
                    result.getEstimatedTimeline(),
                    "DigiLocker digital DL carries 100% statutory legal validity under Rule 9A of IT Rules across India."
            ));

        } else if ("NEW_VEHICLE_RC".equalsIgnoreCase(serviceNeed)) {
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Showroom Purchase & Temporary Registration",
                    "Automobile dealership delivers vehicle with temporary registration number (CRTM) and provides invoice, Form 21 (Sale Certificate), Form 22 (Roadworthiness), and motor insurance policy.",
                    "At Delivery",
                    "Check chassis and engine numbers on vehicle match Form 21 exactly."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Online Application on Vahan Portal (https://vahan.parivahan.gov.in)",
                    "Dealership or owner initiates permanent registration on Vahan portal, uploading Form 20, 21, 22, insurance, and owner's address proof.",
                    "15 Minutes",
                    "Check owner name spelling against Aadhaar."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Road Tax & Registration Fee Payment (" + result.getCalculatedFee() + ")",
                    "Pay applicable registration fee: " + result.getCalculatedFee() + " plus state one-time road tax calculated on vehicle ex-showroom price.",
                    "Online / Dealer Desk",
                    "Save online Tax Receipt (Form 37)."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Physical Inspection by Motor Vehicle Inspector (MVI)",
                    "MVI examines physical chassis stencil and engine number on vehicle to ensure no tampering or stolen parts.",
                    "1 Hour at RTO/Dealer",
                    "Inspection conducted at RTO premises or authorized dealer premises."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "HSRP Plate Fitment & Permanent RC Smart Card Dispatch",
                    "High Security Registration Plates (HSRP) with laser-branded PIN and chromium hologram are affixed to the vehicle. Permanent RC Smart Card is issued and dispatched via Speed Post.",
                    result.getEstimatedTimeline(),
                    "Driving without HSRP plates attracts heavy statutory fines under CMVR."
            ));

        } else {
            // Lifecycle Services (Renewal, Duplicate, Transfer, Address)
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Access Official Parivahan Portal",
                    "Visit https://sarathi.parivahan.gov.in (for DL) or https://vahan.parivahan.gov.in (for RC). Select State.",
                    "5 Minutes",
                    "Ensure your mobile number is updated on Parivahan for OTP authentication."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Submit Application & Upload Supporting Proofs",
                    "Enter existing licence/RC number and DOB. Select requested modification. Upload mandatory supporting proofs.",
                    "15 Minutes",
                    "Scans must be legible and in color."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Fee Payment (" + result.getCalculatedFee() + ")",
                    "Pay prescribed government fee: " + result.getCalculatedFee() + " online. Print acknowledgement receipt.",
                    "5 Minutes",
                    "Save receipt for tracking."
            ));
            steps.add(new ServiceStepDto((long) stepIdx, stepIdx++,
                    "Verification & Updated Document Dispatch",
                    "Licensing / Registering authority verifies records against the national database. Updated Smart Card is printed and dispatched via Speed Post.",
                    result.getEstimatedTimeline(),
                    "Track real-time status online using Application Number."
            ));
        }

        result.setSteps(steps);

        // ----------------------------------------------------
        // 5. CRITICAL TIPS & WARNINGS
        // ----------------------------------------------------
        List<String> tips = new ArrayList<>();
        tips.add("Document Exact Name Match: Ensure applicant name, father's name, and date of birth match character-for-character between Aadhaar, 10th marksheet, and your application form to prevent RTO rejection.");

        if ("LEARNER_LICENCE".equalsIgnoreCase(serviceNeed)) {
            tips.add("Mandatory 30-Day Practice Rule: Under Central Motor Vehicles Rules, you must wait at least 30 days after getting your Learner's Licence before taking the permanent driving test. Do not schedule your test early.");
            tips.add("Learner Driving Regulations: When driving with a Learner's Licence, you must display a red 'L' board (18cm × 18cm) on front and rear, and MUST be accompanied by a passenger holding a valid permanent driving licence.");
            if (isAadhaarFaceless) {
                tips.add("Contactless Home Test Tip: The online LL test uses AI camera facial proctoring. Sit in a well-lit room without other persons or background noise; looking away from the screen can invalidate the test.");
            }
        }

        if ("PERMANENT_DL".equalsIgnoreCase(serviceNeed)) {
            tips.add("RTO Automated Track Tip: Practice tight figure-8 turns and hill stops (handbrake coordination without rolling backward) before your test date. Automated tracks score with pressure sensors and laser beams with no room for manual discretion.");
            tips.add("Vehicle Documents on Test Day: The test vehicle you bring must have an original valid RC, third-party/comprehensive insurance, valid PUC, and prominent 'L' boards. If using a commercial driving school vehicle, dual controls must be certified.");
        }

        if (isBothClasses) {
            tips.add("Dual-Class Convenience: Applying for MCWG (Bike) and LMV (Car) simultaneously saves money and eliminates taking the computerized LL test twice.");
        }

        if (serviceNeed.startsWith("RC") || "NEW_VEHICLE_RC".equalsIgnoreCase(serviceNeed)) {
            tips.add("High Security Registration Plate (HSRP) Rule: Under MoRTH guidelines, driving any vehicle with non-HSRP plates is illegal. HSRP plates with laser-etched barcodes must be affixed at authorized centers.");
            tips.add("DigiLocker Legal Equivalency: Digital driving licence and vehicle RC stored in DigiLocker or mParivahan apps carry equal legal validity to physical cards under the Information Technology Act 2000.");
        }

        result.setCriticalTips(tips);
        return result;
    }
}
