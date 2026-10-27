-- Seed VIT-AP Student Reporting & Document Verification Process

DO $$
DECLARE
    v_college_id BIGINT;
    v_process_id BIGINT;
    v_service_id BIGINT;
BEGIN
    -- 1. Ensure College 3 exists or find by name
    SELECT id INTO v_college_id FROM colleges WHERE short_name = 'VIT' OR name ILIKE '%Vellore institute of technology%' LIMIT 1;
    
    IF v_college_id IS NULL THEN
        INSERT INTO colleges (name, short_name, location, website, description, active, created_at, updated_at)
        VALUES ('Vellore institute of technology AP', 'VIT', 'Andhra Pradesh', 'https://vtop.vitap.ac.in/vtop/open/page', 'Vellore Institute of Technology, Andhra Pradesh (VIT-AP) is an institution of higher education offering undergraduate, postgraduate, and doctoral programmes in engineering and management.', true, NOW(), NOW())
        RETURNING id INTO v_college_id;
    END IF;

    -- 2. Check if process already exists (preserve existing records, do not delete or recreate)
    SELECT id INTO v_process_id FROM college_processes WHERE college_id = v_college_id AND title = 'Student Reporting & Document Verification (B.Tech / UG Admissions)' LIMIT 1;

    IF v_process_id IS NULL THEN
        -- 3. Insert College Process
        INSERT INTO college_processes (
        college_id, title, category, description, eligibility, timeline, fees, status, important_notes, official_portal_url, created_at, updated_at
    ) VALUES (
        v_college_id,
        'Student Reporting & Document Verification (B.Tech / UG Admissions)',
        'Admissions',
        'Official institutional reporting and document verification process for newly admitted undergraduate (B.Tech) students at Vellore Institute of Technology AP (VIT-AP). Covers online pre-reporting profile submission, fee verification, affidavit procurement, physical verification at the campus administration desk, and hostel/day-scholar clearance.',
        'Candidates who have received a provisional admission offer through VITEEE Counselling or official management allotment, completed initial tuition fee payments, and obtained their Provisional Allotment Letter.',
        'Annual Reporting Window (typically July - August as per VIT-AP academic calendar)',
        'Free (Document verification is included in admission tuition; hostel fee separate)',
        'ACTIVE',
        'All original certificates (TC, Migration, Marksheets) must be presented physically at the verification desks along with 2-3 sets of self-attested photocopies. Affidavits (Hostel or Day Scholar) MUST be executed on non-judicial stamp paper with notary attestation. Anti-ragging undertakings do NOT require stamp paper and must be submitted on plain A4 paper from antiragging.in.',
        'https://vtop.vitap.ac.in',
        NOW(),
        NOW()
    ) RETURNING id INTO v_process_id;

    -- 4. Insert Steps into college_process_steps
    INSERT INTO college_process_steps (process_id, step_number, title, description, estimated_time, notes, created_at)
    VALUES
    (
        v_process_id, 1,
        'Online Pre-Reporting & Profile Submission on VTOP',
        'Log in to the VIT-AP VTOP portal (https://vtop.vitap.ac.in) using your student application number and password. Complete the Fresher Profile with biographical data, parent contact numbers, address, and upload required digital copies.',
        '30–45 Minutes',
        'Ensure name spelling matches your 10th marksheet and Aadhaar card exactly before final submission.',
        NOW()
    ),
    (
        v_process_id, 2,
        'Download Provisional Allotment & Generate Anti-Ragging Undertaking',
        'Download the official Provisional Allotment Letter from the admissions portal. Next, visit the central UGC portal at antiragging.in, submit your details, obtain the reference number, and print the Student & Parent Undertakings on plain A4 paper.',
        '20–30 Minutes',
        'Per Supreme Court and UGC directives, Anti-Ragging affidavits DO NOT require stamp paper or notary seal. Plain signed printouts are standard.',
        NOW()
    ),
    (
        v_process_id, 3,
        'Execute Legal Affidavits on Stamp Paper (Hostel or Day Scholar)',
        'Procure ₹20, ₹50, or ₹100 non-judicial stamp paper from a licensed stamp vendor at your local court/tehsil. Print the prescribed VIT-AP draft (Hostel Affidavit for hostellers OR Day Scholar Affidavit for commuters). Both student and parent must sign before a Notary Public, who will apply an official notary seal, stamp, and register entry number.',
        '1–2 Days',
        'CRITICAL: Plain paper printouts for Hostel and Day Scholar affidavits are legally invalid without Notary attestation and Stamp Paper execution.',
        NOW()
    ),
    (
        v_process_id, 4,
        'Medical Examination & Physical Fitness Certificate',
        'Download the college-prescribed medical format. Visit a Registered Medical Practitioner (MBBS doctor minimum) for routine physical checkup (vision, blood pressure, blood group). Have the doctor sign and affix their official rubber stamp containing their Medical Council Registration Number.',
        'Half Day',
        'The doctor seal and Medical Council Registration Number are mandatory; forms without a registration number will be rejected.',
        NOW()
    ),
    (
        v_process_id, 5,
        'Physical In-Person Reporting at VIT-AP Campus Verification Desks',
        'Arrive at the VIT-AP Amaravati campus venue (Auditorium / Academic Block) during your designated reporting time slot. Hand over the original TC, Migration Certificate, and physical affidavits. Present all other original certificates for on-the-spot verification alongside 2-3 sets of self-attested photocopies.',
        '2–3 Hours',
        'Carry 5-6 recent passport-size photographs and all fee payment receipts in a waterproof folder.',
        NOW()
    ),
    (
        v_process_id, 6,
        'Hostel Room Key Handover / Day Scholar Pass & Student ID Smartcard',
        'Proceed to the Hostel Allotment desk to submit your Hostel Allotment Letter and collect your room keys and mess registration, or visit the Transport desk for Day Scholar bus pass allotment. Complete your biometric registration for your VIT-AP ID Smartcard.',
        '1 Hour',
        'Keep your reporting clearance slip safe until your permanent smartcard is issued.',
        NOW()
    );

    -- 5. Insert Required Documents into college_required_documents
    -- 1. Transfer Certificate (TC)
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Transfer Certificate (TC)',
        'Original Transfer Certificate issued by the principal/head of the school or junior college last attended, certifying formal release and no dues.',
        true,
        'Original Hardcopy (Mandatory submission)',
        'WHAT IT IS & PURPOSE:
