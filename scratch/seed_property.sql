
DO $$
DECLARE
    v_service_id BIGINT;
BEGIN
    SELECT id INTO v_service_id FROM services WHERE slug = 'property-land-revenue-registration';

    IF v_service_id IS NULL THEN
        INSERT INTO services (name, slug, category, description, eligibility, timeline, fees, important_notes, official_portal_url, has_questionnaire, created_at, updated_at)
        VALUES (
            'Property / Land Revenue Registration',
            'property-land-revenue-registration',
            'Government & Civic Services',
            'Official statutory guidance framework for property conveyancing, sale deed registration, Encumbrance Certificate (EC Form 15/16) verification, market guidance values (circle rates), and post-registration revenue land mutation (RoR-1B/Patta) through the Andhra Pradesh Registration and Stamps Department (AP IGRS) and AP Revenue Department.',
            'All Indian citizens, NRIs, and legal entities executing property transactions within Andhra Pradesh. Parties must be competent to contract under the Indian Contract Act (18+ years, sound mind). Crucial requirement: Property must NOT be listed under the Section 22A Prohibited Properties Register (assigned lands, ceiling surplus, or wakf/endowments).',
            'SRO Registration: Same-Day (1–3 Hours) | Auto-Mutation Trigger: 24 Hours | Final Revenue Mutation (Patta): 15–30 Days | Online EC: Instant to 24 Hours',
            'Typical AP: ~5% Stamp Duty + 1% Reg Fee + local cess (based on higher of consideration or circle rate). Online EC: ' || chr(8377) || '25–' || chr(8377) || '50. Verify: https://registration.ap.gov.in/',
            'Statutory Guidance Notice: This service provides general workflow guidance based on AP Registration & Stamps Department and Revenue Department procedures. It is not formal legal advice. Mandatory compliance: (1) Section 22A check is critical before entering sale agreements; (2) For transactions >= ' || chr(8377) || '50 Lakhs, buyer must deduct 1% TDS under Section 194-IA via Form 26QB; (3) Registration at SRO alone does not complete revenue ownership—revenue mutation in Webland/RoR-1B is essential.',
            'https://registration.ap.gov.in/',
            true,
            NOW(),
            NOW()
        ) RETURNING id INTO v_service_id;

        -- Insert 9 Steps
        INSERT INTO service_steps (service_id, step_number, title, description, estimated_time, notes, created_at) VALUES
        (v_service_id, 1, 'Identify Required Service & Preliminary Due Diligence', 'Determine your exact transaction type (Sale deed, Gift deed, Lease, Encumbrance Certificate, or Mutation). Conduct preliminary title inspection and physical site inspection to verify possession.', '1–3 Days', 'Ensure seller has registered legal title and clear possession without boundary disputes.', NOW()),
        (v_service_id, 2, 'Section 22A Prohibited Properties Register Verification', 'Check the official AP IGRS portal (registration.ap.gov.in) to verify that the survey number / parcel is NOT listed under Section 22A prohibited categories (government-assigned, ceiling surplus, or wakf/endowments).', '15 Minutes', 'Section 22A barred lands cannot be registered under AP law; any attempted registration will be summarily refused.', NOW()),
        (v_service_id, 3, '30-Year Encumbrance Certificate (EC Form 15) Inspection', 'Search and download an unbroken 30-year Encumbrance Certificate via AP IGRS or MeeSeva to confirm title is free from mortgages, court decrees, or attachment orders.', '1 Day', 'Check that the seller is the recorded owner and no unresolved bank loans exist on the property.', NOW()),
        (v_service_id, 4, 'Sale Deed Drafting with Boundary Schedule & Survey Details', 'Engage a licensed deed writer or legal counsel to draft the conveyance deed incorporating complete survey numbers, sub-divisions, exact extent, and four-side boundary descriptions (North/South/East/West).', '1–2 Days', 'Seller and buyer names must match Aadhaar cards and prior title deeds 100%.', NOW()),
        (v_service_id, 5, 'Market Value (Circle Rate) Calculation & Stamp Duty Assessment', 'Use the AP IGRS Market Value Calculator to determine the statutory circle rate. Compute applicable stamp duty (~5%) and registration fee (~1%). Duty is payable on the higher of circle rate or sale price.', '30 Minutes', 'Ensure consideration is not under-valued below official market guidance value.', NOW()),
        (v_service_id, 6, 'Section 194-IA 1% TDS Remittance via Form 26QB (if >= ' || chr(8377) || '50L)', 'For all property transactions valued at ' || chr(8377) || '50 Lakhs or above, the buyer must deduct 1% TDS from the total consideration and deposit it via Form 26QB on the Income Tax e-filing portal.', '1 Day', 'The Sub-Registrar requires the Form 26QB payment challan before registering high-value deeds.', NOW()),
        (v_service_id, 7, 'E-Stamp / e-Challan Payment & SRO Appointment Slot Booking', 'Generate e-Stamp paper via Stock Holding Corporation (SHCIL) or state CFMS e-Challan payment portal. Book a physical appointment slot at the jurisdictional Sub-Registrar Office.', '1 Day', 'Select an appointment date and slot convenient for all parties and witnesses.', NOW()),
        (v_service_id, 8, 'Physical Appearance, Biometric Capture & Execution at SRO', 'Buyer, seller, and two adult witnesses attend the SRO counter with originals. Complete live thumbprint scanning and webcam photo capture before the Sub-Registrar.', '1–3 Hours', 'Sub-Registrar formally endorses deed numbers and records the transaction in Book 1 / Index II.', NOW()),
        (v_service_id, 9, 'Collect Registered Deed (Index II) & Verify Auto-Mutation in Webland', 'Collect original registered sale deed with official seals; confirm electronic auto-mutation trigger to the Tahsildar for updating revenue land records (RoR-1B/Patta).', 'Same Day', 'Track Tahsildar mutation status on MeeSeva/Webland; updated passbook is issued within 15–30 days.', NOW());

        -- Insert 8 Documents
        INSERT INTO service_required_documents (service_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at) VALUES
        (v_service_id, 'Identity & Address Proofs (Aadhaar & PAN) of Buyer, Seller & 2 Witnesses', 'Mandatory biometric and identity verification documents for all executing parties appearing before the Sub-Registrar.', true, 'Original Aadhaar & PAN Card + 1 self-attested photocopy each', 'Carry original cards physically to SRO counter.', NOW()),
        (v_service_id, 'Chain of Parent / Link Deeds (Minimum 30 Years)', 'Original title deeds establishing unbroken chronological sequence of ownership leading to the seller.', true, 'Original deeds + complete photocopy sets', 'Procure from seller or financial institution release.', NOW()),
        (v_service_id, 'Drafted Sale Deed on Legal Stamp Paper / E-Stamp Certificate', 'Conveyance deed incorporating property schedule, boundaries, consideration, and witness attestations.', true, 'Physical deed with e-Stamp certificate / CFMS challan', 'Drafted through licensed deed writer or legal counsel.', NOW()),
        (v_service_id, 'Encumbrance Certificate (EC Form 15 covering 30 Years)', 'Official search certificate proving title is unencumbered by mortgages, liens, or court attachments.', true, 'Digitally signed Form 15 extract', 'Download from registration.ap.gov.in or MeeSeva.', NOW()),
        (v_service_id, 'Property Schedule with Survey Number, Sub-division & Boundaries', 'Detailed technical schedule identifying survey number, plot/flat number, extent, and exact 4-side boundaries.', true, 'Schedule annexure in deed with layout sketch', 'Extracted from prior deeds, FMB sketch, or approved layout.', NOW()),
        (v_service_id, 'Latest Revenue Records (Pattadar Passbook / RoR-1B Extract & Adangal)', 'Official revenue records proving seller''s registered ownership and physical possession of agricultural parcel.', false, 'Original e-Pattadar Passbook + Meebhoomi 1B extract', 'Download from meebhoomi.ap.gov.in or MeeSeva.', NOW()),
        (v_service_id, 'Approved Building Sanction Plan / DTCP Layout Approval', 'Competent authority layout or building construction approval (APCRDA / DTCP / Municipal Corporation).', false, 'Approved blueprint / sanction order copy', 'Obtain from builder or municipal town planning wing.', NOW()),
        (v_service_id, 'Section 194-IA 1% TDS Payment Challan (Form 26QB)', 'Statutory income tax compliance certificate required for all property sales valued at ' || chr(8377) || '50 Lakhs or above.', false, 'Form 26QB acknowledgement challan printout', 'File online on Income Tax e-filing portal (eportal.incometax.gov.in).', NOW());

        RAISE NOTICE 'Property / Land Revenue Registration service inserted with ID %', v_service_id;
    ELSE
        RAISE NOTICE 'Property / Land Revenue Registration service already exists with ID %', v_service_id;
    END IF;
END $$;

SELECT id, name, slug, category, has_questionnaire FROM services WHERE slug = 'property-land-revenue-registration';
