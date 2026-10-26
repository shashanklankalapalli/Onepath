package com.onepath.service.evaluator;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class PassportPathwayEvaluator implements ServicePathwayEvaluator {

    public static final String PASSPORT_SLUG = "passport-application-renewal";

    @Override
    public boolean supports(String serviceSlug) {
        return PASSPORT_SLUG.equalsIgnoreCase(serviceSlug) || "passport".equalsIgnoreCase(serviceSlug);
    }

    @Override
    public QuestionnaireDto getQuestionnaire(Service service) {
        QuestionnaireDto dto = new QuestionnaireDto();
        dto.setServiceSlug(PASSPORT_SLUG);
        dto.setServiceName(service != null ? service.getName() : "Passport Application & Renewal");
        dto.setTitle("Passport Situation & Eligibility Questionnaire");
        dto.setDescription("Answer these brief questions about your situation, living arrangement, and urgency to receive your personalized passport pathway, exact fees, and tailored checklist.");

        List<QuestionnaireQuestionDto> questions = new ArrayList<>();

        // Q1: Application Type
        questions.add(new QuestionnaireQuestionDto(
                "applicationType",
                "What type of passport application are you making?",
                "Select whether this is your first passport or if you are renewing/re-issuing an existing passport.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("FRESH", "Fresh Passport (First-Time Issue)", "For citizens applying for an ordinary Indian passport for the first time."),
                        new QuestionOptionDto("RENEWAL", "Passport Renewal / Re-issue", "For existing passport holders whose booklet is expiring/expired, pages exhausted, or details have changed.")
                )
        ));

        // Q2: Applicant Category & Residence
        questions.add(new QuestionnaireQuestionDto(
                "applicantCategory",
                "What is your applicant category and residential status?",
                "University students living in hostels/PGs qualify for special address documentation rules under MEA guidelines.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("COLLEGE_STUDENT_HOSTEL", "College / University Student living in Hostel or PG", "Enables applying under campus address using Student ID, Bonafide Certificate, and Hostel Warden Letter without local utility bills.", "Special Student Provision"),
                        new QuestionOptionDto("ADULT", "General Adult Citizen (Age 18 and above)", "Standard adult applicant living with family or independent residence.", "Standard Issue"),
                        new QuestionOptionDto("MINOR", "Minor Applicant (Under 18 Years)", "Under 18 years of age. Standard validity is 5 years or until age 18.", "Minor Category")
                )
        ));

        // Q3: Processing Scheme
        questions.add(new QuestionnaireQuestionDto(
                "scheme",
                "Which processing scheme do you require?",
                "Normal follows the standard queue; Tatkaal is an urgent expedited track with post-verification.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("NORMAL", "Normal Scheme (Standard)", "Standard processing: 7–15 working days post-police verification."),
                        new QuestionOptionDto("TATKAAL", "Tatkaal Scheme (Fast-Track Urgent)", "Expedited dispatch: 1–3 working days with post-delivery police verification (₹2,500 urgent surcharge).", "Fast-Track 1–3 Days")
                )
        ));

        // Q4: Booklet Size
        questions.add(new QuestionnaireQuestionDto(
                "bookletSize",
                "Select Passport Booklet Size",
                "Choose based on your planned travel frequency over the validity period.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("PAGES_36", "36 Pages (Standard Booklet)", "Suitable for most students, tourists, and regular international travelers."),
                        new QuestionOptionDto("PAGES_60", "60 Pages (Jumbo Booklet)", "Recommended for frequent international business travelers, conference attendees, and researchers.")
                )
        ));

        // Q5: Renewal Reason (Conditional on applicationType=RENEWAL)
        questions.add(new QuestionnaireQuestionDto(
                "renewalReason",
                "What is the primary reason for renewal / re-issue?",
                "Determines whether Police Verification is completely exempted or if change proofs are required.",
                "applicationType=RENEWAL",
                Arrays.asList(
                        new QuestionOptionDto("EXPIRY_PAGES_NO_CHANGE", "Validity Expired / Expiring within 1 Year or Visa Pages Exhausted", "Personal particulars and residential address remain identical to old passport.", "Exempts Police Verification!"),
                        new QuestionOptionDto("NAME_CHANGE", "Change in Name or Surname", "Requires Marriage Certificate, Gazette Notification, or two public domain proofs."),
                        new QuestionOptionDto("ADDRESS_CHANGE", "Change in Present Residential Address", "Requires updated residential address proof reflecting your current home."),
                        new QuestionOptionDto("OTHER_PARTICULARS_CHANGE", "Other Particulars (Spouse Name, Parent Name, etc.)", "Requires relevant statutory documentary evidence."),
                        new QuestionOptionDto("LOST_DAMAGED", "Replacement due to Lost, Damaged, or Stolen Passport", "For lost, stolen, or damaged passport booklets requiring replacement issue.", "Replacement Fee")
                )
        ));

        // Q6: Educational Qualification (Non-ECR check)
        questions.add(new QuestionnaireQuestionDto(
                "educationLevel",
                "What is your highest educational qualification?",
                "Determines your Emigration Check (Non-ECR vs ECR) status under MEA immigration rules.",
                null,
                Arrays.asList(
                        new QuestionOptionDto("NON_ECR_QUALIFIED", "10th Standard (Matriculation) or Higher / Degree Holder", "Qualifies for Non-ECR (ECNR) status. No emigration clearance required for overseas employment.", "Non-ECR (ECNR)"),
                        new QuestionOptionDto("BELOW_10TH", "Below 10th Standard", "Requires ECR status; POE clearance mandatory when traveling for employment to 18 notified countries.")
                )
        ));

        dto.setQuestions(questions);
        return dto;
    }

    @Override
    public PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers) {
        String appType = answers.getOrDefault("applicationType", "FRESH");
        String category = answers.getOrDefault("applicantCategory", "ADULT");
        String scheme = answers.getOrDefault("scheme", "NORMAL");
        String booklet = answers.getOrDefault("bookletSize", "PAGES_36");
        String renewalReason = answers.getOrDefault("renewalReason", "EXPIRY_PAGES_NO_CHANGE");
        String education = answers.getOrDefault("educationLevel", "NON_ECR_QUALIFIED");

        PathwayEvaluationResultDto result = new PathwayEvaluationResultDto();
        result.setOfficialPortalUrl("https://www.passportindia.gov.in");

        boolean isFresh = "FRESH".equalsIgnoreCase(appType);
        boolean isPcc = "PCC".equalsIgnoreCase(appType) || "PCC".equalsIgnoreCase(category) || "POLICE_CLEARANCE".equalsIgnoreCase(appType);
        boolean isStudentHostel = "COLLEGE_STUDENT_HOSTEL".equalsIgnoreCase(category);
        boolean isMinor = "MINOR".equalsIgnoreCase(category);
        boolean isTatkaal = "TATKAAL".equalsIgnoreCase(scheme);
        boolean is60Pages = "PAGES_60".equalsIgnoreCase(booklet);
        boolean isNonEcr = "NON_ECR_QUALIFIED".equalsIgnoreCase(education);

        // 1. Determine Pathway Code, Title, and Match Reason
        if (isPcc) {
            result.setPathwayCode("POLICE_CLEARANCE_CERTIFICATE");
            result.setPathwayTitle("Police Clearance Certificate (PCC)");
            result.setBadgeText("PCC Application");
            result.setMatchReason("Based on your answers: You are applying for an official Police Clearance Certificate (PCC) issued by the Ministry of External Affairs.");
        } else if (isFresh) {
            if (isStudentHostel) {
                result.setPathwayCode("FRESH_STUDENT_HOSTEL");
                result.setPathwayTitle("Fresh Passport for College / University Student (Hostel / Campus Resident)");
                result.setBadgeText("Special Student Pathway");
                result.setMatchReason("Based on your answers: You are a university student residing in a hostel or campus PG applying for a first-time Ordinary Indian Passport. You can utilize your student ID, bonafide certificate, and hostel warden certificate as official present address proof.");
            } else if (isMinor) {
                result.setPathwayCode("FRESH_MINOR");
                result.setPathwayTitle("Fresh Ordinary Passport (Minor Under 18 Years)");
                result.setBadgeText("Minor Application");
                result.setMatchReason("Based on your answers: You are applying for an Indian citizen under 18 years of age. Standard validity is 5 years or until age 18. Parental consent (Annexure D) and parental passport details apply.");
            } else {
                result.setPathwayCode("FRESH_ADULT");
                result.setPathwayTitle("Fresh Ordinary Passport (Adult Citizen 18+)");
                result.setBadgeText("Standard Adult Issue");
                result.setMatchReason("Based on your answers: You are an adult Indian citizen (age 18+) applying for a first-time Ordinary Indian Passport (Navy Blue Cover) valid for 10 years.");
            }
        } else {
            // Renewal / Re-issue
            if ("LOST_DAMAGED".equalsIgnoreCase(renewalReason) || "LOST".equalsIgnoreCase(renewalReason) || "DAMAGED".equalsIgnoreCase(renewalReason)) {
                result.setPathwayCode("REPLACEMENT_LOST_DAMAGED");
                result.setPathwayTitle("Passport Replacement (Lost, Damaged, or Stolen Passport)");
                result.setBadgeText("Replacement Issue");
                result.setMatchReason("Based on your answers: You are applying for replacement re-issue of a lost, damaged, or stolen passport.");
            } else if ("ECR_DELETION".equalsIgnoreCase(renewalReason)) {
                result.setPathwayCode("REPLACEMENT_ECR_DELETION");
                result.setPathwayTitle("Passport Replacement for Deletion of ECR Status");
                result.setBadgeText("ECR Deletion");
                result.setMatchReason("Based on your answers: You are re-issuing your passport for deletion of ECR status with qualifying educational credentials.");
            } else if ("NAME_CHANGE".equalsIgnoreCase(renewalReason)) {
                result.setPathwayCode("RENEWAL_NAME_CHANGE");
                result.setPathwayTitle("Passport Renewal / Re-issue with Legal Name or Surname Change");
                result.setBadgeText("Particulars Modification");
                result.setMatchReason("Based on your answers: You are renewing your passport with a change in name or surname. Supporting legal proof (Marriage Certificate, Gazette Notification, or public domain documents) is mandatory.");
            } else if ("ADDRESS_CHANGE".equalsIgnoreCase(renewalReason)) {
                result.setPathwayCode("RENEWAL_ADDRESS_CHANGE");
                result.setPathwayTitle("Passport Renewal / Re-issue with Present Address Update");
                result.setBadgeText("Address Update");
                result.setMatchReason("Based on your answers: You are renewing your passport with a new present residential address. You must submit valid residential proof for your new address.");
            } else if ("OTHER_PARTICULARS_CHANGE".equalsIgnoreCase(renewalReason)) {
                result.setPathwayCode("RENEWAL_OTHER_CHANGE");
                result.setPathwayTitle("Passport Re-issue with Personal Particulars Correction");
                result.setBadgeText("Particulars Correction");
                result.setMatchReason("Based on your answers: You are re-issuing your passport due to changes in spouse name, parental details, or other demographic data.");
            } else {
                result.setPathwayCode("RENEWAL_UNCHANGED");
                result.setPathwayTitle("Passport Renewal / Re-issue (Unchanged Personal Particulars)");
                result.setBadgeText("Standard Renewal");
                result.setMatchReason("Based on your answers: You are renewing an existing passport due to validity expiration within 1 year, past expiry, or exhaustion of visa pages, with all personal particulars remaining identical.");
            }
        }

        // 2. Validity
        if (isPcc) {
            result.setValidity("Valid as per destination country immigration requirement (typically 6 months)");
        } else if (isMinor) {
            result.setValidity("5 Years or until age 18 (whichever is earlier). Note: Minors aged 15 to 18 years can opt for a 10-year validity passport by paying the full adult fee and undergoing adult processing.");
        } else {
            result.setValidity("10 Years (Ordinary Indian Passport, Navy Blue Cover)");
        }

        // 3. Emigration Status
        if (isNonEcr) {
            result.setEmigrationStatus("Non-ECR (ECNR - Emigration Check Not Required): You qualify based on educational qualifications (10th standard or higher / degree holder). No POE clearance required when traveling abroad for employment.");
        } else {
            result.setEmigrationStatus("ECR (Emigration Check Required): Educational qualification is below 10th standard. Prior clearance from the Protector of Emigrants (POE) is required when traveling for employment to 18 notified countries.");
        }

        // 4. Fees and Timeline based on Revised Official Passport Seva Fee Structure (Effective 1 July 2026)
        boolean isLostOrDamaged = !isFresh && !isPcc && ("LOST_DAMAGED".equalsIgnoreCase(renewalReason) || "LOST".equalsIgnoreCase(renewalReason) || "DAMAGED".equalsIgnoreCase(renewalReason));

        Integer normalFee = null;
        Integer tatkaalFee = null;
        String feeDescription = null;

        if (!isPcc) {
            if (!isMinor) {
                // Adult categories: General Adult Citizen or College / University Student in Hostel
                if (isLostOrDamaged) {
                    if (is60Pages) {
                        normalFee = 6000;
                        tatkaalFee = 8500;
                        feeDescription = "Lost/Damaged Passport Replacement (Adult, 60 Pages Jumbo Booklet)";
                    } else {
                        normalFee = 5000;
                        tatkaalFee = 7500;
                        feeDescription = "Lost/Damaged Passport Replacement (Adult, 36 Pages Standard Booklet)";
                    }
                } else {
                    // Fresh or Re-issue (renewal, name change, address change, particulars change)
                    if (is60Pages) {
                        normalFee = 3500;
                        tatkaalFee = 6000;
                        feeDescription = "Basic Passport Fee (Adult, 60 Pages Jumbo Booklet)";
                    } else {
                        normalFee = 2500;
                        tatkaalFee = 5000;
                        feeDescription = "Basic Passport Fee (Adult, 36 Pages Standard Booklet)";
                    }
                }
            } else {
                // Minor under 18
                if (!isLostOrDamaged && !is60Pages) {
                    normalFee = 1750;
                    tatkaalFee = 4250;
                    feeDescription = "Basic Passport Fee (Minor under 18, 36 Pages Standard Booklet)";
                }
                // Other minor configurations remain uncalculated to avoid inventing fees
            }
        }

        if (normalFee != null && tatkaalFee != null) {
            int selectedFee = isTatkaal ? tatkaalFee : normalFee;
            result.setCalculatedFee("₹" + String.format("%,d", selectedFee));

            StringBuilder breakdown = new StringBuilder();
            if (isTatkaal) {
                int tatkaalSurcharge = tatkaalFee - normalFee;
                breakdown.append(feeDescription)
                        .append(": ₹").append(String.format("%,d", normalFee))
                        .append(" + Tatkaal Additional Fee: ₹").append(String.format("%,d", tatkaalSurcharge))
                        .append(" = ₹").append(String.format("%,d", selectedFee)).append(" total");
            } else {
                breakdown.append(feeDescription)
                        .append(": ₹").append(String.format("%,d", normalFee))
                        .append(" (Normal Processing)");
            }
            result.setFeeBreakdown(breakdown.toString());
        } else {
            // Category not listed in revised schedule: do not invent fees; direct users to official calculator
            result.setCalculatedFee("Refer to Official Fee Calculator");
            result.setFeeBreakdown("Official statutory fee varies for this category. Please check the official Passport Seva Fee Calculator: https://www.passportindia.gov.in/psp/FeeCalculator");
        }

        if (isPcc) {
            result.setEstimatedTimeline("7 – 21 Working Days (subject to police verification)");
        } else if (isTatkaal) {
            result.setEstimatedTimeline("1 – 3 Working Days");
        } else {
            result.setEstimatedTimeline(isMinor ? "7 – 15 Working Days" : "7 – 15 Working Days (post-Police Verification clearance)");
        }

        // 5. Police Verification (PV) Process (Page 6)
        if (!isFresh && "EXPIRY_PAGES_NO_CHANGE".equalsIgnoreCase(renewalReason)) {
            result.setPoliceVerificationMode("No Police Verification (Exempted)");
            result.setPoliceVerificationDetails("Police verification is exempted under MEA guidelines for passport renewals where present address is unchanged and applicant has a clear prior record. Your booklet will be queued for immediate printing and dispatch upon PSK Granting Officer approval.");
        } else if (isTatkaal) {
            result.setPoliceVerificationMode("Post-Police Verification (After Delivery)");
            result.setPoliceVerificationDetails("Under the Tatkaal fast-track scheme, passport printing and speed-post dispatch occur BEFORE police verification to ensure delivery within 1–3 working days. Your local police station inspection/visit will be scheduled post-delivery.");
        } else {
            result.setPoliceVerificationMode("Pre-Police Verification (Standard Fresh & Changes)");
            result.setPoliceVerificationDetails("Standard Pre-Police Verification applies. Your local police station receives your application electronically. A police officer will conduct a physical address check or summon you to the station with original documents and 2 local witness references. Passport printing commences AFTER a 'Clear' Police Verification Report (PVR) is submitted.");
        }

        // 6. Documents Checklist tailored to pathway
        List<ServiceRequiredDocumentDto> docs = new ArrayList<>();
        long docIdCounter = 1L;

        if (isFresh) {
            if (isStudentHostel) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "College / University Student ID Card",
                        "Physical photo identity card issued by your recognized educational institution.",
                        true,
                        "Original ID + 1 self-attested photocopy",
                        "Obtain from college student affairs / administrative office if misplaced."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Bonafide Certificate from Educational Institution Head/Registrar",
                        "Official bonafide letter certifying full-time student enrollment and current academic session.",
                        true,
                        "Official letterhead with seal and registrar signature",
                        "Apply via college ERP portal or registrar academic section."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Certificate from Hostel Warden",
                        "Official residence certificate confirming you reside in the hostel/campus room.",
                        true,
                        "Hostel letterhead signed by Chief Warden with date and seal",
                        "Obtain directly from your hostel warden office."
                ));
            } else if (isMinor) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Date of Birth (DOB)",
                        "Birth Certificate issued by Registrar of Births and Deaths or Municipal Corporation, or Aadhaar Card.",
                        true,
                        "Original certificate + 1 self-attested photocopy",
                        "Municipal Corporation health department or state civil registration portal."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Present Address (Parental)",
                        "Aadhaar card or passport copy of either parent with matching present residential address.",
                        true,
                        "Original parent ID + self-attested photocopy",
                        "UIDAI e-Aadhaar download or parent passport."
                ));
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Parent Passport Copies & Annexure D (Consent)",
                        "Self-attested photocopies of first and last pages of both parents' passports (if held) along with Annexure D declaration signed by both parents.",
                        true,
                        "Duly filled Annexure D form + parent passport copies",
                        "Download Annexure D template from passportindia.gov.in."
                ));
            } else {
                // General Adult Fresh
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Present Residential Address (Any ONE)",
                        "Aadhaar Card / e-Aadhaar (UIDAI verifiable, recommended for fast-track), Photo Bank Passbook of running account in Scheduled Public/Private/RRB, Electricity/Water/Telephone bill from last 3 months, or Registered Rent Agreement.",
                        true,
                        "Original document + 1 self-attested photocopy",
                        "Download e-Aadhaar from uidai.gov.in or obtain updated stamped bank passbook."
                ));
            }

            if (!isMinor) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Date of Birth (DOB) (Any ONE)",
                        "Aadhaar Card / e-Aadhaar, Birth Certificate from Registrar/Municipal Corp, School Leaving Certificate / 10th Pass Certificate / Secondary School Marksheet, PAN Card, or Driving Licence.",
                        true,
                        "Original document + 1 self-attested photocopy",
                        "Download DigiLocker verified 10th marksheet or UIDAI e-Aadhaar."
                ));
            }

            if (isNonEcr) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Non-ECR (ECNR Status)",
                        "10th Class Passing Certificate / Mark Sheet, Higher Secondary Certificate, College Degree, or Official University Transcripts.",
                        true,
                        "Original educational certificate + 1 self-attested photocopy",
                        "Fetch digital certificate via DigiLocker or obtain duplicate marksheet from school board/university."
                ));
            }

            if (isStudentHostel) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Permanent Parental Address",
                        "Parental address proof (e.g., parents' Aadhaar card, electricity bill, or voter ID) for filling Permanent Address in application.",
                        false,
                        "Photocopy",
                        "Obtain digital copy from parents."
                ));
            }
        } else {
            // Renewal / Re-issue
            docs.add(new ServiceRequiredDocumentDto(
                    docIdCounter++,
                    "Original Old Passport",
                    "Your physical current/expired passport booklet. It will be cancelled with a stamp and returned to you at PSK.",
                    true,
                    "Physical booklet",
                    "Required for cancellation and verification."
            ));
            docs.add(new ServiceRequiredDocumentDto(
                    docIdCounter++,
                    "Self-Attested Photocopies of Old Passport Pages",
                    "Photocopies of: (1) First two pages (Photo & Details), (2) Last two pages (Address & Parents), (3) Non-ECR / ECR page, and (4) Validity Extension / Observation page (if any).",
                    true,
                    "1 set of photocopies, self-signed with date",
                    "Take clear photocopies of the old passport booklet."
            ));

            if ("ADDRESS_CHANGE".equalsIgnoreCase(renewalReason)) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of New Present Residential Address",
                        "Mandatory because present address has changed from old passport: Updated Aadhaar card, Photo Bank Passbook, or recent Utility bill (last 3 months) in your name.",
                        true,
                        "Original document + 1 self-attested photocopy",
                        "Update address via UIDAI portal or request fresh bank statement."
                ));
            } else {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Proof of Present Residential Address",
                        "Proof of present address is NOT mandatory for renewal since your residential address is unchanged from the old passport.",
                        false,
                        "Optional (Not required if address unchanged)",
                        "Exempted under MEA rules for unchanged renewals."
                ));
            }

            if ("NAME_CHANGE".equalsIgnoreCase(renewalReason)) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Supporting Documents for Legal Name / Surname Change",
                        "Marriage Certificate, Official State/Central Gazette Notification of name change, or two public domain documents showing the updated name.",
                        true,
                        "Original Gazette / Marriage Certificate + 1 self-attested photocopy",
                        "Apply at State Government Gazette Press or Registrar of Marriages."
                ));
            }

            if ("OTHER_PARTICULARS_CHANGE".equalsIgnoreCase(renewalReason)) {
                docs.add(new ServiceRequiredDocumentDto(
                        docIdCounter++,
                        "Supporting Documents for Particulars Change",
                        "Relevant certificate (e.g. Spouse Passport copy, Marriage Certificate, or Court order) supporting the correction.",
                        true,
                        "Original + 1 self-attested photocopy",
                        "Competent civil authority or court."
                ));
            }
        }

        if (isTatkaal) {
            docs.add(new ServiceRequiredDocumentDto(
                    docIdCounter++,
                    "Tatkaal Verification Documents / Annexure F",
                    "Under Tatkaal rules, you must present qualifying documentation (Annexure F verification certificate or 3 specified identity/address documents such as Aadhaar, PAN, Voter ID, Bank Passbook, Student ID).",
                    true,
                    "Originals + 1 set self-attested copies",
                    "Download Annexure F format from passportindia.gov.in or prepare 3 national identity documents."
            ));
        }

        result.setDocuments(docs);

        // 7. Step-by-Step Procedure customized to pathway (Section 3 of research)
        List<ServiceStepDto> steps = new ArrayList<>();
        int stepNum = 1;

        steps.add(new ServiceStepDto(
                (long) stepNum,
                stepNum++,
                "User Registration on Passport Portal",
                "1. Visit the official MEA portal: https://www.passportindia.gov.in.\n2. Click 'Register Now' under the 'New User' section.\n3. Select your Regional Passport Office (RPO) based on your current residential city/district.\n4. Enter your Full Name (exactly as per 10th marksheet/Aadhaar), Date of Birth, Email ID, Login ID, and Password.\n5. Activate your account via the verification email sent by MEA.",
                "10–15 Minutes",
                "Do not use third-party fake portals. Always check that the domain is passportindia.gov.in."
        ));

        steps.add(new ServiceStepDto(
                (long) stepNum,
                stepNum++,
                "Filling the Online Application & Generating ARN",
                "1. Log in to your Passport Seva account.\n2. Select 'Apply for Fresh Passport / Re-issue of Passport'.\n3. Choose Service Type: '" + (isTatkaal ? "Tatkaal" : "Normal") + "'.\n4. Select Booklet Type: '" + (is60Pages ? "60 Pages (Jumbo)" : "36 Pages (Standard)") + "'.\n5. Fill Personal Details, Family Details, Present Address, and Permanent Address.\n" +
                        (isStudentHostel ? "★ STUDENT RULE: Enter your hostel/campus address under 'Present Address', and your parents' address under 'Permanent Address'.\n" : "") +
                        (isFresh ? "" : "★ RENEWAL: Enter previous passport number, date of issue, expiry date, and issuing authority.\n") +
                        "6. Verify details carefully and click Submit. Your Application Reference Number (ARN) will be generated.",
                "20–30 Minutes",
                "Spellings must match 100% across Aadhaar, 10th certificate, and this form."
        ));

        steps.add(new ServiceStepDto(
                (long) stepNum,
                stepNum++,
                "Online Payment & PSK / POPSK Appointment Booking",
                "1. On the 'View Saved/Submitted Applications' screen, click 'Pay and Schedule Appointment'.\n2. Select Online Payment Mode (UPI, Net Banking, Credit/Debit Card, or SBI Challan).\n3. Pay applicable fee: " + result.getCalculatedFee() + ".\n4. Select your preferred Passport Seva Kendra (PSK) or Post Office Passport Seva Kendra (POPSK) and choose an open date and time slot.\n5. Download and print the Appointment Confirmation Slip.",
                "10 Minutes",
                "Keep the printed Appointment Confirmation Slip ready for entry at the PSK gate."
        ));

        steps.add(new ServiceStepDto(
                (long) stepNum,
                stepNum++,
                "PSK Appointment Day Walkthrough (Inside PSK)",
                "Arrive 15 minutes prior to your scheduled time with original documents + 1 set of self-attested photocopies.\n" +
                        "• Security & Entry: Show appointment slip at the gate to get entry.\n" +
                        "• Token Counter: Original documents are checked; a Token Number (e.g., N-12 or T-05) is issued.\n" +
                        "• Counter A (Citizen Service Executive - CSE): Live photograph taken, 10-finger biometric scan, document scanning into digital file, and dual-screen review. You sign on the digital signature pad.\n" +
                        "• Counter B (Verification Officer - VO): Government officer physically verifies originals against scanned copies (address, DOB, Non-ECR status).\n" +
                        "• Counter C (Granting Officer - GO): Decision officer reviews and grants your application, and determines the Police Verification mode.\n" +
                        "• Exit Slip: Collect your Exit Slip / Acknowledgement Receipt showing file status before leaving.",
                "1–2 Hours at PSK",
                "Carry original documents for all uploaded proofs. Self-attest all photocopies."
        ));

        steps.add(new ServiceStepDto(
                (long) stepNum,
                stepNum++,
                "Police Verification (PV) Process (" + result.getPoliceVerificationMode() + ")",
                result.getPoliceVerificationDetails(),
                isTatkaal ? "Scheduled post-delivery" : (!isFresh && "EXPIRY_PAGES_NO_CHANGE".equalsIgnoreCase(renewalReason) ? "Exempted" : "3–7 Days post-PSK"),
                "Prepare photocopies of documents and details (Aadhaar & phone numbers) of 2 local witnesses."
        ));

        steps.add(new ServiceStepDto(
                (long) stepNum,
                stepNum++,
                "Printing, Speed Post Dispatch & Secure In-Person Delivery",
                "1. Once granted and verified, the passport is sent to the Security Printing Press.\n2. The passport is dispatched exclusively via Speed Post (Department of Posts).\n3. A Tracking Number (Speed Post Tracking ID) is sent via SMS and updated on the Passport Seva Portal.\n4. DELIVERY RULE: The postman will hand over the passport envelope ONLY to the applicant in person upon verifying physical photo ID proof.",
                result.getEstimatedTimeline(),
                "Ensure you or an authorized adult family member with authorization and applicant ID is available at the delivery address."
        ));

        result.setSteps(steps);

        // 8. Critical Tips (Section 5 of research & Official Fee Rules)
        List<String> tips = new ArrayList<>();
        if (isStudentHostel) {
            tips.add("Student Dual Address Handling: On your application form, list your hostel/college location under 'Present Address' and your parents' home address under 'Permanent Address'. This prevents address rejection during police verification.");
        }
        tips.add("Exact Name Matching: Ensure spelling of applicant name, father's name, mother's name, and DOB match exactly across Aadhaar, 10th Mark Sheet, and the Application Form. Any discrepancy requires a court affidavit or official gazette notification.");
        tips.add("Police Verification Preparation: Keep photocopies of documents, original IDs, and details (Aadhaar & phone numbers) of two local neighbors/acquaintances ready for police verification witnesses.");
        if (isTatkaal) {
            tips.add("Tatkaal Verification Rule: Tatkaal issuance requires presenting qualifying Annexure F verification or 3 national ID documents during Counter B verification.");
        }
        if (isNonEcr) {
            tips.add("Non-ECR Privilege: Having passed 10th standard or holding a college degree entitles you to Non-ECR status—saving you from POE clearance when traveling abroad for employment.");
        }
        tips.add("Official Fee Rebate: Fresh passport applicants aged 8 years or below and senior citizens above 60 years get a 10% rebate on the basic passport fee.");
        tips.add("Police Clearance Certificate (PCC): Official PCC fee is fixed at ₹500 with no Tatkaal expedited processing applicable.");
        result.setCriticalTips(tips);

        return result;
    }
}
