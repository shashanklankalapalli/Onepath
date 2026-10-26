package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Master Pathway Evaluator for PAN Card Application & Correction
 * based on Income Tax Department regulations, Section 139A & 139AA of the Income Tax Act 1961,
 * and official Protean eGov Technologies & UTITSL processing standards.
 */
@Component
public class PanCardPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String PAN_SERVICE_SLUG = "pan-card-application-correction";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return PAN_SERVICE_SLUG.equalsIgnoreCase(s)
                || "pan-card".equalsIgnoreCase(s)
                || "pan".equalsIgnoreCase(s)
                || "pan-card-correction".equalsIgnoreCase(s)
                || "pan-application".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(PAN_SERVICE_SLUG);
        dto.setServiceName(service != null ? service.getName() : "PAN Card Application & Correction");
        dto.setTitle("PAN Card Application & Correction Navigator");
        dto.setDescription("Answer a few quick questions to identify the correct statutory application type (Form 49A, 49AA, Instant e-PAN, or CSF Correction), check Aadhaar linking requirements, verify official fees, and generate your tailored document checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Application Type
        questions.add(new QuestionnaireQuestionDto(
                "applicationType",
                "What service do you require?",
                "Select whether you need a new PAN card, corrections to an existing card, or a replacement reprint.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("NEW_PAN_INDIVIDUAL", "New PAN Application (Form 49A - Indian Citizen)", "For Indian citizens who have never been allotted a Permanent Account Number.", "Form 49A (New PAN)"),
                        new QuestionOptionDto("CORRECTION_UPDATE", "Changes or Correction in PAN Data (CSF Form)", "For existing PAN holders needing to update Name, DOB, Father's Name, Photo, or Address.", "PAN Correction (CSF)"),
                        new QuestionOptionDto("REPRINT_EXISTING", "Reprint of Lost / Damaged PAN Card (No Data Change)", "Exact replacement of physical card when details are already correct in the Income Tax database.", "Card Reprint (₹50)"),
                        new QuestionOptionDto("INSTANT_EPAN", "Instant e-PAN via Aadhaar (Income Tax Portal)", "100% free digital PDF e-PAN issued in minutes using Aadhaar OTP. For first-time individual applicants.", "Instant e-PAN (FREE)"),
                        new QuestionOptionDto("FOREIGN_APPLICANT", "New PAN for Foreign Citizen / Entity (Form 49AA)", "For foreign citizens, entities incorporated outside India, or non-resident applicants.", "Form 49AA (Foreign)")
                )
        ));

        // Q2: Existing PAN Holding Status
        questions.add(new QuestionnaireQuestionDto(
                "hasExistingPan",
                "Does the applicant already hold a Permanent Account Number (PAN)?",
                "Possessing more than one PAN card is illegal under Section 272B of the Income Tax Act 1961 (mandatory penalty: ₹10,000).",
                null,
                Arrays.asList(
                        new QuestionOptionDto("NO_FIRST_TIME", "No, never allotted a PAN (First-time applicant)", "Standard new allotment route. Never apply for a second PAN if one was allotted previously.", "First-Time Applicant"),
                        new QuestionOptionDto("YES_SINGLE_PAN", "Yes, hold one active PAN (Correction or Reprint)", "Valid status for submitting Changes or Correction Form (CSF) or Reprint request.", "Active PAN Holder"),
                        new QuestionOptionDto("YES_MULTIPLE_PAN", "Yes, inadvertently hold more than one PAN card", "Illegal under Section 272B. Extra PANs must be surrendered immediately to avoid ₹10,000 penalty.", "Mandatory Surrender Alert")
                )
        ));

        // Q3: Correction Particulars
        questions.add(new QuestionnaireQuestionDto(
                "correctionFields",
                "Which particulars need update or correction, if any?",
                "Applies to existing PAN holders. Determines whether documentary proof of change is required.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("NO_CORRECTION_NEW", "Not Applicable (New PAN Application or Exact Reprint)", "No corrections needed; standard allotment or reprint workflow.", "Standard Processing"),
                        new QuestionOptionDto("NAME_OR_DOB", "Name, Father's Name, or Date of Birth Correction", "Requires statutory proof (Aadhaar, Gazette notification, or Class 10 marksheet).", "Identity Proof Required"),
                        new QuestionOptionDto("PHOTO_SIGNATURE", "Photograph and Signature Update", "Updates physical appearance and official signature on the physical laminated card.", "Biometric Refresh"),
                        new QuestionOptionDto("ADDRESS_CONTACT", "Communication Address, Mobile Number, or Email", "Updates delivery address for card dispatch and e-mail for digital e-PAN delivery.", "Address Proof Required")
                )
        ));

        // Q4: Applicant Category
        questions.add(new QuestionnaireQuestionDto(
                "applicantCategory",
                "What is the status of the applicant?",
                "Determines the 4th character of your 10-digit PAN (P=Individual, C=Company, F=Firm, T=Trust, H=HUF).",
                null,
                Arrays.asList(
                        new QuestionOptionDto("INDIVIDUAL_CITIZEN", "Individual Indian Citizen (Adult or Minor)", "4th character is 'P'. Form 49A with Aadhaar e-KYC paperless option.", "Individual (P)"),
                        new QuestionOptionDto("COMPANY_LLP_FIRM", "Company / LLP / Partnership Firm / Trust / HUF", "4th character is 'C', 'F', 'T', or 'H'. Requires entity registration deeds from RoC / Registrar.", "Entity / Corporate"),
                        new QuestionOptionDto("FOREIGN_NATIONAL", "Foreign Citizen / Non-Resident Entity", "Requires Form 49AA and apostilled foreign passport / certificate of incorporation.", "Foreign Entity")
                )
        ));

        // Q5: Submission Mode
        questions.add(new QuestionnaireQuestionDto(
                "submissionMode",
                "Preferred application processing mode?",
                "Protean and UTITSL provide multiple electronic and physical application channels.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("PAPERLESS_EKYC", "Paperless Aadhaar e-KYC (Fastest: 2–4 Days)", "Completely digital via Aadhaar OTP. Photo is pulled from Aadhaar; no physical documents to courier.", "Fastest Paperless Mode"),
                        new QuestionOptionDto("SCAN_ESIGN", "e-Sign Mode (Upload Scanned Photo & Signature)", "Upload your preferred passport photo and signature scan. Sign digitally via Aadhaar OTP.", "Custom Photo & Sign"),
                        new QuestionOptionDto("PHYSICAL_POSTAL", "Physical Forwarding Mode (Postal Mail)", "Print acknowledgement form, affix 2 passport photos, sign across photos, and post to Pune or Mumbai.", "Courier / Postal")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();

        String applicationType = answers.getOrDefault("applicationType", "NEW_PAN_INDIVIDUAL");
        String hasExistingPan = answers.getOrDefault("hasExistingPan", "NO_FIRST_TIME");
        String correctionFields = answers.getOrDefault("correctionFields", "NO_CORRECTION_NEW");
        String applicantCategory = answers.getOrDefault("applicantCategory", "INDIVIDUAL_CITIZEN");
        String submissionMode = answers.getOrDefault("submissionMode", "PAPERLESS_EKYC");

        List<ServiceStepDto> steps = new ArrayList<>();
        List<ServiceRequiredDocumentDto> documents = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        // 1. Critical Legal Violation: Multiple PANs
        if ("YES_MULTIPLE_PAN".equalsIgnoreCase(hasExistingPan)) {
            result.setPathwayCode("PAN_SURRENDER_SECTION_272B");
            result.setPathwayTitle("Surrender of Duplicate PAN Cards (Section 272B Penalty Avoidance)");
            result.setBadgeText("Statutory Penalty Warning: Section 272B");
            result.setCalculatedFee("₹107.00 (Statutory Correction Form Fee)");
            result.setFeeBreakdown("CSF Processing Fee: ₹107.00 | Penalty Avoided: ₹10,000 under Section 272B");
            result.setEstimatedTimeline("15 to 30 Working Days");
            result.setMatchReason("STATUTORY WARNING: Possessing or applying for more than one PAN card is strictly prohibited under Section 139A(7) of the Income Tax Act, 1961. A mandatory penalty of ₹10,000 is leviable under Section 272B for holding duplicate PAN cards. You must retain one primary PAN and immediately surrender the secondary PAN(s) using the Changes or Correction (CSF) form.");
            result.setOfficialPortalUrl("https://www.protean-tinpan.com");

            steps.add(new ServiceStepDto(1, "Identify Primary PAN & Unused Duplicate PAN", "Select the PAN card that is linked to your bank accounts, ITR filings, and Aadhaar as your Primary PAN. Identify the unused secondary PAN number(s) to surrender.", "Day 1", "Never use the duplicate PAN for any financial transactions."));
            steps.add(new ServiceStepDto(2, "Complete PAN Changes / Correction (CSF) Form", "Open the PAN Correction / CSF form on Protean (protean-tinpan.com) or UTITSL. Enter your Primary PAN at the top in Item 11.", "30 Minutes", "In Item 11 ('Mention other Permanent Account Numbers'), enter all duplicate PAN numbers to be cancelled."));
            steps.add(new ServiceStepDto(3, "Submit Form & Write Surrender Letter to Assessing Officer", "Submit the application online and write a formal surrender letter addressed to your jurisdictional Income Tax Assessing Officer (AO) requesting cancellation of the duplicate PAN.", "2–3 Days", "Attach copies of both PAN cards and the CSF acknowledgement slip."));
            steps.add(new ServiceStepDto(4, "Obtain Cancellation Acknowledgement from IT Department", "Submit the letter to your local Income Tax Office (Aayakar Bhavan). Obtain an official stamped acknowledgement for your permanent tax records.", "7–15 Days", "Keep the stamped acknowledgement permanently as legal defense against Section 272B penalties."));

            documents.add(new ServiceRequiredDocumentDto(null, "Copies of Both Primary & Duplicate PAN Cards", "Photocopies of all PAN cards allotted to the applicant.", true, "Physical Photocopy", "Existing physical PAN cards."));
            documents.add(new ServiceRequiredDocumentDto(null, "Formal Letter Addressed to Jurisdictional Assessing Officer (AO)", "Written request explaining inadvertent duplicate allotment and requesting cancellation.", true, "Original Signed Letter", "Self-drafted letter to Income Tax Assessing Officer."));
            documents.add(new ServiceRequiredDocumentDto(null, "Aadhaar Card (Proof of Identity & Address)", "Primary proof of identity and address matching primary PAN.", true, "Original PDF / Photocopy", "UIDAI issued e-Aadhaar."));

            tips.add("Holding more than one PAN card can lead to freezing of bank accounts and Section 272B prosecution.");
            tips.add("Always preserve the stamped Income Tax Department acknowledgement indefinitely.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // 2. Instant e-PAN (Income Tax e-Filing Portal)
        if ("INSTANT_EPAN".equalsIgnoreCase(applicationType)) {
            result.setPathwayCode("INSTANT_EPAN_INCOME_TAX");
            result.setPathwayTitle("Instant e-PAN via Aadhaar e-KYC (Income Tax e-Filing Portal)");
            result.setBadgeText("100% FREE • Immediate Digital PDF");
            result.setCalculatedFee("₹0.00 (Statutory FREE under CBDT)");
            result.setFeeBreakdown("Government Free Service: ₹0.00 (Digitally signed PDF download)");
            result.setEstimatedTimeline("Instant (10 to 30 Minutes)");
            result.setMatchReason("The Income Tax Department provides a completely paperless, digital facility to generate an instant e-PAN within minutes using Aadhaar-based e-KYC. This facility is 100% FREE of cost for first-time individual applicants whose active mobile number is linked to Aadhaar. You receive a digitally signed PDF with a QR code that is legally valid everywhere.");
            result.setOfficialPortalUrl("https://www.incometax.gov.in");

            steps.add(new ServiceStepDto(1, "Access Income Tax e-Filing Portal", "Visit the official Income Tax portal (incometax.gov.in) → Quick Links → Select 'Instant e-PAN' → Click 'Get New e-PAN'.", "2 Minutes", "Ensure you are using the official incometax.gov.in domain."));
            steps.add(new ServiceStepDto(2, "Enter 12-Digit Aadhaar & Validate OTP", "Enter your 12-digit Aadhaar number, accept the consent conditions, and submit. Enter the 6-digit OTP received on your Aadhaar-linked mobile.", "3 Minutes", "Your Aadhaar must not be linked to any pre-existing PAN card."));
            steps.add(new ServiceStepDto(3, "Validate Demographic Details & Email (Optional)", "Review your photo, name, date of birth, gender, and address pulled directly from UIDAI. Validate your email ID via OTP if desired.", "2 Minutes", "Details in Aadhaar become your official PAN details."));
            steps.add(new ServiceStepDto(4, "Submit Request & Save 15-Digit Acknowledgement Number", "Submit the request. The portal generates a 15-digit Acknowledgement Number sent via SMS.", "1 Minute", "Save the acknowledgement number for checking status."));
            steps.add(new ServiceStepDto(5, "Download Digitally Signed e-PAN PDF", "Return to 'Instant e-PAN' → 'Check Status / Download PAN'. Enter Aadhaar and OTP. Download your e-PAN PDF within 10–30 minutes.", "10–30 Mins", "Password to open e-PAN is your Date of Birth in DDMMYYYY format (e.g. 15081998)."));

            documents.add(new ServiceRequiredDocumentDto(null, "Valid 12-Digit Aadhaar Card with Linked Mobile Number", "Mandatory prerequisite; demographic data is extracted directly from UIDAI records.", true, "Aadhaar Number + OTP Authentication", "UIDAI e-Aadhaar."));

            tips.add("Instant e-PAN generates a digital PDF card. If you later desire a physical plastic card, order an Aadhaar PVC card or PAN reprint for ₹50.");
            tips.add("The digitally signed e-PAN carries equal legal validity as a physical laminated card under Rule 114 of Income Tax Rules.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // 3. Foreign Citizen / Entity (Form 49AA)
        if ("FOREIGN_APPLICANT".equalsIgnoreCase(applicationType) || "FOREIGN_NATIONAL".equalsIgnoreCase(applicantCategory)) {
            result.setPathwayCode("FOREIGN_PAN_FORM_49AA");
            result.setPathwayTitle("PAN Application for Foreign Citizens & Entities (Form 49AA)");
            result.setBadgeText("Form 49AA (International Dispatch)");
            result.setCalculatedFee("₹1,017.00 (Inclusive of GST & International Airmail)");
            result.setFeeBreakdown("Form 49AA Application Fee: ₹93.00 + GST + International Speed Post Dispatch: ₹864.00 = Total ₹1,017.00");
            result.setEstimatedTimeline("15 to 25 Working Days");
            result.setMatchReason("Foreign citizens, Non-Resident Indians (NRIs) with foreign communication addresses, and entities incorporated outside India must apply using Form 49AA through Protean or UTITSL. Supporting documents must be apostilled or attested by the Indian Embassy/High Commission.");
            result.setOfficialPortalUrl("https://www.protean-tinpan.com");

            steps.add(new ServiceStepDto(1, "Fill Online Form 49AA on Protean or UTITSL", "Select 'Application for Allotment of New PAN (Form 49AA)' on protean-tinpan.com or utiitsl.com. Complete personal/entity particulars and foreign address.", "30 Minutes", "Ensure legal name matches foreign passport exactly."));
            steps.add(new ServiceStepDto(2, "Upload or Assemble Apostilled Supporting Proofs", "Provide copy of valid foreign passport (apostilled or certified by Indian Embassy) and overseas address proof (bank statement / utility bill).", "1–2 Days", "Apostille certification is mandatory for non-Hague Convention countries."));
            steps.add(new ServiceStepDto(3, "Pay International Processing Fee Online", "Pay ₹1,017.00 online using international credit card or net banking. Save the 15-digit Acknowledgement Number.", "5 Minutes", "Preserve payment receipt for tax records."));
            steps.add(new ServiceStepDto(4, "Dispatch Physical Dossier (if not digitally signed)", "If using physical mode, print acknowledgement, affix 2 photos, sign, and courier to Protean (Pune) or UTITSL (Mumbai) within 15 days.", "7–10 Days", "Courier tracking number should be retained."));
            steps.add(new ServiceStepDto(5, "e-PAN Delivery & International Card Dispatch", "e-PAN PDF is delivered via email within 5–7 days; physical laminated card is dispatched to the overseas address via registered airmail.", "15–25 Days", "Speed Post tracking number is shared via SMS/email."));

            documents.add(new ServiceRequiredDocumentDto(null, "Valid Foreign Passport / OCI Card", "Apostilled copy of valid passport showing nationality, photo, and legal name.", true, "Apostilled / Embassy Attested Copy", "Issued by foreign government."));
            documents.add(new ServiceRequiredDocumentDto(null, "Proof of Overseas Address", "Foreign bank statement, utility bill, or certificate of residence attested by Indian Embassy.", true, "Attested Proof under 3 months old", "Overseas bank or local utility."));

            tips.add("Ensure the name format strictly matches your foreign passport to prevent international clearance delays.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // 4. Changes or Correction in PAN Data (CSF Form)
        if ("CORRECTION_UPDATE".equalsIgnoreCase(applicationType)) {
            result.setPathwayCode("PAN_CORRECTION_CSF_FORM");
            result.setPathwayTitle("Changes or Correction in PAN Data (CSF Form via Protean / UTITSL)");
            result.setBadgeText("PAN Correction / Update Form (CSF)");
            result.setCalculatedFee("₹107.00 (Physical Card + e-PAN) / ₹66.00 (e-PAN Only)");
            result.setFeeBreakdown("Processing Fee: ₹91.00 + 18% GST (₹16.00) = ₹107.00 for Physical Card Dispatch");
            result.setEstimatedTimeline("5 to 15 Working Days (2–4 Days if paperless Aadhaar e-KYC)");
            result.setMatchReason("For existing PAN holders who need to correct errors in Name, Father's Name, Date of Birth, or update their residential address, photograph, or signature. The allotted 10-character PAN remains unchanged; an updated physical card and digital e-PAN are issued reflecting the corrected particulars.");
            result.setOfficialPortalUrl("https://www.protean-tinpan.com");

            populateCorrectionSteps(steps, submissionMode);
            populateCorrectionDocuments(documents, correctionFields);

            tips.add("IMPORTANT: Check the checkbox next to the specific field(s) you wish to correct. Leaving checkboxes blank results in no update.");
            tips.add("Under Section 139AA, your updated PAN must be linked with your Aadhaar card to remain operative.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // 5. Reprint of Lost / Damaged PAN Card (No Data Change)
        if ("REPRINT_EXISTING".equalsIgnoreCase(applicationType)) {
            result.setPathwayCode("PAN_REPRINT_PHYSICAL_CARD");
            result.setPathwayTitle("Reprint of Lost / Damaged PAN Card (Protean / UTITSL Reprint Portal)");
            result.setBadgeText("Fast-Track Card Reprint (₹50)");
            result.setCalculatedFee("₹50.00 (Inclusive of GST & Speed Post Delivery)");
            result.setFeeBreakdown("Reprint Charge: ₹50.00 (Includes domestic Speed Post home delivery)");
            result.setEstimatedTimeline("5 to 10 Working Days");
            result.setMatchReason("For citizens who have misplaced, lost, or damaged their physical PAN card, but whose existing details in the Income Tax database are completely correct. No new documents or form filling is required. You can order a direct reprint via Protean or UTITSL using your PAN and Aadhaar numbers.");
            result.setOfficialPortalUrl("https://www.onlineservices.nsdl.com/paam/ReprintEPan.html");

            steps.add(new ServiceStepDto(1, "Access Official Reprint Portal", "Visit Protean Reprint Portal (onlineservices.nsdl.com/paam/ReprintEPan.html) or UTITSL Reprint facility.", "2 Minutes", "Ensure you use the portal corresponding to your original PAN issuing authority."));
            steps.add(new ServiceStepDto(2, "Enter PAN, Aadhaar, and Date of Birth", "Enter your 10-character PAN, 12-digit Aadhaar number, and Month/Year of Birth. Accept the statutory consent declaration.", "3 Minutes", "Your PAN and Aadhaar must already be linked."));
            steps.add(new ServiceStepDto(3, "Authenticate via Mobile / Email OTP", "Select OTP delivery mode (Mobile, Email, or Both). Enter the 6-digit OTP sent to your registered contact.", "2 Minutes", "OTP verification ensures authorized card reissue."));
            steps.add(new ServiceStepDto(4, "Pay Statutory Reprint Fee (₹50)", "Pay ₹50.00 online via UPI, Net Banking, or Debit Card. Download the Payment Acknowledgement Receipt.", "2 Minutes", "Preserve the 15-digit Acknowledgement Number."));
            steps.add(new ServiceStepDto(5, "Physical Card Dispatch via Speed Post", "The Income Tax Department prints the exact replacement card and dispatches it via India Post Speed Post to your registered address.", "5–10 Days", "Track postal delivery using the tracking number received via SMS."));

            documents.add(new ServiceRequiredDocumentDto(null, "Existing 10-Character PAN Number", "Your allotted PAN number to retrieve record from database.", true, "10-Digit Alphanumeric PAN", "Previous tax returns or bank passbook."));
            documents.add(new ServiceRequiredDocumentDto(null, "Aadhaar Card with Linked Mobile Number", "Required for instant OTP authentication on the reprint gateway.", true, "Aadhaar Number + OTP", "UIDAI e-Aadhaar."));

            tips.add("Reprint is only for cards with NO change in data. If you need to change your address or photo, use the CSF Correction form.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // 6. Non-Individual Entity (Company / LLP / Firm / Trust)
        if ("COMPANY_LLP_FIRM".equalsIgnoreCase(applicantCategory)) {
            result.setPathwayCode("ENTITY_PAN_FORM_49A");
            result.setPathwayTitle("PAN Application for Companies, LLPs, Firms, & Trusts (Form 49A)");
            result.setBadgeText("Corporate / Entity PAN (Form 49A)");
            result.setCalculatedFee("₹107.00 (Physical Card + e-PAN)");
            result.setFeeBreakdown("Statutory Entity Fee: ₹107.00 (Includes GST and physical card dispatch)");
            result.setEstimatedTimeline("7 to 15 Working Days");
            result.setMatchReason("For corporate entities, LLPs, partnership firms, trusts, and Hindu Undivided Families (HUFs) operating in India. The 4th character of the PAN indicates the legal entity status (C=Company, F=Firm, T=Trust, H=HUF). Application requires authorized signatory credentials and entity incorporation deeds.");
            result.setOfficialPortalUrl("https://www.protean-tinpan.com");

            steps.add(new ServiceStepDto(1, "Select Entity Category on Protean / UTITSL", "Choose Form 49A. Select entity type: Company, Partnership Firm, Limited Liability Partnership, or Trust.", "10 Minutes", "Enter official registered entity name matching registration deed exactly."));
            steps.add(new ServiceStepDto(2, "Fill Entity Particulars & Authorized Signatory Details", "Enter registration number, date of incorporation, registered office address, and details of Managing Director / Partner / Trustee.", "20 Minutes", "Single names or initials must adhere strictly to RoC records."));
            steps.add(new ServiceStepDto(3, "Upload Entity Registration Deed / RoC Certificate", "Upload Certificate of Incorporation issued by Registrar of Companies (RoC), Partnership Deed, or Trust Deed.", "10 Minutes", "For DSC mode, authorized signatory signs with Class-3 Digital Signature Certificate."));
            steps.add(new ServiceStepDto(4, "Pay Statutory Fee & Obtain Acknowledgement", "Pay ₹107.00 online. Print the 15-digit acknowledgement slip.", "5 Minutes", "For physical mode, affix authorized stamp and sign."));
            steps.add(new ServiceStepDto(5, "Processing & Dispatch to Registered Office", "Income Tax Department allots the entity PAN. Digital e-PAN is emailed to the official entity email, and physical card is delivered to registered office.", "7–15 Days", "Speed Post tracking provided via email."));

            documents.add(new ServiceRequiredDocumentDto(null, "Certificate of Incorporation / Registration Deed", "RoC Certificate of Incorporation for Companies/LLPs; Partnership Deed for Firms; Trust Deed for Trusts.", true, "Official Certified Document Copy", "Issued by Ministry of Corporate Affairs / Charity Commissioner."));
            documents.add(new ServiceRequiredDocumentDto(null, "Identity & Address Proof of Authorized Signatory", "Aadhaar Card, Passport, or Voter ID of Director, Managing Partner, or Karta.", true, "Government Photo ID", "Individual proof of authorized signatory."));

            tips.add("Entity PAN is mandatory before opening a current bank account or applying for GST registration.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // 7. Standard New PAN Application (Form 49A - Indian Citizen)
        result.setPathwayCode("NEW_PAN_FORM_49A_INDIVIDUAL");
        result.setPathwayTitle("New PAN Card Application for Indian Citizens (Form 49A)");
        result.setBadgeText("New PAN (Form 49A) • Paperless e-KYC Available");
        result.setCalculatedFee("₹107.00 (Physical Card + e-PAN) / ₹66.00 (e-PAN Only)");
        result.setFeeBreakdown("Application Fee: ₹91.00 + 18% GST (₹16.00) = ₹107.00 (Includes domestic Speed Post delivery)");
        result.setEstimatedTimeline("PAPERLESS_EKYC".equalsIgnoreCase(submissionMode) ? "2 to 4 Working Days" : "7 to 15 Working Days");
        result.setMatchReason("For individual Indian citizens applying for their first Permanent Account Number (PAN). You can apply through Protean (formerly NSDL e-Gov) or UTITSL. Choosing the paperless Aadhaar e-KYC mode generates your PAN in 2–4 days without mailing any physical paper documents.");
        result.setOfficialPortalUrl("https://www.protean-tinpan.com");

        populateNewPanSteps(steps, submissionMode);
        populateStandardDocuments(documents, submissionMode);

        tips.add("Paperless Aadhaar e-KYC is the fastest route (2–4 days); your photo is pulled directly from your Aadhaar card.");
        tips.add("Ensure your name and date of birth match your Aadhaar card 100% to avoid biometric e-KYC rejection.");
        tips.add("Single names without a surname should be entered entirely in the 'Last Name / Surname' field.");

        result.setSteps(steps);
        result.setDocuments(documents);
        result.setCriticalTips(tips);
        return result;
    }

    private void populateNewPanSteps(List<ServiceStepDto> steps, String submissionMode) {
        if ("PAPERLESS_EKYC".equalsIgnoreCase(submissionMode)) {
            steps.add(new ServiceStepDto(1, "Access Online Application on Protean / UTITSL Portal", "Visit protean-tinpan.com or utiitsl.com. Under Application Type, select 'New PAN - Indian Citizen (Form 49A)'. Select Category as 'Individual'.", "5 Minutes", "Ensure you use authorized service portals."));
            steps.add(new ServiceStepDto(2, "Select 'Submit Digitally through e-KYC & e-Sign (Paperless)'", "Choose the paperless mode. Enter personal details matching your Aadhaar card letter-for-letter.", "10 Minutes", "In paperless mode, your photograph is extracted directly from UIDAI."));
            steps.add(new ServiceStepDto(3, "Enter Parent Details & Income Source", "Provide your Father's Name (standard default) or Mother's Name (if single parent). Select your source of income (e.g. Salary, Business, or Income from Other Sources/Student).", "5 Minutes", "For married female applicants, Father's Name remains the standard default under Income Tax rules."));
            steps.add(new ServiceStepDto(4, "Assign Assessing Officer (AO) Code", "Select Indian Citizen → Choose your State and City. The portal displays your jurisdictional Area Code, AO Type, Range Code, and AO Number. Click to select.", "3 Minutes", "The system automatically selects the correct tax jurisdiction."));
            steps.add(new ServiceStepDto(5, "Aadhaar Authentication & Statutory Fee Payment", "Enter Aadhaar number and validate using OTP. Pay the official fee of ₹107.00 online via UPI, Net Banking, or Debit Card.", "5 Minutes", "Preserve the 15-digit Acknowledgement Number generated."));
            steps.add(new ServiceStepDto(6, "e-Sign Authorization & e-PAN Delivery", "Complete final Aadhaar e-Sign on the NSDL/UTITSL electronic signature gateway using a second OTP. Your e-PAN PDF is delivered via email in 24–48 hours.", "2 Minutes", "No paper forms or documents need to be couriered."));
            steps.add(new ServiceStepDto(7, "Physical Laminated Card Speed Post Delivery", "The Income Tax Department prints your physical tamper-proof laminated PAN card and dispatches it to your Aadhaar residential address via Speed Post.", "2–4 Days", "Track dispatch status online using your 15-digit acknowledgement number."));
        } else {
            steps.add(new ServiceStepDto(1, "Complete Form 49A Online & Upload Scanned Documents", "Fill Form 49A on Protean or UTITSL. Upload scanned color copies of your passport photograph, signature, PoI, PoA, and PoB.", "20 Minutes", "Photo must be 200 DPI color, 3.5 x 2.5 cm (under 50 KB); signature must be under 20 KB."));
            steps.add(new ServiceStepDto(2, "Pay Statutory Fee & Print 15-Digit Acknowledgement", "Pay ₹107.00 online. Print the generated 15-digit Acknowledgement Form.", "5 Minutes", "Check all entered particulars carefully."));
            steps.add(new ServiceStepDto(3, "Affix Photographs & Sign Across Border Box", "Paste two recent passport-size photographs. Sign across the left photo, and sign strictly inside the signature box below the right photo.", "10 Minutes", "Signature must not touch or cross the outer border lines."));
            steps.add(new ServiceStepDto(4, "Postal Dispatch to Protean (Pune) or UTITSL (Mumbai)", "Courier or speed-post the signed acknowledgement form along with self-attested copies of PoI, PoA, and PoB to the processing center within 15 days.", "1–3 Days", "Write 'APPLICATION FOR PAN -- ACKNOWLEDGEMENT NUMBER' on top of envelope."));
            steps.add(new ServiceStepDto(5, "Verification & Card Dispatch", "Income Tax Department verifies physical documents and dispatches your physical card via Speed Post.", "7–15 Days", "Digital e-PAN is emailed as soon as the PAN is allotted."));
        }
    }

    private void populateCorrectionSteps(List<ServiceStepDto> steps, String submissionMode) {
        steps.add(new ServiceStepDto(1, "Select 'Changes or Correction in Existing PAN Data' (CSF Form)", "On protean-tinpan.com or utiitsl.com, select Application Type as 'Changes or Correction in existing PAN Data / Reprint of PAN Card (No change in existing PAN data)'.", "5 Minutes", "Enter your existing 10-character PAN number accurately."));
        steps.add(new ServiceStepDto(2, "Check Checkbox Next to Fields Requiring Correction", "CRITICAL RULE: Check the tick-box on the left margin for every field you are updating (Name, Date of Birth, Father's Name, or Address). Enter the new/correct details.", "15 Minutes", "Fields left unchecked will NOT be updated in the Income Tax database."));
        steps.add(new ServiceStepDto(3, "Upload Documentary Proof of Correction", "Upload valid supporting documentary proof proving the change (Aadhaar card for name/DOB; Marriage certificate/Gazette notification if changing surname/legal name).", "10 Minutes", "The proof must clearly substantiate the correction requested."));
        steps.add(new ServiceStepDto(4, "Pay Statutory Correction Fee (₹107)", "Pay the official processing fee of ₹107.00 online via UPI, Net Banking, or Card. Save your 15-digit Acknowledgement Number.", "5 Minutes", "Preserve acknowledgement slip for status tracking."));
        steps.add(new ServiceStepDto(5, "Aadhaar e-Sign / Postal Submission", "If using paperless e-Sign mode, authenticate with Aadhaar OTP. If using physical mode, post the signed acknowledgement with proof of PAN to Pune/Mumbai.", "Same Day / Postal", "No postal mailing needed if authenticated via Aadhaar OTP."));
        steps.add(new ServiceStepDto(6, "Verification & Updated PAN Card Delivery", "The Income Tax Department updates your record in the Central database. An updated e-PAN is emailed, and the updated physical card is dispatched via Speed Post.", "5–15 Days", "Your 10-character PAN remains identical; only card particulars are refreshed."));
    }

    private void populateStandardDocuments(List<ServiceRequiredDocumentDto> docs, String submissionMode) {
        if ("PAPERLESS_EKYC".equalsIgnoreCase(submissionMode)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Aadhaar Card (Single Statutory Proof)", "In paperless e-KYC mode, Aadhaar serves as sole comprehensive proof of identity, address, and date of birth.", true, "Aadhaar Number + Mobile OTP Authentication", "UIDAI issued e-Aadhaar."));
        } else {
            docs.add(new ServiceRequiredDocumentDto(null, "Proof of Identity (PoI)", "Passport, Voter ID, Driving Licence, or Bank Certificate with photo proving legal identity.", true, "Original color scan (under 300 KB)", "Issued by competent government authority."));
            docs.add(new ServiceRequiredDocumentDto(null, "Proof of Address (PoA)", "Aadhaar Card, Electricity Bill (<3 months), Water/Gas Bill (<3 months), Bank Passbook/Statement (<3 months), or Passport.", true, "Original color scan (under 300 KB)", "Utility company or bank branch."));
            docs.add(new ServiceRequiredDocumentDto(null, "Proof of Date of Birth (PoB)", "Birth Certificate, Class 10th Matriculation Marksheet, Passport, or Pension Payment Order.", true, "Original color scan (under 300 KB)", "Municipal Corporation or Education Board."));
            docs.add(new ServiceRequiredDocumentDto(null, "Recent Color Passport Photographs (2 Photos)", "Recent 3.5 x 2.5 cm color photographs with white background and clear frontal facial view.", true, "Original physical prints (under 50 KB for upload)", "Photo studio print."));
            docs.add(new ServiceRequiredDocumentDto(null, "Applicant Signature / Thumb Impression Scan", "Clear signature in black ink inside prescribed box (or left thumb impression attested by Magistrate/Notary).", true, "Black ink scan (under 20 KB)", "Signed on blank white paper."));
        }
    }

    private void populateCorrectionDocuments(List<ServiceRequiredDocumentDto> docs, String correctionFields) {
        docs.add(new ServiceRequiredDocumentDto(null, "Copy of Existing Allotted PAN Card / Allotment Letter", "Copy of existing physical PAN card or official PAN allotment letter proving allotted number.", true, "Photocopy / Clear PDF Scan", "Existing PAN card."));
        docs.add(new ServiceRequiredDocumentDto(null, "Proof of Identity (PoI) & Address (PoA)", "Aadhaar Card, Passport, or Voter ID supporting correct name and address.", true, "Original color scan", "Government issued photo ID."));

        if ("NAME_OR_DOB".equalsIgnoreCase(correctionFields)) {
            docs.add(new ServiceRequiredDocumentDto(null, "Proof of Date of Birth (Birth Cert / 10th Marksheet)", "Statutory municipal birth certificate or secondary education marksheet clearly showing correct DOB.", true, "Original Municipal or Board Certificate", "Issued by Registrar of Births & Deaths / Board."));
            docs.add(new ServiceRequiredDocumentDto(null, "Proof of Change of Name (Marriage Cert / Gazette / Deed Poll)", "For female applicants changing surname post-marriage: Marriage Certificate or Wedding Card; for other name changes: Official Gazette notification.", false, "Official Gazette Notification or Marriage Certificate", "Department of Publication or Registrar of Marriages."));
        }
    }
}