A Transfer Certificate (TC) is a statutory academic record issued by the school or junior college you last attended (12th standard / Intermediate). It certifies that you have completed your course, have cleared all institution dues, and have formally departed to pursue higher education. VIT-AP requires the physical ORIGINAL TC at the time of reporting for university enrollment.

ISSUING AUTHORITY:
Issued and signed by the School Principal, Headmaster, or Junior College Director of your last attended institution.

WHERE TO OBTAIN:
From the administrative office or student records section of your 12th standard school / junior college.

STEP-BY-STEP PROCEDURE:
1. Submit an application for TC to your school/college administrative desk upon announcement of your 12th board results.
2. Complete the institution''s ''No Dues'' clearance form across all relevant departments (library, laboratory, accounts/fees, sports).
3. Pay any nominal certificate processing fees, if applicable by the school.
4. Collect the original TC signed by the Principal with the school''s official rubber seal.

SUPPORTING DETAILS NEEDED:
- 12th Board Examination Hall Ticket / Admit Card
- Fee clearance receipt / No Dues slip
- Institutional identity card

EXECUTION & NOTARY REQUIREMENTS:
- Needs to be an ORIGINAL hardcopy on the school''s official letterhead with ink signature and institutional seal.
- Photocopies or laminated copies are NOT accepted for final submission.
- NO stamp paper and NO lawyer/notary attestation is required.

MISTAKES TO AVOID:
- Do NOT laminate the original TC before submission, as universities require physical verification and retention.
- Ensure the student name, date of birth, and parents'' names on the TC match your 10th marksheet and Aadhaar card exactly.
- Make at least 5-6 photocopies and scan a high-resolution PDF for your personal records before handing over the original.

OFFICIAL REFERENCE:
VIT-AP UG Admissions Guidelines: https://vitap.ac.in',
        NOW()
    );

    -- 2. Migration Certificate
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Migration Certificate',
        'Official Migration Certificate issued by the education board (CBSE, CISCE, State Board, or NIOS) enabling registration into a university.',
        true,
        'DigiLocker Verified PDF / Original Hardcopy',
        'WHAT IT IS & PURPOSE:
A Migration Certificate is an official document issued by your secondary/senior secondary school education board (CBSE, CISCE, State Board, or NIOS). It certifies that the board has no objection to the student transitioning to a university outside the jurisdiction of that school board. It is mandatory for university enrollment and student registration under the UGC.

ISSUING AUTHORITY:
Issued by the respective Central or State Board of Secondary Education (CBSE, CISCE, State Intermediate Board, etc.).

WHERE TO OBTAIN:
1. Digital Route (Fastest): Directly via Government of India DigiLocker portal (digilocker.gov.in) or mobile app.
2. Physical Route: From your school''s administrative office (for CBSE/ICSE schools, usually provided alongside your 12th marksheet) or via the respective State Education Board''s regional office/online portal.

