-- Seed Voter ID Registration & Address Change Service

DO $$
DECLARE
    v_service_id BIGINT;
BEGIN
    -- 1. Clean up existing service if present to ensure idempotency
    DELETE FROM services WHERE slug = 'voter-id-registration-address-change';

    -- 2. Insert Service Record
    INSERT INTO services (
        name,
        slug,
        category,
        description,
        eligibility,
        timeline,
        fees,
        important_notes,
        official_portal_url,
        has_questionnaire,
        created_at,
        updated_at
    ) VALUES (
        'Voter ID Registration & Address Change',
        'voter-id-registration-address-change',
        'Government & Civic Services',
        'Official Election Commission of India (ECI) service for fresh voter enrollment (Form 6), shifting of residence / address modification (Form 8), correction of existing electoral roll entries, replacement of lost or damaged Voter ID (EPIC) cards, and instant digital e-EPIC download.',
        'Indian citizen who has attained 18 years of age or will turn 18 on any of the 4 statutory qualifying dates (1st Jan, 1st Apr, 1st Jul, 1st Oct), ordinarily resident in the Assembly Constituency, and not disqualified under the Representation of the People Act, 1950.',
        '15–30 Working Days (Physical BLO Field Verification & Speed Post Delivery) | Instant for e-EPIC Download & Status Search',
        'Free of Cost (₹0.00 — No government fees are charged for Form 6, Form 8, or delivery of new PVC EPIC card)',
        'Submitting an application does not automatically guarantee enrollment; inclusion in the electoral roll strictly requires verification by the Booth Level Officer (BLO) and approval by the Electoral Registration Officer (ERO). Shifting residence automatically deletes old polling station enrollment. For lost cards, an official Police Complaint / Lost Article Report copy is mandatory. Citizens can also seek physical assistance through local Village / Ward Secretariats (Grama Ward Sachivalayam in Andhra Pradesh).',
        'https://voters.eci.gov.in/',
        true,
        NOW(),
        NOW()
    ) RETURNING id INTO v_service_id;

    -- 3. Insert Default Service Steps
    INSERT INTO service_steps (service_id, step_number, title, description, estimated_time, notes, created_at)
    VALUES
    (
        v_service_id, 1,
        'Register / Login on ECI Voters'' Services Portal',
        'Visit the official Election Commission portal (https://voters.eci.gov.in/) or download the Voter Helpline App. Register using your mobile number and verify via OTP to access citizen voter services.',
        '5 Minutes',
        'Always ensure you are using the official government domain (.gov.in) or verified Voter Helpline App.',
        NOW()
    ),
    (
        v_service_id, 2,
        'Select Relevant Form (Form 6 for New Voter / Form 8 for Shifting, Correction or Replacement)',
        'Choose the appropriate statutory form: Form 6 for brand new voter registration; Form 8 for shifting of residence, correction of entries, or replacement of lost/damaged Voter ID.',
        '5 Minutes',
        'Do not submit Form 6 if you are already registered; use Form 8 to transfer or correct your existing EPIC number.',
        NOW()
    ),
    (
        v_service_id, 3,
        'Fill Application Details & Upload Supporting Proofs',
        'Input personal, family, and address particulars. Upload clear self-attested copies of Proof of Age, Proof of Residence, or Proof of Correction, along with a recent color passport-size photograph.',
        '15–20 Minutes',
        'For lost cards, attaching an official Police Lost Report / FIR copy is mandatory.',
        NOW()
    ),
    (
        v_service_id, 4,
        'Submit Online & Note 11-Digit Reference Number',
        'Review the completed application on screen, verify all spellings, and submit. Download the acknowledgment slip containing your 11-digit Reference ID for online status tracking.',
        '2 Minutes',
        'Preserve your 11-digit Reference ID carefully to monitor the verification stages.',
        NOW()
    ),
    (
        v_service_id, 5,
        'Booth Level Officer (BLO) Physical Field Verification',
        'The Electoral Registration Officer (ERO) assigns your application to the designated Booth Level Officer (BLO), who visits your residence to verify original documents and confirm physical occupancy.',
        '7–15 Days',
        'Keep original documents ready at home for the BLO physical visit.',
        NOW()
    ),
    (
        v_service_id, 6,
        'Electoral Roll Publication & Free PVC Card Speed Post Delivery',
        'Upon ERO statutory approval, your name is published in the electoral roll. You can immediately download your digital e-EPIC PDF, and a high-security PVC Voter Card is dispatched via Speed Post free of cost.',
        'Within 30 Days',
        'Delivery of the new PVC EPIC card via India Post Speed Post is completely free.',
        NOW()
    );

    -- 4. Insert Default Service Required Documents
    INSERT INTO service_required_documents (service_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at)
    VALUES
    (
        v_service_id,
        'Proof of Date of Birth & Age (Birth Certificate / Aadhaar / 10th Marksheet / PAN / DL)',
        'Statutory document proving date of birth and age eligibility (18+ on qualifying date).',
        true,
        'Self-Attested PDF / JPG (Max 2MB)',
        'WHAT IT IS: Official record verifying your exact date of birth.
ACCEPTED DOCUMENTS: Birth Certificate issued by Municipal Authority/Registrar of Births, Aadhaar Card, PAN Card, Driving Licence, or Class 10th / Matriculation Marksheet/Certificate showing DOB.
WHERE TO OBTAIN: DigiLocker (digilocker.gov.in), municipal corporation, school education board, or respective issuing authorities.
HOW TO OBTAIN: Download verified digital copy from DigiLocker or scan the original hardcopy.
COMMON MISTAKES: Submitting documents where the name or date of birth differs from your voter application.',
        NOW()
    ),
    (
        v_service_id,
        'Proof of Ordinary Residence (Aadhaar / Utility Bill / Bank Passbook / Indian Passport / Rent Agreement)',
        'Document proving ordinary residence in the applied Assembly Constituency.',
        true,
        'Self-Attested PDF / JPG (Max 2MB)',
        'WHAT IT IS: Official record establishing ordinary residence at the applied address within the constituency.
ACCEPTED DOCUMENTS: Latest Water/Electricity/Gas connection bill (under 3 months old in applicant''s, parent''s, or spouse''s name), Aadhaar Card, Current Bank/Post Office Passbook, Indian Passport, or Registered Rent/Lease Deed.
WHERE TO OBTAIN: Electricity distribution company, municipal water board, bank branch, or UIDAI.
HOW TO OBTAIN: Download latest online bill or obtain bank passbook statement. If renting, ensure the rent agreement is registered.
COMMON MISTAKES: Submitting temporary hostel addresses without permanent parental proof, or utility bills older than 3 months.',
        NOW()
    ),
    (
        v_service_id,
        'Recent Color Passport-Sized Photograph (White Background)',
        'Recent color portrait photo taken within the last 3 months with a clean white/light background.',
        true,
        'JPG / JPEG (3.5 cm x 4.5 cm, Max 2MB)',
        'WHAT IT IS: Formal passport photograph printed on your physical PVC Voter ID card and electoral roll.
SPECIFICATIONS: 3.5 cm x 4.5 cm color photo, front-facing, neutral expression, eyes open, ears clearly visible, clean white background. No sunglasses, uniforms, or caps.
WHERE TO OBTAIN: Any photo studio or high-resolution smartphone camera with good lighting against a plain white wall.
COMMON MISTAKES: Uploading casual selfies, cropped group photos, or low-resolution social media pictures.',
        NOW()
    ),
    (
        v_service_id,
        'Existing Voter ID (EPIC) Number (For Form 8 / Shifting / Correction / Replacement)',
        '10-character alphanumeric Electoral Photo Identity Card (EPIC) number currently allotted to you.',
        false,
        '10-Character Alphanumeric Code',
        'WHAT IT IS: Your unique voter identification number (e.g., ABC1234567) printed on the front of your existing Voter Card.
WHERE TO FIND IT: On your existing physical Voter Card, previous Voter Information Slip, or search by details at electoralsearch.eci.gov.in.
HOW TO USE: Enter in Form 8 to automatically fetch your current polling station details.',
        NOW()
    ),
    (
        v_service_id,
        'Police FIR / Online Lost Article Report Copy (Mandatory ONLY for Lost Cards)',
        'Official police acknowledgement verifying reporting of the lost Voter ID card.',
        false,
        'Signed PDF / Police Stamp Copy',
        'WHAT IT IS: Official report filed with local police station or via State Police Citizen Portal reporting the loss of your Voter ID.
WHERE TO OBTAIN: Online citizen portal of State Police (e.g. AP Police Citizen Services at appolice.gov.in) or local police station.
CONDITIONS: Mandatory ONLY if the replacement reason selected is ''Lost''. Not required if destroyed by fire/flood or mutilated.',
        NOW()
    ),
    (
        v_service_id,
        'Documentary Proof Supporting Requested Correction (Marksheet / Gazette / Aadhaar / Passport)',
        'Statutory proof establishing the correct detail (Marksheet, Birth Certificate, Gazette, or Marriage Certificate).',
        false,
        'Self-Attested PDF / JPG (Max 2MB)',
        'WHAT IT IS: Official government or educational record verifying the exact corrected data.
FOR NAME CORRECTION: Official Gazette notification, Marriage Certificate (for surname change post-marriage), or Passport.
FOR DOB CORRECTION: Birth Certificate, Class 10th marksheet, or Aadhaar Card.
FOR RELATIVE NAME: School leaving certificate or family ration card.',
        NOW()
    );

    RAISE NOTICE 'Successfully seeded Voter ID Registration & Address Change service with id %', v_service_id;
END $$;
