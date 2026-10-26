package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Master Pathway Evaluator for Aadhaar Card Update & Correction
 * based on Unique Identification Authority of India (UIDAI) regulations,
 * Aadhaar Act 2016, and official myAadhaar portal standards.
 */
@Component
public class AadhaarPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String AADHAAR_SLUG = "aadhaar-update-correction";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return AADHAAR_SLUG.equalsIgnoreCase(s)
                || "aadhaar-update".equalsIgnoreCase(s)
                || "aadhaar-correction".equalsIgnoreCase(s)
                || "aadhaar".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(AADHAAR_SLUG);
        dto.setServiceName(service != null ? service.getName() : "Aadhaar Card Update & Correction");
        dto.setTitle("Aadhaar Update & Correction Navigator");
        dto.setDescription("Answer a few quick questions to identify whether your update can be completed online via myAadhaar or requires visiting an authorized Aadhaar Seva Kendra, check lifetime limits, verify fees, and generate your required document checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: What needs updating?
        questions.add(new QuestionnaireQuestionDto(
                "updateType",
                "What detail do you need to update or correct?",
                "Determines whether the request can be processed online or mandates physical biometric verification at an Aadhaar centre.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("ADDRESS_ONLINE", "Address Update (Self or Head of Family)", "Can be completed 100% online via myAadhaar portal if mobile is linked to Aadhaar.", "Online Self-Service"),
                        new QuestionOptionDto("MOBILE_OR_EMAIL", "Mobile Number or Email ID Update", "MANDATORY in-person visit to Aadhaar Seva Kendra. No documents required; biometric capture only.", "Aadhaar Centre Visit"),
                        new QuestionOptionDto("NAME_DOB_GENDER", "Name, Date of Birth, or Gender Correction", "Demographic update requiring in-person visit with valid original PoI or DoB proof. Strict lifetime limits apply.", "Demographic Update"),
                        new QuestionOptionDto("PHOTO_BIOMETRICS", "Photograph, Fingerprints, or Iris Update", "Requires visiting an Aadhaar Seva Kendra for live camera photo and biometric scanner capture.", "Biometric Update"),
                        new QuestionOptionDto("CHILD_MBU", "Child Mandatory Biometric Update (Age 5 or Age 15)", "Statutory free biometric update for children reaching 5 or 15 years of age at any authorized centre.", "Statutory Free (MBU)"),
                        new QuestionOptionDto("DOC_REVALIDATION", "10-Year Document Re-validation (PoI & PoA)", "For Aadhaar cards issued >10 years ago without recent updates. Can be completed online via myAadhaar.", "Online Re-validation")
                )
        ));

        // Q2: Mobile Number Linking Status
        questions.add(new QuestionnaireQuestionDto(
                "mobileLinked",
                "Is your active mobile number currently linked to your Aadhaar card?",
                "Required to receive One-Time Passwords (OTP) for authenticating into the myAadhaar self-service portal.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("YES_LINKED_OTP", "Yes, active and can receive OTP", "Eligible for online myAadhaar portal access and online address/document updates.", "OTP Authentication Ready"),
                        new QuestionOptionDto("NO_NOT_LINKED", "No, not linked / mobile number changed or lost", "Online portal cannot be accessed without OTP. You must visit an Aadhaar Seva Kendra in person.", "Centre Visit Mandatory")
                )
        ));

        // Q3: Supporting Documentation Availability
        questions.add(new QuestionnaireQuestionDto(
                "hasSupportingDoc",
                "Do you have valid supporting documentation in your own name?",
                "UIDAI requires original documentary proof matching the requested change (PoI, PoA, or DoB).",
                null,
                Arrays.asList(
                        new QuestionOptionDto("HAVE_OWN_DOCS", "Yes, have valid original proof in my name", "Standard PoI (Passport/PAN/Voter ID), PoA (Electricity/Water/Bank statement), or DoB (Birth cert/10th marksheet).", "Standard Verification"),
                        new QuestionOptionDto("HOF_BASED_ADDRESS", "No, want to use Head of Family (HoF) address proof", "Use parent's or spouse's PoA with ration card or marriage certificate proving family relationship.", "HoF Verification Route"),
                        new QuestionOptionDto("NO_DOCUMENTS_STANDARD", "No documents in my name (Need Gazetted Certificate)", "Use standard UIDAI Certificate format (Annexure-I) signed by Gazetted Officer, Tehsildar, or MP/MLA.", "Standard UIDAI Format")
                )
        ));

        // Q4: Lifetime Modification History
        questions.add(new QuestionnaireQuestionDto(
                "lifetimeHistory",
                "Have you previously updated this demographic field in your lifetime?",
                "UIDAI strictly enforces lifetime limits: Name (max 2 times), DOB (max 1 time), Gender (max 1 time).",
                null,
                Arrays.asList(
                        new QuestionOptionDto("FIRST_TIME", "First time or within permissible lifetime limit", "Within UIDAI limits: Name (up to 2 times), DOB (1 time), Gender (1 time). Standard processing.", "Within Limits"),
                        new QuestionOptionDto("EXCEEDED_LIMIT", "Exceeded lifetime limit (Need Exception Approval)", "Updating Name 3rd time or DOB/Gender 2nd time requires Regional Office (RO) approval with Gazette notification.", "Exception Handling Required")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();

        String updateType = answers.getOrDefault("updateType", "ADDRESS_ONLINE");
        String mobileLinked = answers.getOrDefault("mobileLinked", "YES_LINKED_OTP");
        String hasSupportingDoc = answers.getOrDefault("hasSupportingDoc", "HAVE_OWN_DOCS");
        String lifetimeHistory = answers.getOrDefault("lifetimeHistory", "FIRST_TIME");

        List<ServiceStepDto> steps = new ArrayList<>();
        List<ServiceRequiredDocumentDto> documents = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        // Exception Handling: Exceeded lifetime limit
        if ("EXCEEDED_LIMIT".equalsIgnoreCase(lifetimeHistory) && "NAME_DOB_GENDER".equalsIgnoreCase(updateType)) {
            result.setPathwayCode("UIDAI_EXCEPTION_RO_APPROVAL");
            result.setPathwayTitle("Aadhaar Demographic Exception Handling (UIDAI Regional Office Approval)");
            result.setBadgeText("Regional Office Exception Required");
            result.setCalculatedFee("₹50.00 (Statutory Demographic Fee)");
            result.setFeeBreakdown("ASK Demographic Fee: ₹50.00 | Gazette Notification: As per Government Press");
            result.setEstimatedTimeline("30 to 90 Working Days (Subject to Regional Office Scrutiny)");
            result.setMatchReason("You have indicated that you have reached or exceeded UIDAI's lifetime limit (Name: max 2 changes, DOB: max 1 change, Gender: max 1 change). Any further change can only be approved under exceptional circumstances by the UIDAI Regional Office (RO) through a formal Gazette Notification and special approval slip.");
            result.setOfficialPortalUrl("https://uidai.gov.in");

            steps.add(new ServiceStepDto(1, "Publish Gazette Notification / Obtain Court Order", "For a 3rd name change, publish a legal name change notification in the Central or State Gazette. For 2nd DOB change, obtain a high-level medical board or court certificate.", "15–30 Days", "Mandatory legal requirement under UIDAI Office Memorandum."));
            steps.add(new ServiceStepDto(2, "Visit Authorized Aadhaar Seva Kendra (ASK)", "Visit an Aadhaar Seva Kendra, submit the standard demographic update, and obtain an Enrolment ID (EID) slip.", "Day 1", "Inform the operator that this is an exception update."));
            steps.add(new ServiceStepDto(3, "Submit Exception Petition to UIDAI Regional Office", "Email your EID slip, Gazette notification copy, original identity proof, and exception explanation to your jurisdictional UIDAI Regional Office (e.g., ro.hyderabad@uidai.net.in for AP/Telangana).", "1–2 Days", "Include your full 28-digit EID number and contact details."));
            steps.add(new ServiceStepDto(4, "Regional Office Scrutiny & Backend Approval", "The Regional Office verifies the genuineness of the documents with issuing authorities and clears the update in the Central Identities Data Repository (CIDR).", "30–60 Days", "Track status online at myaadhaar.uidai.gov.in using your EID."));

            documents.add(new ServiceRequiredDocumentDto(null, "Central / State Gazette Notification", "Official published notification of name change.", true, "Original Gazette Copy / Digital Gazette PDF", "Published through Department of Publication (egazette.gov.in)."));
            documents.add(new ServiceRequiredDocumentDto(null, "Existing Aadhaar Card / EID Slip", "Current Aadhaar card and the 28-digit update enrolment slip.", true, "Original / Printed EID", "Issued by the Aadhaar Seva Kendra operator."));
            documents.add(new ServiceRequiredDocumentDto(null, "Primary Proof of Identity (PoI)", "Passport, PAN Card, or Voter ID supporting the new name.", true, "Original Certificate", "Official government issued photo ID."));

            tips.add("UIDAI Regional Offices scrutinize exception cases strictly to prevent identity fraud.");
            tips.add("Never pay unofficial agents promising online exception updates without Regional Office approval.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // Case 1: Online Address Update (myAadhaar)
        if ("ADDRESS_ONLINE".equalsIgnoreCase(updateType)) {
            if ("NO_NOT_LINKED".equalsIgnoreCase(mobileLinked)) {
                // Cannot do online without mobile
                result.setPathwayCode("ASK_ADDRESS_UPDATE_OFFLINE");
                result.setPathwayTitle("Aadhaar Address & Mobile Update at Aadhaar Seva Kendra");
                result.setBadgeText("In-Person ASK Visit Required (Mobile Not Linked)");
                result.setCalculatedFee("₹50.00 (Inclusive of GST)");
                result.setFeeBreakdown("Demographic & Mobile Update Fee: ₹50.00");
                result.setEstimatedTimeline("5 to 15 Working Days");
                result.setMatchReason("Because your mobile number is not actively linked to receive OTPs, you cannot use the online myAadhaar self-service portal. You must visit an authorized Aadhaar Seva Kendra in person to update your mobile number and address simultaneously.");
                result.setOfficialPortalUrl("https://uidai.gov.in");

                populateAskSteps(steps, "Address & Mobile Update", "₹50.00");
                populateAddressDocuments(documents, hasSupportingDoc);

                tips.add("Link your active mobile number at the centre so all future address updates can be done online from home.");
                tips.add("Carry original physical documents; photocopies or laminated cards are not accepted by ECMP scanners.");
            } else {
                result.setPathwayCode("ONLINE_MYAADHAAR_ADDRESS");
                result.setPathwayTitle("Online Address Update via myAadhaar Portal (Self-Service)");
                result.setBadgeText("100% Online Self-Service (OTP Verified)");
                result.setCalculatedFee("₹50.00 (Periodic Free Promotional Waivers Apply)");
                result.setFeeBreakdown("myAadhaar Online Portal Charge: ₹50.00 (Often waived during UIDAI campaigns)");
                result.setEstimatedTimeline("3 to 10 Working Days");
                result.setMatchReason("Your mobile number is active for OTP authentication. You can update your residential address online from home through the myAadhaar portal using your own valid Proof of Address (PoA) or Head of Family (HoF) consent, without visiting any centre.");
                result.setOfficialPortalUrl("https://myaadhaar.uidai.gov.in");

                steps.add(new ServiceStepDto(1, "Log in to myAadhaar Portal via OTP", "Visit myaadhaar.uidai.gov.in. Enter your 12-digit Aadhaar number, solve the captcha, and authenticate using the OTP sent to your registered mobile.", "5 Minutes", "Ensure you use the official government portal myaadhaar.uidai.gov.in."));
                steps.add(new ServiceStepDto(2, "Select 'Address Update' & Enter New Details", "Click 'Address Update' → Select 'Update Aadhaar Online' (or 'Head of Family based Address Update'). Enter your new complete residential address including PIN code, District, Village/Town, and Post Office.", "10 Minutes", "The system auto-fills local language text; check spelling carefully."));
                steps.add(new ServiceStepDto(3, "Upload Valid Proof of Address (PoA) Document", "Select your document type from the UIDAI valid list (Electricity bill, Bank statement, Rent agreement, Passport). Upload a clear color scan or photo (PDF/JPEG, under 2 MB).", "5 Minutes", "The document must be in the applicant's name and show the exact new address."));
                steps.add(new ServiceStepDto(4, "Pay Statutory Fee & Save Service Request Number (SRN)", "Pay the nominal fee of ₹50 online via UPI, Net Banking, or Debit/Credit card. Download the Payment Acknowledgement Slip containing your 14-digit SRN.", "2 Minutes", "Preserve the 14-digit SRN to track approval status."));
                steps.add(new ServiceStepDto(5, "Backend Verification & Download Updated e-Aadhaar", "UIDAI back-office verification team validates the uploaded PoA against government databases. Once approved (usually 3–10 days), download your updated digital e-Aadhaar.", "3–10 Days", "The digitally signed e-Aadhaar is legally valid everywhere under Section 4(3) of Aadhaar Act."));

                populateAddressDocuments(documents, hasSupportingDoc);

                tips.add("Ensure uploaded document scan is clear, color, and uncropped with all 4 corners visible.");
                tips.add("Utility bills (electricity, water, gas) and bank statements must be less than 3 months old.");
            }

        } else if ("MOBILE_OR_EMAIL".equalsIgnoreCase(updateType)) {
            // Case 2: Mobile Number or Email Update
            result.setPathwayCode("ASK_MOBILE_EMAIL_UPDATE");
            result.setPathwayTitle("Mobile Number & Email ID Linking / Update (Aadhaar Seva Kendra)");
            result.setBadgeText("In-Person Centre Visit (NO Documents Needed)");
            result.setCalculatedFee("₹50.00 (Inclusive of GST)");
            result.setFeeBreakdown("UIDAI Prescribed Demographic Fee: ₹50.00 (Fixed maximum statutory charge)");
            result.setEstimatedTimeline("5 to 15 Working Days");
            result.setMatchReason("IMPORTANT UIDAI RULE: Updates to Mobile Number or Email ID CANNOT be performed online under any circumstances for cybersecurity reasons. You must visit an authorized Aadhaar Seva Kendra, Bank Branch, or Post Office for in-person biometric authentication. No supporting documents are required.");
            result.setOfficialPortalUrl("https://appointments.uidai.gov.in");

            populateAskSteps(steps, "Mobile & Email Update", "₹50.00");

            documents.add(new ServiceRequiredDocumentDto(null, "Existing Aadhaar Number / Current Card", "Your 12-digit Aadhaar number (or physical card / photocopy / digital copy) to retrieve your profile.", true, "Aadhaar Card or 12-Digit Number", "Any existing copy of your Aadhaar card."));
            documents.add(new ServiceRequiredDocumentDto(null, "Physical Presence of Resident for Biometric Auth", "Resident must appear in person for fingerprint/iris validation before the operator.", true, "In-Person Biometric Scan", "No paper document required; biometrics are captured live."));

            tips.add("STRICT SYSTEM WARNING: Any website, SMS, or agent claiming to link your mobile number to Aadhaar online without a centre visit is FRAUDULENT.");
            tips.add("No proof of address, identity, or phone bill is required to update mobile or email; only your live fingerprint/iris authentication.");

        } else if ("CHILD_MBU".equalsIgnoreCase(updateType)) {
            // Case 3: Child Mandatory Biometric Update (Age 5 or 15)
            result.setPathwayCode("CHILD_MANDATORY_BIOMETRIC_MBU");
            result.setPathwayTitle("Child Mandatory Biometric Update (MBU at Age 5 & Age 15)");
            result.setBadgeText("Statutory FREE of Cost (Aadhaar Centre)");
            result.setCalculatedFee("₹0.00 (FREE under UIDAI Mandate)");
            result.setFeeBreakdown("Government Subsidized: ₹0.00 (No fee can be charged for MBU at age 5–7 and 15–17)");
            result.setEstimatedTimeline("7 to 15 Working Days");
            result.setMatchReason("Under UIDAI regulations, children enrolled before age 5 must update their biometrics (photo, 10 fingerprints, and dual iris) upon reaching age 5, and again upon reaching age 15. This Mandatory Biometric Update is completely FREE of cost at all authorized Aadhaar centres.");
            result.setOfficialPortalUrl("https://appointments.uidai.gov.in");

            populateAskSteps(steps, "Mandatory Biometric Update", "FREE (₹0.00)");

            documents.add(new ServiceRequiredDocumentDto(null, "Child's Existing Aadhaar Card", "Original or printed Aadhaar card of the child.", true, "Physical Card / EID Copy", "Child's existing Aadhaar."));
            documents.add(new ServiceRequiredDocumentDto(null, "Parent / Guardian Aadhaar Card", "Aadhaar card of parent or legal guardian for authentication.", true, "Original Aadhaar Card", "Parent or guardian must accompany minor child."));
            documents.add(new ServiceRequiredDocumentDto(null, "Child's School ID or Birth Certificate", "Original birth certificate or recognized school photo identity card.", false, "Original Document", "Issued by school or municipal authority."));

            tips.add("MBU is 100% free of cost. If an operator asks for money for children aged 5–7 or 15–17, file a complaint on resident.uidai.gov.in or call 1947.");
            tips.add("Failure to complete MBU may lead to biometric deactivation for school admissions and scholarship verifications.");

        } else if ("PHOTO_BIOMETRICS".equalsIgnoreCase(updateType)) {
            // Case 4: Photograph or Biometrics Update
            result.setPathwayCode("ASK_BIOMETRIC_UPDATE");
            result.setPathwayTitle("Photograph & Biometric Update (Aadhaar Seva Kendra)");
            result.setBadgeText("In-Person ASK Visit (Photo & Fingerprints)");
            result.setCalculatedFee("₹100.00 (Inclusive of GST)");
            result.setFeeBreakdown("Biometric Update Fee: ₹100.00 (Includes live photo capture, 10 fingerprints & iris)");
            result.setEstimatedTimeline("7 to 21 Working Days");
            result.setMatchReason("Updating your photograph, fingerprints, or iris requires specialized ECMP biometric hardware and must be completed in person at an authorized Aadhaar Seva Kendra. The operator takes a live webcam photograph; you cannot bring a printed photo.");
            result.setOfficialPortalUrl("https://appointments.uidai.gov.in");

            populateAskSteps(steps, "Biometric & Photograph Update", "₹100.00");

            documents.add(new ServiceRequiredDocumentDto(null, "Existing Aadhaar Card", "Your current Aadhaar card or 12-digit number for profile lookup.", true, "Physical Card or Number", "Current Aadhaar card."));
            documents.add(new ServiceRequiredDocumentDto(null, "Supporting Identity Proof (if updating demographic details too)", "Passport, PAN Card, Voter ID, or Driving Licence if correcting name alongside photo.", false, "Original Government ID", "Official photo ID card."));

            tips.add("The photographer will capture your photo directly using the official UIDAI camera with a white background.");
            tips.add("Biometric update of fingerprints and iris also resolves biometric authentication failures during ration PDS or banking transactions.");

        } else if ("DOC_REVALIDATION".equalsIgnoreCase(updateType)) {
            // Case 5: 10-Year Document Re-validation
            result.setPathwayCode("ONLINE_DOCUMENT_REVALIDATION");
            result.setPathwayTitle("10-Year Aadhaar Document Re-validation (PoI & PoA Update)");
            result.setBadgeText("Online Document Re-validation (myAadhaar)");
            result.setCalculatedFee("₹50.00 (Often Waived FREE during UIDAI Campaigns)");
            result.setFeeBreakdown("Document Update Charge: ₹50.00 (Subject to periodic government fee waiver campaigns)");
            result.setEstimatedTimeline("5 to 15 Working Days");
            result.setMatchReason("UIDAI urges residents who enrolled for Aadhaar over 10 years ago and have never updated their records to re-validate their Proof of Identity (PoI) and Proof of Address (PoA) documents to keep their CIDR records accurate and active.");
            result.setOfficialPortalUrl("https://myaadhaar.uidai.gov.in");

            steps.add(new ServiceStepDto(1, "Log in to myAadhaar Portal with OTP", "Visit myaadhaar.uidai.gov.in, log in with your Aadhaar number and mobile OTP.", "5 Minutes", "Secure login on official portal."));
            steps.add(new ServiceStepDto(2, "Select 'Document Update' Service", "Review your existing demographic details (Name, DOB, Gender, Address) on screen and verify that they are correct.", "2 Minutes", "If demographic details are correct, proceed to upload documents."));
            steps.add(new ServiceStepDto(3, "Upload Valid Proof of Identity (PoI)", "Select your PoI document (PAN card, Voter ID, Passport, Driving Licence) and upload a clear scan.", "5 Minutes", "Name must match Aadhaar exactly."));
            steps.add(new ServiceStepDto(4, "Upload Valid Proof of Address (PoA)", "Select your PoA document (Electricity bill, Bank passbook, Gas bill, Rent agreement) and upload a clear scan.", "5 Minutes", "Address must match Aadhaar exactly."));
            steps.add(new ServiceStepDto(5, "Submit & Download Acknowledgement Slip (URN)", "Pay fee if applicable (or submit during free promotional campaign) and save the 14-digit URN for tracking.", "2 Minutes", "Track status online until verified."));

            documents.add(new ServiceRequiredDocumentDto(null, "Proof of Identity (PoI)", "PAN Card, Voter ID, Passport, or Driving Licence matching name.", true, "Color scan under 2 MB (PDF/JPEG)", "Issued by competent government authority."));
            documents.add(new ServiceRequiredDocumentDto(null, "Proof of Address (PoA)", "Electricity bill, Bank passbook, or Registered lease agreement.", true, "Color scan under 2 MB (PDF/JPEG)", "Must be less than 3 months old for utility bills."));

            tips.add("Document re-validation strengthens the validity of your Aadhaar and prevents unexpected service suspensions.");
            tips.add("Ensure the name and address in the uploaded proofs match your existing Aadhaar records 100%.");

        } else {
            // Case 6: Name, DOB, or Gender Correction
            result.setPathwayCode("ASK_DEMOGRAPHIC_CORRECTION");
            result.setPathwayTitle("Aadhaar Demographic Correction (Name, DOB, or Gender at ASK)");
            result.setBadgeText("Aadhaar Seva Kendra Visit (Original Proof Mandatory)");
            result.setCalculatedFee("₹50.00 (Inclusive of GST)");
            result.setFeeBreakdown("UIDAI Prescribed Demographic Correction Fee: ₹50.00");
            result.setEstimatedTimeline("5 to 15 Working Days");
            result.setMatchReason("Corrections to your legal Name, Date of Birth, or Gender require verifying authentic statutory records and capturing biometric confirmation at an authorized Aadhaar Seva Kendra. Adherence to lifetime limits (Name max 2 times, DOB max 1 time) is verified by the operator.");
            result.setOfficialPortalUrl("https://appointments.uidai.gov.in");

            populateAskSteps(steps, "Demographic Correction", "₹50.00");

            documents.add(new ServiceRequiredDocumentDto(null, "Valid Proof of Identity (PoI) / Proof of DOB", "Original Passport, PAN Card, Voter ID, Driving Licence, or Class 10th Certificate clearly showing the correct name and date of birth.", true, "Original Government Issued Certificate", "Must be presented in original to the operator."));
            documents.add(new ServiceRequiredDocumentDto(null, "Existing Aadhaar Card", "Current physical Aadhaar card or digital copy.", true, "Physical Card or Number", "Required for initial record retrieval."));

            tips.add("UIDAI strictly allows Name change only 2 times, and Date of Birth change only 1 time in a person's lifetime.");
            tips.add("Always verify your details on the operator's secondary customer-facing screen before the enrolment transaction is submitted.");
        }

        result.setSteps(steps);
        result.setDocuments(documents);
        result.setCriticalTips(tips);
        return result;
    }

    private void populateAskSteps(List<ServiceStepDto> steps, String serviceName, String feeText) {
        steps.add(new ServiceStepDto(1, "Book Appointment or Locate Nearest Aadhaar Centre", "Visit appointments.uidai.gov.in or myaadhaar.uidai.gov.in → 'Book an Appointment'. Select your city/location and choose a convenient time slot at your local Aadhaar Seva Kendra (ASK), authorized Bank, or Post Office.", "10 Minutes", "Online slot booking saves wait time. Walk-ins with token are also permitted."));
        steps.add(new ServiceStepDto(2, "Carry Original Supporting Documents & Current Aadhaar", "Gather original physical documents required for your update (e.g. PoI, PoA, or DoB). If updating mobile or biometrics only, no documentation other than your Aadhaar number is needed.", "Before Visit", "Operators only scan original documents; photocopies are rejected."));
        steps.add(new ServiceStepDto(3, "Document Verification & Data Entry by Operator", "Appear at the counter. The certified operator opens the UIDAI ECMP software, verifies your originals, enters the updated details, and scans the original document.", "10–15 Minutes", "Check spelling on the customer-facing secondary monitor."));
        steps.add(new ServiceStepDto(4, "Biometric Capture & Authentication", "Place fingers on the fingerprint scanner, look into the iris camera, and sit for a live webcam photo as required by the update type.", "5 Minutes", "Biometric authentication links your physical identity to the update request."));
        steps.add(new ServiceStepDto(5, "Pay Prescribed Statutory Fee (" + feeText + ")", "Pay the official statutory fee (" + feeText + ") at the cash/digital counter and collect the printed receipt.", "2 Minutes", "Never pay more than the printed receipt amount. UIDAI mandates standard charges."));
        steps.add(new ServiceStepDto(6, "Collect Printed Acknowledgement Slip (EID)", "Operator prints the Update Acknowledgement Slip containing your 28-digit Enrolment ID (EID: 14-digit enrolment number + 14-digit date/time timestamp).", "2 Minutes", "Preserve the EID slip carefully to track processing status online."));
        steps.add(new ServiceStepDto(7, "Track Status & Download Updated e-Aadhaar", "Check update status on myaadhaar.uidai.gov.in under 'Check Enrolment & Update Status'. Once approved (usually 5–15 days), download your updated e-Aadhaar.", "5–15 Days", "Password to open e-Aadhaar PDF is first 4 letters of your name in CAPITAL + year of birth (e.g. SHAS1998)."));
    }

    private void populateAddressDocuments(List<ServiceRequiredDocumentDto> documents, String hasSupportingDoc) {
        if ("HOF_BASED_ADDRESS".equalsIgnoreCase(hasSupportingDoc)) {
            documents.add(new ServiceRequiredDocumentDto(null, "Head of Family (HoF) Relationship Document", "Ration Card, Marriage Certificate, Birth Certificate, or Pension Card establishing relationship with Head of Family.", true, "Color scan under 2 MB (PDF/JPEG)", "Issued by State Food & Civil Supplies or Revenue Department."));
            documents.add(new ServiceRequiredDocumentDto(null, "Head of Family Aadhaar & OTP Consent", "Aadhaar number of the Head of Family and their online OTP consent on myAadhaar portal.", true, "Online Digital Consent via OTP", "HoF must authenticate the request within 30 days of filing."));
        } else if ("NO_DOCUMENTS_STANDARD".equalsIgnoreCase(hasSupportingDoc)) {
            documents.add(new ServiceRequiredDocumentDto(null, "Standard Certificate for Aadhaar Enrolment/Update (Annexure-I)", "Official UIDAI prescribed certificate format with resident photo cross-signed and stamped by Gazetted Officer, Tehsildar, or MP/MLA/Ward Councillor.", true, "Original Certificate on Official Letterhead", "Download blank format from uidai.gov.in and get certified by competent official."));
        } else {
            documents.add(new ServiceRequiredDocumentDto(null, "Proof of Address (PoA) Document", "Valid document from UIDAI Master List: Electricity Bill (<3 months), Water/Gas Bill (<3 months), Bank Passbook with photo & stamp (<3 months), Registered Rent Agreement, or Passport.", true, "Original document or color PDF scan under 2 MB", "Utility bill from discom or bank passbook with branch manager signature and stamp."));
        }
        documents.add(new ServiceRequiredDocumentDto(null, "Existing Aadhaar Card / Number", "Current 12-digit Aadhaar number for authenticating into the portal.", true, "12-digit Aadhaar Number", "Current Aadhaar card."));
    }
}