STEP-BY-STEP PROCEDURE:
Option A - DigiLocker (Recommended):
1. Visit https://digilocker.gov.in or open the DigiLocker app on your smartphone.
2. Sign in using your Aadhaar-linked mobile number and 6-digit security PIN.
3. Search for your Education Board (e.g., ''Central Board of Secondary Education'' or your State Board).
4. Select ''Class XII Migration Certificate'', enter your Roll Number, Passing Year, and School Code.
5. Click ''Get Document''. DigiLocker will fetch the cryptographically verified e-Migration Certificate with the Government of India digital signature.

Option B - Physical Route:
1. Inquire with your school office whether the board sent physical migration certificates along with passing certificates.
2. If not issued by school, apply online through your board''s official portal (e.g., cbse.gov.in duplicate certificate portal / state board portal).
3. Pay the prescribed fee online and choose Speed Post delivery or counter collection.

SUPPORTING DETAILS NEEDED:
- 12th Board Roll Number and Registration Number
- Year of Passing
- School Center Code (found on 12th Admit Card)
- Aadhaar Number linked with DigiLocker

EXECUTION & NOTARY REQUIREMENTS:
- Digital version fetched through DigiLocker with valid digital signature is legally valid under the Information Technology Act, 2000.
- If submitting physical certificate, submit the original hardcopy issued by the board.
- NO stamp paper and NO notary public attestation is required.

MISTAKES TO AVOID:
- Do not apply for migration certificate before your 12th board results are officially published.
- Ensure your name spelling matches your Aadhaar card and VIT-AP admission offer letter.

OFFICIAL REFERENCE:
Government of India DigiLocker Portal: https://digilocker.gov.in | CBSE Portal: https://cbse.gov.in',
        NOW()
    );

    -- 3. Physical Fitness Certificate
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Physical Fitness Certificate',
        'Medical fitness certificate on the college-prescribed format signed by a Registered Medical Practitioner (minimum MBBS) certifying general health, blood group, vision, and physical fitness.',
        true,
        'Original Signed Hardcopy with Doctor''s Official Seal & Reg No.',
        'WHAT IT IS & PURPOSE:
A Physical Fitness Certificate is a medical assessment confirming that the candidate is physically and mentally fit to pursue academic studies and campus life, and does not suffer from any contagious or incapacitating medical conditions. VIT-AP requires this to ensure student well-being and maintain campus health records.

ISSUING AUTHORITY:
Must be examined and signed by a Registered Medical Practitioner (RMP) holding an MBBS degree minimum, registered with the National Medical Commission (NMC) or State Medical Council.

WHERE TO OBTAIN:
Any government hospital, primary health center (PHC), registered private clinic, or nursing home with a qualified MBBS/MD doctor.

STEP-BY-STEP PROCEDURE:
1. Download the official VIT-AP Medical Fitness Certificate proforma from the VIT-AP admission brochure / VTOP portal (or standard institutional medical format).
2. Print the proforma cleanly on a single A4 sheet.
3. Visit a registered MBBS doctor for routine physical examination (blood pressure, height, weight, vision acuity, color blindness check, and chest/systemic examination).
4. Carry diagnostic report or lab slip confirming your Blood Group (ABO and Rh factor).
5. The doctor will fill in the clinical findings, declare you fit, sign, and date the form.
6. MANDATORY: Ensure the doctor affixes their official rubber stamp containing their Full Name, Qualifications (e.g., MBBS, MD), and State/National Medical Council Registration Number.

SUPPORTING DETAILS NEEDED:
- College Medical Proforma printout
- Valid Government ID (Aadhaar Card)
- Recent passport photograph of the student (pasted on the form and cross-signed/stamped by the doctor)
- Laboratory report of Blood Group testing

EXECUTION & NOTARY REQUIREMENTS:
- Must be an ORIGINAL hardcopy bearing the doctor''s real pen signature and rubber stamp.
- The Medical Council Registration Number on the stamp is STRICTLY required; certificates without a registration number will be rejected by verification officers.
- NO non-judicial stamp paper and NO lawyer/notary attestation is required.

MISTAKES TO AVOID:
- Do not get the certificate signed by Homeopathy, Ayurvedic, or Unani practitioners unless explicitly authorized by the university; VIT-AP requires an allopathic MBBS doctor minimum.
- Do not forget the doctor''s seal or registration number.
- Ensure the blood group matches your online student profile on VTOP.

OFFICIAL REFERENCE:
VIT-AP Health Services & Medical Format: https://vitap.ac.in',
        NOW()
    );

    -- 4. Provisional Allotment Letter
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Provisional Allotment Letter',
        'Official provisional admission allotment letter issued by VIT-AP confirming branch allotment, application number, category, and fee clearance status.',
        true,
        'Signed Hardcopy Printout',
        'WHAT IT IS & PURPOSE:
