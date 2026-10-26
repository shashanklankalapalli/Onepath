package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Master Pathway Evaluator for Voter ID Registration & Address Change
 * based on the Representation of the People Act (1950/1951), Registration of Electors Rules 1960,
 * and official Election Commission of India (ECI) Voter Services Portal standards (voters.eci.gov.in).
 */
@Component
public class VoterIdPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String VOTER_SERVICE_SLUG = "voter-id-registration-address-change";

    @Override
    public boolean supports(String serviceSlug) {
        if (serviceSlug == null) return false;
        String s = serviceSlug.toLowerCase().trim();
        return VOTER_SERVICE_SLUG.equalsIgnoreCase(s)
                || "voter-id".equalsIgnoreCase(s)
                || "voter-registration".equalsIgnoreCase(s)
                || "voters-service".equalsIgnoreCase(s)
                || "voter-card".equalsIgnoreCase(s)
                || "epic-card".equalsIgnoreCase(s)
                || "voter-id-registration".equalsIgnoreCase(s);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(VOTER_SERVICE_SLUG);
        dto.setServiceName(service != null ? service.getName() : "Voter ID Registration & Address Change");
        dto.setTitle("Election Commission of India (ECI) Voter Services Navigator");
        dto.setDescription("Answer a few quick questions to identify your exact voter service pathway (Form 6, Form 8 Shifting, Form 8 Correction, Replacement EPIC, e-EPIC Download, or Status Tracking), check statutory qualifying dates, verify documents, and generate your personalized checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Primary Voter Need / Purpose
        questions.add(new QuestionnaireQuestionDto(
                "voterPurpose",
                "What voter service do you require?",
                "Select your primary objective under the Election Commission of India (ECI) framework.",
                null,
                Arrays.asList(
                        new QuestionOptionDto(
                                "NEW_VOTER_REGISTRATION",
                                "New Voter Registration (First-Time Voter — Form 6)",
                                "For Indian citizens who have never been enrolled in any electoral roll and are turning 18.",
                                "Form 6 (Fresh Enrollment)"
                        ),
                        new QuestionOptionDto(
                                "SHIFTING_RESIDENCE",
                                "Shifting of Residence / Address Change (Form 8)",
                                "For registered electors who have moved to a new home (within or outside the Assembly Constituency).",
                                "Form 8 (Address Change)"
                        ),
                        new QuestionOptionDto(
                                "CORRECTION_DETAILS",
                                "Correction of Existing Details in Electoral Roll (Form 8)",
                                "For existing electors needing to correct Name, DOB, Gender, Relation, Relative Name, Photo, or Mobile.",
                                "Form 8 (Correction of Entries)"
                        ),
                        new QuestionOptionDto(
                                "REPLACEMENT_EPIC",
                                "Replacement of Lost, Destroyed, or Damaged Voter ID (Form 8)",
                                "Request a new physical PVC card without modifying any electoral data (Lost requires Police Report).",
                                "Form 8 (Replacement Card)"
                        ),
                        new QuestionOptionDto(
                                "DOWNLOAD_E_EPIC",
                                "Download Digital Voter ID (e-EPIC PDF)",
                                "Instant digital download of official digitally signed voter card containing secure QR code.",
                                "Instant e-EPIC (FREE)"
                        ),
                        new QuestionOptionDto(
                                "CHECK_STATUS_SEARCH",
                                "Check Application Status & Search Electoral Roll",
                                "Track your 11-digit Form 6/8 reference ID or search the active voter list by EPIC, details, or mobile.",
                                "Status & Roll Search"
                        )
                )
        ));

        // Q2: Age & Qualifying Date Status
        questions.add(new QuestionnaireQuestionDto(
                "qualifyingAgeStatus",
                "What is your age or enrollment status?",
                "Under Section 14(b) of RPA 1950, ECI provides 4 annual qualifying dates: 1st Jan, 1st Apr, 1st Jul, 1st Oct.",
                null,
                Arrays.asList(
                        new QuestionOptionDto(
                                "QUALIFIED_18_PLUS",
                                "Already 18 Years or Older (Immediate Enrollment)",
                                "Eligible for immediate inclusion in the active electoral roll for upcoming elections.",
                                "Age 18+ (Immediate)"
                        ),
                        new QuestionOptionDto(
                                "ADVANCE_17_PLUS",
                                "17+ Years Old (Advance Application for Upcoming Qualifying Date)",
                                "Under amended ECI rules, citizens turning 18 can submit Form 6 in advance starting from age 17+.",
                                "Age 17+ (Advance Filing)"
                        ),
                        new QuestionOptionDto(
                                "ALREADY_ENROLLED",
                                "Already Enrolled Elector (Holds EPIC Number / on Voter List)",
                                "Applies to address changes, corrections, replacement cards, or digital downloads.",
                                "Existing Registered Voter"
                        )
                )
        ));

        // Q3: Specific Condition / Replacement Reason (for Form 8 & Corrections)
        questions.add(new QuestionnaireQuestionDto(
                "conditionDetails",
                "If updating or replacing an existing Voter ID, what is the specific reason?",
                "Determines whether an FIR/Police Lost Report, proof of change, or surrender of old card is mandated.",
                null,
                Arrays.asList(
                        new QuestionOptionDto(
                                "STANDARD_OR_NEW",
                                "Not Applicable / Standard Request (New Registration or Digital Download)",
                                "Standard processing without special damage or loss documentation.",
                                "Standard Workflow"
                        ),
                        new QuestionOptionDto(
                                "LOST_STOLEN_EPIC",
                                "Lost or Stolen Voter ID Card (Mandatory Police Complaint / FIR)",
                                "ECI mandates attaching an official Police Lost Article Report or FIR copy for lost cards.",
                                "Police Report Required"
                        ),
                        new QuestionOptionDto(
                                "DESTROYED_OR_MUTILATED",
                                "Destroyed (Natural Calamity / Fire / Flood) or Physically Mutilated",
                                "No FIR required for natural disasters; mutilated cards must be physically surrendered.",
                                "No FIR / Card Surrender"
                        ),
                        new QuestionOptionDto(
                                "CORRECTING_NAME_OR_DOB",
                                "Correcting Name, Relative Name, or Date of Birth",
                                "Requires statutory documentary evidence (10th marksheet, birth cert, or Gazette notification).",
                                "Demographic Proof Required"
                        ),
                        new QuestionOptionDto(
                                "PHOTO_MOBILE_UPDATE",
                                "Updating Photograph or Mobile Number Linking",
                                "Essential for e-EPIC digital download enablement and clean photo printing on new PVC card.",
                                "Digital Enablement"
                        )
                )
        ));

        // Q4: State Jurisdiction Focus
        questions.add(new QuestionnaireQuestionDto(
                "stateJurisdiction",
                "What is your target State jurisdiction?",
                "ECI Voter Services Portal serves all of India; specific regional assistance like Sachivalayams applies in AP.",
                null,
                Arrays.asList(
                        new QuestionOptionDto(
                                "ANDHRA_PRADESH",
                                "Andhra Pradesh (CEO Andhra Pradesh / Sachivalayam Support)",
                                "Includes local assistance through Village / Ward Secretariats (Grama Ward Sachivalayam) and MeeSeva.",
                                "Andhra Pradesh (Priority)"
                        ),
                        new QuestionOptionDto(
                                "ALL_INDIA_OTHER_STATES",
                                "Other States / Union Territories of India (Standard ECI Portal)",
                                "Processed through standard Election Commission of India national workflow.",
                                "Pan-India National"
                        )
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();

        String purpose = answers.getOrDefault("voterPurpose", "NEW_VOTER_REGISTRATION");
        String ageStatus = answers.getOrDefault("qualifyingAgeStatus", "QUALIFIED_18_PLUS");
        String condition = answers.getOrDefault("conditionDetails", "STANDARD_OR_NEW");
        String state = answers.getOrDefault("stateJurisdiction", "ANDHRA_PRADESH");

        boolean isAp = "ANDHRA_PRADESH".equalsIgnoreCase(state);
        String apNote = isAp
                ? " In Andhra Pradesh, applicants can also seek physical assistance through their local Village/Ward Secretariat (Grama Ward Sachivalayam) or visit the Chief Electoral Officer AP portal (ceoandhra.nic.in)."
                : "";

        List<ServiceStepDto> steps = new ArrayList<>();
        List<ServiceRequiredDocumentDto> documents = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        // -------------------------------------------------------------
        // PATHWAY 1: NEW VOTER REGISTRATION (FORM 6)
        // -------------------------------------------------------------
        if ("NEW_VOTER_REGISTRATION".equalsIgnoreCase(purpose)) {
            result.setPathwayCode("NEW_VOTER_REGISTRATION_FORM_6");
            result.setPathwayTitle("New Voter Registration & Electoral Roll Enrollment (Form 6)");
            result.setBadgeText("ECI Form 6 • 100% Free • Statutory Enrollment");
            result.setCalculatedFee("₹0.00 (Statutory Free of Cost)");
            result.setFeeBreakdown("Government Application Fee: ₹0.00 | New High-Security PVC EPIC Card & Speed Post Dispatch: ₹0.00 (Completely Free under Election Commission of India)");
            result.setEstimatedTimeline("15 to 30 Working Days (Includes BLO Field Verification & Electoral Roll Publication)");
            result.setMatchReason("Under Section 19 of the Representation of the People Act, 1950, every Indian citizen who has reached 18 years of age (or is turning 18 on any of the 4 statutory qualifying dates: 1 Jan, 1 Apr, 1 Jul, 1 Oct) is entitled to enroll in the electoral roll of their ordinary residence. Form 6 is the official application. Once approved, you receive a free PVC Voter ID card via India Post Speed Post." + apNote);
            result.setOfficialPortalUrl("https://voters.eci.gov.in/");

            steps.add(new ServiceStepDto(
                    1,
                    "Create Account on ECI Voters' Services Portal",
                    "Visit the official portal at https://voters.eci.gov.in/ (or download the official Voter Helpline App on Android/iOS). Register using your active Indian mobile number and verify via OTP to generate your login credentials.",
                    "5–10 Minutes",
                    "Always use an active personal mobile number; this number will be permanently linked to your Voter ID for e-EPIC downloads."
            ));
            steps.add(new ServiceStepDto(
                    2,
                    "Fill Online Form 6 & Select Assembly Constituency",
                    "Click on 'New registration for general electors (Form 6)'. Select your State (e.g. Andhra Pradesh), District, and Assembly Constituency (AC). Fill in your personal details (Name, Date of Birth, Gender), Father's/Mother's/Spouse's Name, and complete residential address.",
                    "15–20 Minutes",
                    "Ensure your name and date of birth match your Class 10 marksheet or Birth Certificate exactly."
            ));
            steps.add(new ServiceStepDto(
                    3,
                    "Upload Supporting Documents & Color Photograph",
                    "Upload self-attested digital copies of: (1) Proof of Age (Birth Certificate, Aadhaar, PAN, DL, or 10th marksheet), (2) Proof of Ordinary Residence (Aadhaar, Utility Bill, Bank Passbook, or Rent Deed), and (3) Recent passport-sized color photograph with white background (<2MB JPG).",
                    "5–10 Minutes",
                    "Self-attest documents by signing on the photocopies before scanning or uploading."
            ));
            steps.add(new ServiceStepDto(
                    4,
                    "Review, Submit & Save 11-Digit Reference ID",
                    "Preview the completed Form 6 for 100% factual accuracy. Submit the form online. Immediately save and print the auto-generated 11-digit Reference ID acknowledgement slip.",
                    "2 Minutes",
                    "The 11-digit Reference ID is essential for tracking your application status online."
            ));
            steps.add(new ServiceStepDto(
                    5,
                    "Booth Level Officer (BLO) Physical Field Verification",
                    "The Electoral Registration Officer (ERO) assigns your application to your designated Booth Level Officer (BLO). The BLO will physically visit your residence to verify your identity, verify original supporting documents, and confirm ordinary residence.",
                    "7–15 Days",
                    "Keep original documents ready at home. Submitting an application does not guarantee automatic inclusion; BLO verification and ERO statutory approval are mandatory."
            ));
            steps.add(new ServiceStepDto(
                    6,
                    "Electoral Roll Inclusion & Free PVC Card Speed Post Delivery",
                    "Upon ERO approval, your name is enrolled in the supplementary electoral roll and an official 10-character EPIC Number is generated. You can immediately download your digital e-EPIC PDF. A high-security PVC Voter Card with hologram is delivered via India Post Speed Post free of charge within 30 days.",
                    "Within 30 Days",
                    "India Post delivery of the PVC card is completely free; no postal charges are payable to the postman."
            ));

            // Documents
            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Proof of Date of Birth & Age (Birth Certificate / Aadhaar / 10th Marksheet / PAN / DL)",
                    "Statutory document proving date of birth and age eligibility (18+ on qualifying date).",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official government or educational record verifying your exact date of birth.\n\nACCEPTED DOCUMENTS: Birth Certificate issued by Municipal Authority/Registrar of Births, Aadhaar Card, PAN Card, Driving Licence, or Class 10th / Matriculation Marksheet/Certificate showing DOB.\n\nWHERE TO OBTAIN: Digilocker, municipal birth registrar, school board, or respective issuing authorities.\n\nHOW TO OBTAIN: Scan an original document or download a verified digital copy from DigiLocker. Ensure your name and parent's name match your application.\n\nCOMMON MISTAKES: Submitting an affidavit without supporting statutory certificates, or submitting documents with mismatched birth dates."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Proof of Ordinary Residence (Aadhaar / Utility Bill / Bank Passbook / Indian Passport / Rent Agreement)",
                    "Document proving ordinary residence in the applied Assembly Constituency.",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official record establishing that you ordinarily reside at the specified residential address within the Assembly Constituency.\n\nACCEPTED DOCUMENTS: Latest Water/Electricity/Gas connection bill (under 3 months old in applicant's, parent's, or spouse's name), Aadhaar Card, Current Bank/Post Office Passbook, Indian Passport, or Registered Rent/Lease Deed.\n\nWHERE TO OBTAIN: Electricity board, municipal corporation, bank branch, or UIDAI.\n\nHOW TO OBTAIN: Obtain the latest original bill or passbook statement. If renting, ensure the rent agreement is legally registered.\n\nCOMMON MISTAKES: Submitting temporary hostel addresses without parental address proof, or using utility bills older than 3 months."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Recent Color Passport-Sized Photograph (White Background)",
                    "Recent color portrait photo taken within the last 3 months with a clean white/light background.",
                    true,
                    "JPG / JPEG (3.5 cm x 4.5 cm, Max 2MB)",
                    "WHAT IT IS: Formal passport photograph printed on your physical PVC Voter ID card and electoral roll.\n\nSPECIFICATIONS: 3.5 cm x 4.5 cm color photo, front-facing, neutral expression, eyes open, ears clearly visible, clean white background. No sunglasses, uniforms, or caps.\n\nWHERE TO OBTAIN: Any photo studio or high-resolution smartphone camera with good lighting against a plain white wall.\n\nCOMMON MISTAKES: Uploading casual selfies, low-resolution social media crops, or photos with dark/busy backgrounds."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Family Member Existing Voter ID / EPIC Number (Optional Reference)",
                    "Existing EPIC number of family member (father, mother, spouse, or sibling) residing at the same address.",
                    false,
                    "10-Character EPIC Number",
                    "WHAT IT IS: The Voter ID number of an immediate family member who is already enrolled at the exact same residential address.\n\nPURPOSE: Helps the ECI system and Booth Level Officer (BLO) automatically map you to the exact same Polling Station, Part Number, and Section as your family.\n\nWHERE TO OBTAIN: Look up on family member's physical Voter Card or search electoralsearch.eci.gov.in."
            ));

            tips.add("Under amended Section 14(b) of RPA 1950, there are 4 qualifying dates every year: 1st January, 1st April, 1st July, and 1st October. You can apply in advance from age 17+.");
            tips.add("Submitting an application does not guarantee automatic inclusion. Inclusion strictly depends on physical verification by the Booth Level Officer (BLO) and statutory approval by the Electoral Registration Officer (ERO).");
            tips.add("Possessing more than one Voter ID card or enrolling in multiple constituencies is a criminal offense under Section 31 of the Representation of the People Act, 1950 (punishable with imprisonment up to 1 year and fine).");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // -------------------------------------------------------------
        // PATHWAY 2: SHIFTING OF RESIDENCE / ADDRESS CHANGE (FORM 8)
        // -------------------------------------------------------------
        if ("SHIFTING_RESIDENCE".equalsIgnoreCase(purpose)) {
            result.setPathwayCode("SHIFTING_RESIDENCE_FORM_8");
            result.setPathwayTitle("Shifting of Residence & Address Change (Form 8)");
            result.setBadgeText("ECI Form 8 • Within or Outside Constituency • Free PVC Card");
            result.setCalculatedFee("₹0.00 (Statutory Free of Cost)");
            result.setFeeBreakdown("Statutory Address Update: ₹0.00 | Replacement PVC Card Dispatch: ₹0.00 (Free of cost by Government of India)");
            result.setEstimatedTimeline("15 to 21 Working Days (Auto-triggers old polling station deletion)");
            result.setMatchReason("When an already registered elector moves to a new residence—whether within the same Assembly Constituency or to a different Constituency/District/State—they must submit Form 8 for Shifting of Residence. IMPORTANT STATUTORY RULE: Shifting to a new constituency automatically triggers deletion from the old polling station list upon successful enrollment at the new location (you do NOT need to file Form 7 separately)." + apNote);
            result.setOfficialPortalUrl("https://voters.eci.gov.in/");

            steps.add(new ServiceStepDto(
                    1,
                    "Access Form 8 on ECI Voters' Services Portal",
                    "Log in to https://voters.eci.gov.in/ using your registered mobile number. Under the Forms menu, select 'Application for Shifting of Residence / Correction of Entries (Form 8)'. Select 'Self' if applying for your own card.",
                    "3 Minutes",
                    "Make sure your mobile number is active to receive the verification OTP."
            ));
            steps.add(new ServiceStepDto(
                    2,
                    "Select Shifting Type & Provide Existing EPIC Number",
                    "Select the first option: 'Shifting of Residence'. Next, choose whether your shift is 'Within Assembly Constituency' or 'Outside Assembly Constituency'. Enter your existing 10-character EPIC Number to retrieve your current electoral roll records.",
                    "3 Minutes",
                    "Your name, gender, relative name, and old polling station details will auto-populate from the ECI national database."
            ));
            steps.add(new ServiceStepDto(
                    3,
                    "Enter Complete New Residential Address",
                    "Enter your complete new residential address: House/Door/Flat No., Street/Locality, Town/Village, Post Office, PIN Code, State (e.g. Andhra Pradesh), District, and the new Assembly Constituency name/number.",
                    "10 Minutes",
                    "Ensure the Door Number and Locality match your supporting address proof exactly."
            ));
            steps.add(new ServiceStepDto(
                    4,
                    "Upload New Address Proof Document",
                    "Attach a valid supporting document reflecting your new address (Aadhaar Card, utility bill, bank passbook, or registered rent agreement in your name, parent's name, or spouse's name).",
                    "5 Minutes",
                    "The proof must clearly show the applicant's name and new residential address."
            ));
            steps.add(new ServiceStepDto(
                    5,
                    "Submit Form 8 & Download Acknowledgment Slip",
                    "Review the application summary, verify accuracy, and submit. Download the acknowledgment slip containing your 11-digit Reference ID.",
                    "2 Minutes",
                    "Save the reference ID to monitor progress on the 'Track Application Status' page."
            ));
            steps.add(new ServiceStepDto(
                    6,
                    "BLO Verification at New Residence & Automatic Deletion from Old List",
                    "The Booth Level Officer (BLO) of the new polling station conducts a verification check at your new home. Once approved by the new ERO, your record is automatically transferred, your old polling station entry is deleted, and a new PVC Voter Card is dispatched via Speed Post.",
                    "15–21 Days",
                    "You will receive an SMS confirmation once the address transfer is finalized."
            ));

            // Documents
            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Existing Voter ID (EPIC) Number",
                    "10-character alphanumeric Electoral Photo Identity Card (EPIC) number currently allotted to you.",
                    true,
                    "10-Character Alphanumeric Code",
                    "WHAT IT IS: Your unique voter identification number (e.g., ABC1234567) printed on the front of your existing Voter Card.\n\nWHERE TO FIND IT: On your existing physical Voter Card, previous Voter Information Slip, or search by details at electoralsearch.eci.gov.in.\n\nHOW TO USE: Enter in Form 8 to automatically fetch your current polling station details."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Proof of New Ordinary Residence (Aadhaar / Utility Bill / Bank Passbook / Rent Agreement)",
                    "Document proving actual residence at the new address within the target constituency.",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official document establishing your relocation to the new address.\n\nACCEPTED PROOFS: Latest Electricity/Water/LPG bill (under 3 months old), updated Aadhaar Card, Current Bank Passbook, Indian Passport, or Registered Rent/Lease Deed.\n\nWHERE TO OBTAIN: Respective utility providers, bank branch, or UIDAI.\n\nCONDITIONS: Must be in the name of the applicant, parent, or spouse. If in spouse's or parent's name, relationship must be verifiable."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Recent Passport-Sized Color Photograph (Optional / Recommended)",
                    "Optional fresh color photo to replace outdated photograph on the new PVC card.",
                    false,
                    "JPG / JPEG (<2MB)",
                    "WHAT IT IS: Recent photo (3.5 x 4.5 cm, white background). If your old photo is outdated, upload a fresh portrait to ensure a clear image on your new replacement card."
            ));

            tips.add("CRITICAL RULE: Shifting of residence automatically deletes your name from the old polling station list upon successful enrollment at your new residence. You do NOT need to fill Form 7 separately.");
            tips.add("Never apply for a fresh Voter ID (Form 6) when shifting homes; always use Form 8 to transfer your existing EPIC number. Creating duplicate Voter IDs is punishable by law.");
            tips.add("Once approved, you can immediately download your updated e-EPIC PDF from voters.eci.gov.in using your existing EPIC number.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // -------------------------------------------------------------
        // PATHWAY 3: CORRECTION OF EXISTING DETAILS (FORM 8)
        // -------------------------------------------------------------
        if ("CORRECTION_DETAILS".equalsIgnoreCase(purpose)) {
            result.setPathwayCode("CORRECTION_ENTRIES_FORM_8");
            result.setPathwayTitle("Correction of Entries in Existing Electoral Roll (Form 8)");
            result.setBadgeText("ECI Form 8 • Up to 4 Fields per Application • Free Card");
            result.setCalculatedFee("₹0.00 (Statutory Free of Cost)");
            result.setFeeBreakdown("Correction Processing: ₹0.00 | Updated PVC Card & Speed Post Dispatch: ₹0.00 (Free of cost by Government of India)");
            result.setEstimatedTimeline("15 to 21 Working Days");
            result.setMatchReason("If an existing voter's name, relative's name, relation type, date of birth, gender, photograph, or mobile number is incorrectly recorded in the electoral roll or printed wrongly on their EPIC card, Form 8 allows correcting up to 4 fields in a single application with documentary evidence." + apNote);
            result.setOfficialPortalUrl("https://voters.eci.gov.in/");

            steps.add(new ServiceStepDto(
                    1,
                    "Initiate Form 8 Correction Request",
                    "Log in to https://voters.eci.gov.in/ and select 'Form 8 (Shifting of Residence / Correction of Entries)'. Select 'Self' and choose the second option: 'Correction of Entries in Existing Electoral Roll'.",
                    "3 Minutes",
                    "Provide your existing 10-character EPIC Number to retrieve your active electoral entry."
            ));
            steps.add(new ServiceStepDto(
                    2,
                    "Select Specific Fields to Correct (Max 4 Fields)",
                    "Select the checkboxes corresponding to the details needing correction: Name, Gender, Date of Birth, Relation Type, Relative's Name, Mobile Number, or Photograph (ECI permits a maximum of 4 attributes per application).",
                    "5 Minutes",
                    "You cannot change Address under the Correction option; for address change, use the 'Shifting of Residence' option."
            ));
            steps.add(new ServiceStepDto(
                    3,
                    "Enter Correct Information & Upload Documentary Proof",
                    "Input the exact corrected values. Upload valid supporting documentation matching each corrected field (e.g., 10th marksheet for DOB, Gazette notification or Marriage certificate for Name change, or fresh portrait for photo update).",
                    "10 Minutes",
                    "Ensure the documentary proof clearly shows the correct spelling and details."
            ));
            steps.add(new ServiceStepDto(
                    4,
                    "Submit Form 8 & Track Progress via Reference ID",
                    "Review all modified fields, accept the self-declaration, and submit. Download your acknowledgment receipt containing your 11-digit Reference ID.",
                    "2 Minutes",
                    "Use the 'Track Application Status' tab on voters.eci.gov.in to follow the ERO approval stages."
            ));
            steps.add(new ServiceStepDto(
                    5,
                    "ERO Approval & Free PVC Card Delivery",
                    "The Electoral Registration Officer (ERO) verifies the uploaded evidence against statutory rules. Upon approval, the electoral roll is amended, and a new high-security PVC card with corrected details is sent via Speed Post free of charge.",
                    "15–21 Days",
                    "Once approved, you can immediately download your corrected e-EPIC PDF."
            ));

            // Documents
            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Existing Voter ID (EPIC) Number",
                    "10-character alphanumeric EPIC number of the voter record requiring correction.",
                    true,
                    "10-Character Alphanumeric Code",
                    "Your current Voter ID number used to locate the specific record in the National Electoral Database."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Documentary Proof Supporting Requested Correction",
                    "Statutory proof establishing the correct detail (Marksheet, Birth Certificate, Gazette, or Marriage Certificate).",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official government or educational record verifying the exact corrected data.\n\nFOR NAME CORRECTION: Official Gazette notification, Marriage Certificate (for surname change post-marriage), or Passport.\n\nFOR DOB CORRECTION: Birth Certificate, Class 10th marksheet, or Aadhaar Card.\n\nFOR RELATIVE NAME: School leaving certificate or family ration card."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Recent Color Passport-Sized Photograph (Mandatory if updating photo)",
                    "Recent color portrait (3.5 x 4.5 cm, white background) if photograph correction is selected.",
                    "PHOTO_MOBILE_UPDATE".equalsIgnoreCase(condition),
                    "JPG / JPEG (<2MB)",
                    "Upload a fresh studio-quality portrait with neutral expression against a plain white background."
            ));

            tips.add("ECI allows a maximum of 4 fields to be corrected in a single Form 8 application.");
            tips.add("Linking your unique mobile number through Form 8 is essential for enabling instant digital e-EPIC PDF downloads.");
            tips.add("Do not submit Form 6 for corrections; Form 6 is strictly for brand new voter registration.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // -------------------------------------------------------------
        // PATHWAY 4: REPLACEMENT OF LOST OR DAMAGED VOTER ID (FORM 8)
        // -------------------------------------------------------------
        if ("REPLACEMENT_EPIC".equalsIgnoreCase(purpose)) {
            result.setPathwayCode("REPLACEMENT_EPIC_FORM_8");
            result.setPathwayTitle("Issue of Replacement EPIC Card without Correction (Form 8)");
            result.setBadgeText("ECI Form 8 • Replacement Card • Lost / Damaged / Mutilated");
            result.setCalculatedFee("₹0.00 (Statutory Free of Cost)");
            result.setFeeBreakdown("Replacement Card Processing: ₹0.00 | High-Security PVC Card & Speed Post Delivery: ₹0.00 (Completely Free under ECI)");
            result.setEstimatedTimeline("15 to 21 Working Days");
            result.setMatchReason("When an existing Voter ID card is lost, stolen, destroyed due to natural calamities (fire, flood, earthquake), or physically mutilated/defaced, a replacement card can be requested without altering any electoral roll data using Form 8. STATUTORY MANDATE: For lost cards, an official copy of the Police Complaint / FIR / Online Lost Article Report is mandatory." + apNote);
            result.setOfficialPortalUrl("https://voters.eci.gov.in/");

            steps.add(new ServiceStepDto(
                    1,
                    "Lodge Police Lost Report (Mandatory ONLY if Card is Lost)",
                    "If your Voter ID was lost or stolen, lodge an online Lost Article Report / NCR on your State Police Portal (e.g. AP Police Citizen Portal at appolice.gov.in or local cyber kiosk) or file an FIR at your local police station. Download the stamped/digitally signed copy.",
                    "1 Day",
                    "For destroyed (fire/flood) or mutilated cards, no police complaint is required."
            ));
            steps.add(new ServiceStepDto(
                    2,
                    "Select Replacement Option in Form 8 on ECI Portal",
                    "Log in to https://voters.eci.gov.in/ → Open Form 8 → Select the third option: 'Issue of Replacement EPIC without Correction'. Enter your existing EPIC Number to fetch your records.",
                    "3 Minutes",
                    "All your existing demographic and constituency particulars will be displayed."
            ));
            steps.add(new ServiceStepDto(
                    3,
                    "Choose Replacement Reason & Attach Required Document",
                    "Select the exact reason: (a) Lost (attach copy of Police FIR / Lost Report), (b) Destroyed due to reason beyond control like floods/fire, or (c) Mutilated / Torn (the damaged card must be surrendered to the BLO).",
                    "5 Minutes",
                    "Ensure the police report clearly mentions 'Loss of Voter ID Card (EPIC) Number: [Your Number]'."
            ));
            steps.add(new ServiceStepDto(
                    4,
                    "Submit Form 8 & Save 11-Digit Reference ID",
                    "Accept the legal declaration, submit the application, and download the acknowledgement receipt containing your 11-digit Reference ID.",
                    "2 Minutes",
                    "Save the reference ID for online tracking."
            ));
            steps.add(new ServiceStepDto(
                    5,
                    "Verification & Speed Post Delivery of Replacement PVC Card",
                    "The ERO verifies your record against the electoral database. A new high-security PVC Voter Card with hologram and micro-printing is printed and dispatched directly to your registered address via India Post Speed Post free of charge.",
                    "15–21 Days",
                    "You will receive an India Post tracking number via SMS once the card is dispatched."
            ));

            // Documents
            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Existing Voter ID (EPIC) Number",
                    "10-character alphanumeric EPIC number of the card to be replaced.",
                    true,
                    "10-Character Alphanumeric Code",
                    "Printed on your previous card or searchable at electoralsearch.eci.gov.in."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Police FIR / Online Lost Article Report Copy (Mandatory ONLY for Lost Cards)",
                    "Official police acknowledgement verifying reporting of the lost Voter ID card.",
                    "LOST_STOLEN_EPIC".equalsIgnoreCase(condition),
                    "Signed PDF / Police Stamp Copy",
                    "WHAT IT IS: Official report filed with local police station or via State Police Citizen Portal reporting the loss of your Voter ID.\n\nWHERE TO OBTAIN: Online citizen portal of State Police (e.g. AP Police Citizen Services) or physical police station.\n\nCONDITIONS: Mandatory ONLY if the replacement reason selected is 'Lost'. Not required if destroyed by fire/flood or mutilated."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Identity Proof (Aadhaar Card / PAN Card / Driving License)",
                    "Government photo ID verifying identity of the applicant requesting replacement.",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "Aadhaar Card, PAN Card, Driving Licence, or Passport verifying applicant identity."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Mutilated / Defaced Physical Card (For physical surrender if mutilated)",
                    "Existing damaged or defaced card to be surrendered to the Booth Level Officer (BLO).",
                    "DESTROYED_OR_MUTILATED".equalsIgnoreCase(condition),
                    "Physical Card Surrender",
                    "Mandatory ONLY if 'Mutilated' is selected. Hand over the defaced plastic/laminated card to the BLO upon request."
            ));

            tips.add("For lost Voter ID cards, an FIR or online Lost Article Report from the police is legally mandatory under ECI guidelines to prevent card misuse.");
            tips.add("Replacement of a lost or mutilated card is completely free of cost; no fees are payable to the Election Commission or India Post.");
            tips.add("While waiting for the physical replacement card, you can download your official digital e-EPIC PDF immediately from voters.eci.gov.in.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // -------------------------------------------------------------
        // PATHWAY 5: DOWNLOAD DIGITAL VOTER ID (e-EPIC)
        // -------------------------------------------------------------
        if ("DOWNLOAD_E_EPIC".equalsIgnoreCase(purpose)) {
            result.setPathwayCode("DOWNLOAD_DIGITAL_E_EPIC");
            result.setPathwayTitle("Download Digital Voter ID (e-EPIC PDF via ECI Portal)");
            result.setBadgeText("Instant Digital PDF • Secure QR Code • Equal Legal Validity");
            result.setCalculatedFee("₹0.00 (Statutory 100% Free)");
            result.setFeeBreakdown("Government Digital e-EPIC Download: ₹0.00 (Instant Free PDF with Secure QR Code)");
            result.setEstimatedTimeline("Instant (2 to 5 Minutes)");
            result.setMatchReason("e-EPIC is a secure, non-editable, digitally signed PDF version of the Electoral Photo Identity Card (EPIC) issued by the Election Commission of India. It contains a high-security QR code with image and demographic data. Under ECI directives, e-EPIC carries equal legal validity as the physical PVC card and can be downloaded instantly by electors with a linked mobile number." + apNote);
            result.setOfficialPortalUrl("https://voters.eci.gov.in/");

            steps.add(new ServiceStepDto(
                    1,
                    "Access e-EPIC Download on ECI Portal",
                    "Visit the official portal at https://voters.eci.gov.in/ and click on the 'e-EPIC Download' icon on the dashboard (or use the Voter Helpline App).",
                    "1 Minute",
                    "Ensure you are using the official government domain: voters.eci.gov.in."
            ));
            steps.add(new ServiceStepDto(
                    2,
                    "Login or Register with Mobile Number",
                    "Sign in with your registered mobile number and password. If new to the portal, complete 1-minute registration using your mobile number and OTP.",
                    "2 Minutes",
                    "Login is mandatory to access the e-EPIC download service."
            ));
            steps.add(new ServiceStepDto(
                    3,
                    "Enter EPIC Number or Form Reference Number",
                    "Select 'EPIC no.' or 'Form Reference no.'. Enter your 10-character EPIC number (or 11-digit Form Reference ID), select your State (e.g. Andhra Pradesh), and click 'Search'.",
                    "1 Minute",
                    "Your name, relative name, assembly constituency, and masked mobile number (e.g. XXXXXXX123) will appear."
            ));
            steps.add(new ServiceStepDto(
                    4,
                    "Authenticate via One-Time Password (OTP)",
                    "Click 'Send OTP'. Enter the 6-digit verification OTP received on your registered mobile number and click 'Verify'.",
                    "1 Minute",
                    "If your mobile number is not registered, the system will prompt you to update mobile number via Form 8 first."
            ));
            steps.add(new ServiceStepDto(
                    5,
                    "Download & Store Digitally Signed e-EPIC PDF",
                    "Click 'Download e-EPIC'. The system generates a high-resolution, digitally signed PDF file containing your photograph, serial number, and encrypted QR code. Save to your device or DigiLocker.",
                    "Instant",
                    "e-EPIC is completely valid for voting at your polling booth and as statutory identity proof nationwide."
            ));

            // Documents
            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Voter ID (EPIC) Number or 11-Digit Form Reference Number",
                    "10-character alphanumeric EPIC number or newly allotted Form Reference ID.",
                    true,
                    "10-Character Alphanumeric Code",
                    "Found on existing Voter ID, previous voting slip, or application acknowledgment."
            ));

            documents.add(new ServiceRequiredDocumentDto(
                    null,
                    "Registered Mobile Number with SMS/OTP Capability",
                    "Active mobile phone linked with your electoral roll record to receive authentication OTP.",
                    true,
                    "Mobile SMS Verification",
                    "Must be linked in the ECI roll. If not linked, submit Form 8 to link mobile in 7–10 days."
            ));

            tips.add("e-EPIC is a portable, digitally signed PDF with a secure QR code that carries 100% equal legal validity as a physical PVC card under ECI directives.");
            tips.add("If the portal shows 'Mobile number is not registered on electoral roll', submit Form 8 (Correction of Entries -> Mobile Number). Once approved, you can download e-EPIC immediately.");
            tips.add("You can also save and display your e-EPIC inside the Government of India DigiLocker app.");

            result.setSteps(steps);
            result.setDocuments(documents);
            result.setCriticalTips(tips);
            return result;
        }

        // -------------------------------------------------------------
        // PATHWAY 6: CHECK VOTER STATUS & SEARCH ELECTORAL ROLL
        // -------------------------------------------------------------
        result.setPathwayCode("CHECK_STATUS_ELECTORAL_SEARCH");
        result.setPathwayTitle("Check Voter Application Status & Electoral Roll Search");
        result.setBadgeText("Online Tracking • Electoral Search • Polling Station Locator");
        result.setCalculatedFee("₹0.00 (Statutory 100% Free)");
        result.setFeeBreakdown("Real-Time Application Status Tracking & Electoral Roll Search: ₹0.00");
        result.setEstimatedTimeline("Instant Real-Time Lookup");
        result.setMatchReason("Enables citizens to track the real-time processing status of submitted Form 6 or Form 8 applications, or verify whether their name is currently enrolled in the active electoral roll (voter list), locate their designated polling station, part number, and serial number." + apNote);
        result.setOfficialPortalUrl("https://electoralsearch.eci.gov.in/");

        steps.add(new ServiceStepDto(
                1,
                "Track Submitted Application Status (Form 6 or Form 8)",
                "Visit https://voters.eci.gov.in/ and click 'Track Application Status'. Enter your 11-digit Reference ID and select your State (e.g. Andhra Pradesh). View the 4 progress stages: Submitted → BLO Appointed → Field Verified → Accepted/Rejected.",
                "2 Minutes",
                "If rejected, the reason for rejection will be clearly stated along with the ERO remarks."
        ));
        steps.add(new ServiceStepDto(
                2,
                "Search Active Electoral Roll on electoralsearch.eci.gov.in",
                "To check if your name is already enrolled, visit the official National Electoral Search portal at https://electoralsearch.eci.gov.in/ (or CEO Andhra Pradesh portal at ceoandhra.nic.in).",
                "1 Minute",
                "The portal provides 3 search options: By Details, By EPIC, or By Mobile Number."
        ));
        steps.add(new ServiceStepDto(
                3,
                "Search by EPIC Number (Fastest Method)",
                "Select 'Search by EPIC'. Enter your 10-character EPIC number, select State, complete the Captcha, and click 'Search'.",
                "1 Minute",
                "Instantly displays your active Polling Station, Part Number, Serial Number, and Parliamentary/Assembly Constituency."
        ));
        steps.add(new ServiceStepDto(
                4,
                "Search by Personal Details (If EPIC Number is Unknown)",
                "If you don't know your EPIC number, select 'Search by Details'. Input Name, Relative's Name, Date of Birth or Age, Gender, State, District, and Assembly Constituency.",
                "3 Minutes",
                "Ensure spellings resemble common phonetics used during original registration."
        ));
        steps.add(new ServiceStepDto(
                5,
                "Download Voter Information Slip (VIS)",
                "Click 'View Details' on your search result. Click 'Print / Download Voter Information Slip'. This slip displays your Polling Station location, Room/Booth Number, Part/Serial numbers, and BLO contact details.",
                "Instant",
                "The Voter Information Slip helps you locate your exact voting queue at your polling booth on voting day."
        ));

        // Documents
        documents.add(new ServiceRequiredDocumentDto(
                null,
                "11-Digit Form Reference Number (For Tracking Pending Applications)",
                "Auto-generated reference number from your Form 6 or Form 8 acknowledgment slip.",
                false,
                "11-Digit Reference Code",
                "Issued upon online submission of Form 6 or Form 8."
        ));

        documents.add(new ServiceRequiredDocumentDto(
                null,
                "Voter ID (EPIC) Number (For Roll Search by EPIC)",
                "10-character alphanumeric Voter ID number.",
                false,
                "10-Character Alphanumeric Code",
                "Found on existing Voter ID or previous voting slips."
        ));

        documents.add(new ServiceRequiredDocumentDto(
                null,
                "Registered Mobile Number or Exact Personal Demographic Details",
                "Mobile number linked to voter record or exact Name, Age, and Relative Name.",
                false,
                "Mobile OTP or Demographic Inputs",
                "Used for searching by mobile or searching by demographic details on electoralsearch.eci.gov.in."
        ));

        tips.add("Always verify your name in the electoral roll at least 3 weeks before polling day; possessing a physical card alone does not guarantee voting if your name is not on the active electoral roll.");
        tips.add("The Voter Information Slip (VIS) downloaded from electoralsearch.eci.gov.in specifies your exact Polling Station name, address, part number, and serial number.");
        tips.add("For unresolved delays or field verification issues, dial the national Election Helpline toll-free at 1950.");

        result.setSteps(steps);
        result.setDocuments(documents);
        result.setCriticalTips(tips);
        return result;
    }
}
