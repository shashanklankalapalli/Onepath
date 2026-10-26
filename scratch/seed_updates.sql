
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM platform_updates WHERE title = 'UIDAI Revised Standard Operating Procedure (SOP) for Aadhaar Name Changes') THEN
        INSERT INTO platform_updates (
            title, description, category, related_service_or_process, effective_date, urgent, official_source_url, created_at
        ) VALUES (
            'UIDAI Revised Standard Operating Procedure (SOP) for Aadhaar Name Changes',
            'The Unique Identification Authority of India (UIDAI) has implemented a revised Standard Operating Procedure (SOP) governing resident name modifications in the Aadhaar database, effective 18 September 2026.' || E'

' ||
            '• Minor Typographical Corrections: Minor spelling, phonetics, spacing, or transliteration adjustments between English and regional languages require standard Proof of Identity (PoI) documentation (such as Passport, PAN Card, or Voter ID).' || E'
' ||
            '• Formal Legal Name Changes: Major name changes, surname additions post-marriage, or complete legal name alterations require specialized legal validation—including an official Central/State Gazette Notification and original Marriage Certificate where applicable.' || E'
' ||
            '• Verification Tiers: Aadhaar Seva Kendras (ASK) now enforce tiered biometric verification and document scrutiny based on the applicant''s lifetime update threshold (maximum 2 lifetime updates permitted without Regional Office exception approval).',
            'Policy & Rules',
            'Aadhaar Card Update & Correction',
            '2026-09-18',
            false,
            'https://uidai.gov.in/',
            NOW()
        );
        RAISE NOTICE 'Aadhaar Name Change SOP update inserted.';
    ELSE
        RAISE NOTICE 'Aadhaar Name Change SOP update already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM platform_updates WHERE title = 'MoRTH Draft Notification: Proposed Driving Licence Rules & Documentation Amendments') THEN
        INSERT INTO platform_updates (
            title, description, category, related_service_or_process, effective_date, urgent, official_source_url, created_at
        ) VALUES (
            'MoRTH Draft Notification: Proposed Driving Licence Rules & Documentation Amendments',
            '[STATUS: DRAFT PROPOSAL — NOT CURRENTLY IN FORCE]' || E'

' ||
            'The Ministry of Road Transport and Highways (MoRTH) has published proposed draft amendments to the Central Motor Vehicles Rules (CMVR) regarding driving licence administration and citizen documentation:' || E'

' ||
            '• Online Service Streamlining: Proposals seek to eliminate in-person RTO visits for DL address updates, badge renewals, and duplicate licence requests through paperless Aadhaar e-KYC integration.' || E'
' ||
            '• Automated Testing Standardisation: Driving skill tests on accredited Automated Driving Test Tracks (ADTT) will follow standardized sensor-based evaluation metrics nationwide.' || E'
' ||
            '• Digital Medical Certificate (Form 1A): Direct electronic transmission of medical fitness certifications from registered medical practitioners to the Parivahan Sarathi portal.' || E'

' ||
            'IMPORTANT NOTICE: This measure is an official draft amendment issued for public stakeholder objections and suggestions; it does NOT constitute an active statutory rule until final notification is gazetted.',
            'Policy & Rules',
            'Driving Licence & Vehicle Registration',
            '2026-10-15',
            false,
            'https://parivahan.gov.in/',
            NOW()
        );
        RAISE NOTICE 'MoRTH DL draft amendment update inserted.';
    ELSE
        RAISE NOTICE 'MoRTH DL draft amendment update already exists.';
    END IF;
END $$;

SELECT id, title, category, related_service_or_process, effective_date, official_source_url FROM platform_updates;