The Provisional Allotment Letter is the official legal offer of admission issued by VIT-AP. It specifies the student''s allotted engineering branch/programme, campus (VIT-AP Amaravati), fee category, admission number, and confirms that initial tuition fees have been credited. It serves as primary proof of your right to report at the verification desk.

ISSUING AUTHORITY:
Director of Admissions, Vellore Institute of Technology AP (VIT-AP).

WHERE TO OBTAIN:
Downloaded directly from the official VIT Admissions portal / VITEEE Counselling portal / VTOP freshers login.

STEP-BY-STEP PROCEDURE:
1. Log in to the official VIT Admissions / Counselling portal (https://vitap.ac.in or vtop.vitap.ac.in) using your registered Application Number and password/OTP.
2. Navigate to the ''Seat Allotment / Allotment Letter'' section.
3. Confirm that your fee payment status reflects ''Success'' or ''Fee Confirmed''.
4. Click ''Download Provisional Allotment Letter'' and save the generated PDF.
5. Print 2 to 3 color copies on good quality A4 paper.
6. The candidate must physically sign at the designated applicant signature line at the bottom of the letter.

SUPPORTING DETAILS NEEDED:
- VITEEE / Admission Application Number
- Portal Login Password / Date of Birth
- Tuition fee transaction ID / payment receipt for reference

EXECUTION & NOTARY REQUIREMENTS:
- Needs to be a clean printout with the candidate''s original signature.
- NO stamp paper and NO notary public attestation is required.

MISTAKES TO AVOID:
- Do not report with an outdated allotment letter from an earlier counselling phase if your branch was subsequently upgraded; ensure you carry the final upgraded allotment letter.
- Always check that your fee receipt details match the transaction number listed on the letter.

OFFICIAL REFERENCE:
VIT-AP Admissions Portal: https://vitap.ac.in | VTOP Open Page: https://vtop.vitap.ac.in/vtop/open/page',
        NOW()
    );

    -- 5. Hostel Affidavit
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Hostel Affidavit',
        'Legal sworn affidavit for campus residence executed on non-judicial stamp paper, signed by student and parent before a Notary Public.',
        false,
        'Original Notarized Hardcopy on Non-Judicial Stamp Paper',
        'WHAT IT IS & PURPOSE:
The Hostel Affidavit is a legally binding undertaking required from every student choosing to reside in the university hostels. In this affidavit, the student and their parent/guardian solemnly affirm that they will strictly follow hostel rules, adhere to campus curfew and biometric in/out timings, refrain from bringing contraband/substances or unauthorized appliances, accept liability for any hostel property damages, and abide by disciplinary decisions of the Chief Warden.

WHO NEEDS IT:
Only students who are opting for on-campus hostel accommodation. Day scholars commuting from home or local guardians do NOT need this document.

WHOSE NAME & WHO SIGNS:
The affidavit is executed jointly in the names of the Student (deponent 1) and Parent/Legal Guardian (deponent 2). BOTH the student and the parent MUST physically sign the affidavit in the presence of the Notary Public.

STAMP PAPER & NOTARY SPECIFICATIONS:
- Procure Non-Judicial Stamp Paper of value ₹20, ₹50, or ₹100 (as prescribed by Andhra Pradesh state stamp laws or the student''s home state).
- The legal text prescribed by VIT-AP must be cleanly printed directly onto the non-judicial stamp paper (and continued on green legal ledger paper or plain A4 sheets if multiple pages are needed).
- MUST be attested by an Advocate or Notary Public appointed by the State/Central Government.
- The Notary Public must affix their official red notary seal, notary registration stamp, date, and entry serial number from their official Notary Register.

WHERE TO OBTAIN:
Any local Sub-Registrar Office, Tehsil/Taluk Office, District Court, or notary kiosk in your city/town.

STEP-BY-STEP PROCEDURE:
1. Download the official ''Hostel Affidavit Draft Format'' from the VIT-AP Fresher Handbook or VTOP admission portal.
2. Purchase a ₹20, ₹50, or ₹100 Non-Judicial Stamp Paper from a licensed stamp vendor at your local court or registrar office.
3. Have a legal typist or notary printer print the exact VIT-AP hostel draft wording onto the stamp paper, filling in the student''s name, application number, branch, parent''s name, and permanent address.
4. Visit the Notary Public together with your parent.
5. Present original Aadhaar cards of student and parent for identity verification.
6. Sign the affidavit (both student and parent) in front of the Notary Public.
7. The Notary will stamp, seal, sign, and record the document number in their register.

CRITICAL MISTAKES TO AVOID:
- CRITICAL LEGAL NOTICE: Plain paper printouts are legally invalid without Notary attestation and Stamp Paper execution.
- Do not sign the stamp paper beforehand at home; the Notary must witness the physical signatures.
- Do not confuse this with the Anti-Ragging Affidavit; Anti-Ragging does NOT require stamp paper, whereas this Hostel Affidavit STRICTLY requires stamp paper and notary attestation.

OFFICIAL REFERENCE:
VIT-AP Hostel Rules & Admission Forms: https://vitap.ac.in',
        NOW()
    );

    -- 6. Day Scholar Affidavit
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Day Scholar Affidavit',
        'Legal sworn affidavit on non-judicial stamp paper for day scholars, verifying local residential address with parents/guardians and agreeing to campus transit rules.',
        false,
        'Original Notarized Hardcopy on Non-Judicial Stamp Paper',
        'WHAT IT IS & PURPOSE:
The Day Scholar Affidavit is a legal undertaking required for students who will commute to VIT-AP daily from home or reside with parents/authorized local guardians in the Vijayawada, Guntur, Amaravati, or Tenali region instead of taking university hostel accommodation. It affirms that the student lives under parental/guardian care, specifies the verified local residential address within commuting distance, and legally binds the student not to enter or loiter in university hostel blocks without written proctor permission.

WHO NEEDS IT:
Only students who are admitted as Day Scholars. Hostellers residing in university dormitories do NOT need this document.

WHOSE NAME & WHO SIGNS:
Executed in the joint names of the Student and Parent/Authorized Local Guardian. BOTH the student and parent/guardian must sign before a Notary Public.

STAMP PAPER & NOTARY SPECIFICATIONS:
- Procure Non-Judicial Stamp Paper of value ₹20, ₹50, or ₹100 (as per Andhra Pradesh or applicable state stamp laws).
- Print the exact institutional day-scholar undertaking wording onto the stamp paper.
- Attested and notarized by an Advocate or Notary Public with official seal, notary stamp, and official register entry number.

WHERE TO OBTAIN:
From licensed stamp vendors and Notary Publics at your local District Court, Sub-Registrar Office, or Tehsil premises.

STEP-BY-STEP PROCEDURE:
1. Download the official ''Day Scholar Undertaking / Affidavit Format'' from the VIT-AP admission portal or prospectus.
2. Purchase a non-judicial stamp paper of ₹20, ₹50, or ₹100 from an authorized stamp vendor.
3. Print the prescribed text onto the stamp paper, entering the student''s full name, roll/application number, parent/guardian name, and the exact local residential address.
4. Gather address proof of the local residence (electricity bill, property tax receipt, registered rental agreement, or local Aadhaar).
5. Meet the Notary Public along with your parent or local guardian.
6. Sign the affidavit in the presence of the Notary.
7. Ensure the Notary applies their notary seal, signature, and official registry serial number.

CRITICAL MISTAKES TO AVOID:
- CRITICAL LEGAL NOTICE: Plain paper printouts are legally invalid without Notary attestation and Stamp Paper execution.
- Do not provide an address that is excessively far from campus (e.g. beyond safe daily commuting limits), as university regulations require day scholars to reside within specified transit zones or use university bus routes.
- If staying with a local guardian (not parents), ensure the local guardian''s authorization letter from parents is attached.

OFFICIAL REFERENCE:
VIT-AP Day Scholar Regulations: https://vitap.ac.in',
        NOW()
    );

    -- 7. Anti-Ragging Affidavit
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Anti-Ragging Affidavit',
        'Mandatory statutory anti-ragging undertakings generated online via the official UGC portal (antiragging.in), signed on plain A4 paper by student and parent.',
        true,
        'Plain Paper Signed Printouts (Student & Parent Undertakings)',
        'WHAT IT IS & PURPOSE:
The Anti-Ragging Undertaking (often referred to as Anti-Ragging Affidavit) is a mandatory nationwide statutory declaration required by the University Grants Commission (UGC) and the Supreme Court of India in compliance with the ''UGC Regulations on Curbing the Menace of Ragging in Higher Educational Institutions, 2009''. It commits both the student and parent to zero tolerance for ragging, affirming knowledge of severe penal and academic consequences.

WHO NEEDS IT:
MANDATORY for ALL incoming undergraduate and postgraduate students, whether hosteller or day scholar.

STATUTORY LEGAL EXEMPTION - NO STAMP PAPER / NO NOTARY:
IMPORTANT LEGAL NOTICE: Per UGC circulars and Supreme Court directives, Anti-Ragging Affidavits DO NOT REQUIRE STAMP PAPER OR NOTARY PUBLIC SIGNATURES. They are prepared entirely online as electronic undertakings and printed on standard plain A4 white paper.

WHERE TO OBTAIN:
Official Government of India UGC Anti-Ragging Web Portal: https://antiragging.in (or https://www.amanmovement.org).

STEP-BY-STEP PROCEDURE:
1. Visit the official UGC portal at https://antiragging.in.
2. Click on the button ''Fill Undertaking'' (or ''Register for Undertaking'').
3. Select your College Type: Choose ''University / College''.
4. Fill in Student Details: Full Name, Gender, Mobile Number, Email ID, Aadhaar Number, and Permanent Address.
5. Fill in Parent / Guardian Details: Name, Mobile, Email, and Address.
6. Fill in University Details:
   - State: Andhra Pradesh
   - University/College Name: Select ''Vellore Institute of Technology - AP'' (or search VIT-AP)
   - Director/Principal Name: Enter Vice Chancellor / Director of Admissions
   - Nearest Police Station: Thullur Police Station (Guntur/Amaravati)
7. Fill in Course Details: Under Graduate (B.Tech), Name of Course (e.g., Computer Science and Engineering), Year of Study: 1st Year.
8. Submit the online form.
9. You will immediately receive a unique National Anti-Ragging Reference ID via SMS and Email.
10. Download the two auto-generated PDF documents:
    - Undertaking by the Student
    - Undertaking by the Parent / Guardian
11. Print both undertakings on clean, plain A4 white paper.
12. Physically sign the Student Undertaking. Have your parent/guardian physically sign the Parent Undertaking.

MISTAKES TO AVOID:
- DO NOT waste money buying stamp paper or paying a notary public for Anti-Ragging undertakings; the UGC explicitly mandates plain paper printouts.
- Keep note of your Anti-Ragging Reference Number, as you will need to enter this number during physical reporting and in the VTOP portal.
- Ensure the spelling of student name and university matches your admission letter.

OFFICIAL REFERENCE:
UGC Anti-Ragging National Portal: https://antiragging.in | Aman Satya Kachroo Trust: https://www.amanmovement.org',
        NOW()
    );

    -- 8. Aadhaar Card
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Aadhaar Card',
        'Photocopies of Government of India 12-digit Aadhaar Card, with the original card carried for physical desk inspection.',
        true,
        'Photocopies (2-3 Copies) + Original for Physical Verification',
        'WHAT IT IS & PURPOSE:
The Aadhaar Card is your primary biometric National Identity Proof issued by the Unique Identification Authority of India (UIDAI). VIT-AP uses it to verify the student''s identity, date of birth, nationality, and permanent residential address, as well as for registering campus biometrics.

ISSUING AUTHORITY:
Unique Identification Authority of India (UIDAI), Government of India.

WHERE TO OBTAIN:
Downloaded online from the official UIDAI myAadhaar portal (https://myaadhaar.uidai.gov.in).

STEP-BY-STEP PROCEDURE:
1. Visit the official UIDAI portal: https://myaadhaar.uidai.gov.in.
2. Click ''Download Aadhaar''.
3. Enter your 12-digit Aadhaar number (or 16-digit VID / 28-digit Enrolment ID) and the security captcha.
4. Click ''Send OTP''. An OTP will be sent to your Aadhaar-linked registered mobile phone number.
5. Enter the OTP and click ''Verify & Download''.
6. Open the password-protected downloaded e-Aadhaar PDF. (Password format: First 4 letters of your name in CAPITAL letters followed by your 4-digit Year of Birth, e.g., SHAS2006).
7. Print 2 to 3 clear color or high-contrast black-and-white photocopies.
8. Self-attest each photocopy by signing with date at the bottom margin.

SUPPORTING DETAILS NEEDED:
- Active mobile number registered with your Aadhaar for OTP verification.
- Original PVC or laminated Aadhaar card to show at the verification desk.

EXECUTION & NOTARY REQUIREMENTS:
- Self-attested paper photocopies are submitted.
- Original Aadhaar card is inspected at the desk and immediately returned.
- NO notary attestation is required.

MISTAKES TO AVOID:
- Ensure the mobile number linked to Aadhaar is active before travelling to campus, as OTP verification may be required during SIM card acquisition or student bank account opening.
- Ensure your name, father''s name, and date of birth match your 10th marksheet exactly.

OFFICIAL REFERENCE:
UIDAI Official Portal: https://myaadhaar.uidai.gov.in',
        NOW()
    );

    -- 9. Student Profile
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Student Profile',
        'Official student profile summary generated and printed directly from the VIT-AP VTOP admissions portal after completing online registration.',
        true,
        'Printed Copy from VTOP Portal',
        'WHAT IT IS & PURPOSE:
The Student Profile is an institution-specific summary sheet generated by the VIT-AP administrative portal (VTOP). It compiles your verified biographical data, academic records, entrance exam rank, hostel/day scholar preference, emergency contact details, blood group, and proctoring information.

ISSUING AUTHORITY:
VIT-AP University Admissions & Academic Information Systems (VTOP).

WHERE TO OBTAIN:
Downloaded directly from the VIT-AP VTOP portal: https://vtop.vitap.ac.in.

STEP-BY-STEP PROCEDURE:
1. Log in to the VIT-AP VTOP portal (https://vtop.vitap.ac.in) using your student credentials (Application Number / Registration Number and password).
2. Complete all required profile fields under ''Fresher Registration / Student Profile'' (personal details, parent/guardian contacts, local address, medical history, blood group).
3. Upload digital copies of your photo and signature if prompted.
4. Review all submitted information for 100% accuracy and click ''Final Submit''.
5. Click ''Print / Download Student Profile''.
6. Print 1 to 2 clear copies on standard A4 paper.
7. Candidate and parent must sign at the declaration section at the end of the printed profile.

SUPPORTING DETAILS NEEDED:
- VTOP login credentials
- 10th and 12th board marks and registration numbers
- Emergency contact numbers and blood group

EXECUTION & NOTARY REQUIREMENTS:
- Standard paper printout signed by the candidate and parent.
- NO stamp paper and NO notary attestation is required.

MISTAKES TO AVOID:
- Complete the online profile before arriving at campus for reporting to avoid long delays at computer helpdesks.
- Verify that your blood group entered matches your Medical Fitness Certificate.

OFFICIAL REFERENCE:
VIT-AP VTOP Portal: https://vtop.vitap.ac.in/vtop/open/page',
        NOW()
    );

    -- 10. Passport Size Photos
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Passport Size Photos',
        'Recent identical color passport-size photographs (minimum 5-6 copies) with a clear white background for ID card, dossiers, and records.',
        true,
        'Original Color Photographs (5-6 copies)',
        'WHAT IT IS & PURPOSE:
Recent physical passport-size photographs are required to complete university registration, create your physical student dossier, issue your VIT-AP Student Identity Card & RFID Smartcard, apply for hostel residence, and register for campus bank accounts.

ISSUING AUTHORITY:
Any professional photography studio or color photo printing center.

WHERE TO OBTAIN:
Local photo studio or high-resolution photo kiosk.

STEP-BY-STEP PROCEDURE:
1. Visit a photo studio and request formal passport-size photographs (standard 3.5 cm x 4.5 cm).
2. Dress in formal or semi-formal attire (avoid white shirts if the background is white; dark or collared shirts provide good contrast).
3. Ensure a plain white or light off-white background with neutral facial expression, ears clearly visible, and no colored glasses or caps.
4. Have 10 to 12 copies printed (you will need 5-6 for reporting and remaining for hostel, bank, and library cards).
5. On the reverse side of 2-3 photos, gently write your Name and VIT Application Number using a soft ballpoint pen (avoid pressing hard to prevent photo surface indentation).

SUPPORTING DETAILS NEEDED:
- Clean formal attire.
- High-resolution studio camera.

EXECUTION & NOTARY REQUIREMENTS:
- Physical original glossy or matte photo prints.
- NO notary or gazetted officer attestation required unless requested.

MISTAKES TO AVOID:
- Do not use casual selfies, cropped group photos, or low-resolution social media pictures.
- Do not use old photos where your appearance is significantly different from your current look.

OFFICIAL REFERENCE:
VIT-AP UG Admissions Guidelines: https://vitap.ac.in',
        NOW()
    );

    -- 11. Hostel Allotment Letter
    INSERT INTO college_required_documents (
        process_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at
    ) VALUES (
        v_process_id,
        'Hostel Allotment Letter',
        'Official hostel allotment confirmation slip generated from the VTOP hostel portal confirming room type, block, and fee clearance.',
        false,
        'Printed Copy (2 Copies)',
        'WHAT IT IS & PURPOSE:
The Hostel Allotment Letter is the official room allocation slip issued by the VIT-AP Hostel Administration. It verifies that the student has been assigned a specific hostel block, room type (e.g., 2-bed, 3-bed, 4-bed, AC or Non-AC), and that hostel and mess fees have been successfully paid. It is presented at the hostel warden''s desk to collect room keys and biometric access.

WHO NEEDS IT:
Only students who have opted for and paid for on-campus hostel accommodation. Day scholars do NOT need this document.

ISSUING AUTHORITY:
Chief Warden / Hostel Administration Office, VIT-AP University.

WHERE TO OBTAIN:
Downloaded directly from the VIT-AP VTOP Hostel portal (https://vtop.vitap.ac.in).

STEP-BY-STEP PROCEDURE:
1. Log in to the VTOP portal using your student credentials.
2. Navigate to ''Hostel Management'' -> ''Room Allotment Status''.
3. Confirm that your hostel fee and mess fee payments show ''Paid / Confirmed''.
4. Check your allocated Hostel Block (e.g., Block 1, Block 2, etc.) and Room Category.
5. Click ''Download Allotment Letter / Room Confirmation Slip''.
6. Print 2 clear copies.
7. Keep one copy with your reporting folder and hand the second copy to the hostel block warden upon arrival on campus.

SUPPORTING DETAILS NEEDED:
- VTOP credentials
- Hostel fee payment transaction receipt / UTR number

EXECUTION & NOTARY REQUIREMENTS:
- Standard paper printout.
- NO stamp paper and NO notary attestation is required.

MISTAKES TO AVOID:
- Do not report to the hostel block before your scheduled reporting date and time slot.
- Ensure you have paid both hostel room fee AND mess advance, as partial payments may hold up room possession.

OFFICIAL REFERENCE:
VIT-AP VTOP Hostel Portal: https://vtop.vitap.ac.in',
        NOW()
    );

    -- 6. Insert Updates into college_process_updates
    INSERT INTO college_process_updates (process_id, title, description, update_type, effective_date, created_at)
    VALUES
    (
        v_process_id,
        'Reporting Schedule & Slot Booking Live',
        'All newly admitted B.Tech candidates must check their allotted reporting dates and slots on the VTOP portal.',
        'ANNOUNCEMENT',
        CURRENT_DATE,
        NOW()
    ),
    (
        v_process_id,
        'Mandatory Affidavit Compliance Notice',
        'Hostel and Day Scholar affidavits must be executed on non-judicial stamp paper with notary seal. Anti-ragging undertakings do NOT require stamp paper and should be printed on plain A4 paper.',
        'GENERAL',
        CURRENT_DATE,
        NOW()
    );

        RAISE NOTICE 'Successfully inserted VIT-AP process (id: %)', v_process_id;
    ELSE
        RAISE NOTICE 'VIT-AP process already exists (id: %), preserving existing records.', v_process_id;
    END IF;

    -- 7. Ensure dual-access Service exists in services (preserve existing records, do not delete)
    SELECT id INTO v_service_id FROM services WHERE slug = 'vit-ap-reporting' LIMIT 1;

    IF v_service_id IS NULL THEN
        INSERT INTO services (
        name, slug, category, description, eligibility, timeline, fees, important_notes, official_portal_url, has_questionnaire, created_at, updated_at
    ) VALUES (
        'VIT-AP Student Reporting & Document Verification',
        'vit-ap-reporting',
        'Education',
        'Official institutional reporting and document verification process for newly admitted undergraduate (B.Tech) students at Vellore Institute of Technology AP (VIT-AP). Covers online pre-reporting profile submission, fee verification, affidavit procurement, physical verification at the campus administration desk, and hostel/day-scholar clearance.',
        'Candidates who have received a provisional admission offer through VITEEE Counselling or official management allotment, completed initial tuition fee payments, and obtained their Provisional Allotment Letter.',
        'Annual Reporting Window (typically July - August as per VIT-AP academic calendar)',
        'Free (Reporting and verification is included in admission tuition; hostel fee separate)',
        'All original certificates (TC, Migration, Marksheets) must be presented physically at the verification desks along with 2-3 sets of self-attested photocopies. Affidavits (Hostel or Day Scholar) MUST be executed on non-judicial stamp paper with notary attestation. Anti-ragging undertakings do NOT require stamp paper and must be submitted on plain A4 paper from antiragging.in.',
        'https://vtop.vitap.ac.in',
        false,
        NOW(),
        NOW()
    ) RETURNING id INTO v_service_id;

    -- Add service steps
    INSERT INTO service_steps (service_id, step_number, title, description, estimated_time, notes, created_at)
    SELECT v_service_id, step_number, title, description, estimated_time, notes, NOW()
    FROM college_process_steps WHERE process_id = v_process_id;

    -- Add service documents
    INSERT INTO service_required_documents (service_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at)
    SELECT v_service_id, document_name, description, mandatory, accepted_formats, how_to_obtain, NOW()
    FROM college_required_documents WHERE process_id = v_process_id;

        RAISE NOTICE 'Successfully inserted VIT-AP civic service (id: %)', v_service_id;
    ELSE
        RAISE NOTICE 'VIT-AP civic service already exists (id: %), preserving existing records.', v_service_id;
    END IF;

    RAISE NOTICE 'VIT-AP seed check completed successfully.';
END $$;
