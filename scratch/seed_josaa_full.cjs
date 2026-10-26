const { execFile } = require('child_process');
const psqlPath = 'C:\\Program Files\\PostgreSQL\\18\\bin\\psql.exe';
const env = { ...process.env, PGPASSWORD: 'qwe', PGCLIENTENCODING: 'UTF8' };

const description = "Official centralized and state technical seat allocation framework covering 23 IITs, 31 NITs, 26 IIITs, IIEST Shibpur, 38 GFTIs, and Andhra Pradesh state engineering colleges (JNTUK, JNTUA, AU, SVU). Features guided choice filling, dual eligibility evaluation, Freeze/Float/Slide decision rules, and comprehensive document verification.";
const eligibility = "Candidates must satisfy dual qualifications: (1) Competitive entrance exam qualification in JEE (Advanced) for IITs, JEE (Main) Paper 1 for NITs/IIITs/GFTIs, or AP EAPCET for state engineering; AND (2) Class 12 aggregate board score of >= 75% (or Top 20 percentile in board) for general central seats (65% for SC/ST/PwD) or >= 45% aggregate in PCM (40% for reserved) for AP state engineering.";
const timeline = "June - August (6 JoSAA Rounds + 2 CSAB Special Rounds / 3 AP State Rounds)";
const fees = "SAF: Rs 35,000 (GEN/OBC/EWS) / Rs 15,000 (SC/ST/PwD) - Tuition adjusted | AP EAPCET: Rs 1,200 (OC/BC) / Rs 600 (SC/ST) | Registration: Free";
const notes = "STRICT REPORTING MANDATE: If a seat is allotted in ANY JoSAA round, candidate must pay SAF and complete online reporting; failure to do so results in seat forfeiture and PERMANENT DEBARMENT from subsequent rounds. OBC-NCL and EWS certificates MUST be issued ON or AFTER April 1 of admission year in Central format. Medical certificate in mandatory Annexure-7 format required.";
const portal = "https://josaa.nic.in";

const sql = `
ALTER TABLE services ALTER COLUMN fees TYPE VARCHAR(300);
ALTER TABLE services ALTER COLUMN timeline TYPE VARCHAR(300);

UPDATE services SET
  description = '${description.replace(/'/g, "''")}',
  eligibility = '${eligibility.replace(/'/g, "''")}',
  timeline = '${timeline.replace(/'/g, "''")}',
  fees = '${fees.replace(/'/g, "''")}',
  important_notes = '${notes.replace(/'/g, "''")}',
  official_portal_url = '${portal}'
WHERE slug = 'josaa-state-admissions-counselling';

DO $$
DECLARE
  v_id BIGINT;
BEGIN
  SELECT id INTO v_id FROM services WHERE slug = 'josaa-state-admissions-counselling';
  IF v_id IS NOT NULL THEN
    DELETE FROM service_steps WHERE service_id = v_id;
    DELETE FROM service_required_documents WHERE service_id = v_id;

    INSERT INTO service_steps (service_id, step_number, title, description, estimated_time, notes, created_at) VALUES
    (v_id, 1, 'Portal Authentication & Profile Verification', 'Log into official JoSAA portal (https://josaa.nic.in) or AP EAPCET portal (https://cets.apsche.ap.gov.in) using JEE Application Number / EAPCET Hall Ticket and Password. Carefully verify state code of eligibility, gender, and category details.', '15-20 Minutes', 'Ensure your state code of eligibility matches the state where you actually appeared for Class 12.', NOW()),
    (v_id, 2, 'Strategic Choice Filling & Priority Arrangement', 'Fill college and branch preferences in STRICT order of your authentic personal preference. You have UNLIMITED choices. Put your dream programs at the top, realistic choices in the middle, and reliable backup options at the bottom.', '2-3 Days', 'Golden Rule: Never place a lower-preferred option above a higher-preferred option simply because of previous year cutoffs.', NOW()),
    (v_id, 3, 'Mock Allotment Review & Mandatory Choice Locking', 'Review the two official Mock Allotment results published to understand indicative seat trends. Make necessary adjustments. Ensure you LOCK your choices before the announced deadline timestamp.', '1 Hour', 'If not manually locked, system auto-locks your last saved preferences at the deadline.', NOW()),
    (v_id, 4, 'Seat Allotment Result & Mandatory Willingness Selection', 'Upon announcement of allotment results, log in immediately. If a seat is allotted, you MUST select one of three mandatory willingness options: Freeze (accept & exit), Float (accept & upgrade across any institute), or Slide (accept & upgrade within same institute).', 'Same Day', 'Selecting Float or Slide guarantees your current seat while keeping you in contention for higher choices.', NOW()),
    (v_id, 5, 'Seat Acceptance Fee (SAF) Payment & Document Upload', 'Pay the mandatory Seat Acceptance Fee online (Rs 35,000 for Gen/OBC/EWS, Rs 15,000 for SC/ST/PwD) via Net Banking, UPI, or Card. Upload clear scans of required certificates.', '1-2 Hours', 'CRITICAL: Failure to pay SAF or upload documents leads to permanent debarment from subsequent rounds.', NOW()),
    (v_id, 6, 'Online Document Verification & Query Resolution (24-Hour SLA)', 'Virtual Reporting Centre (VRC) officers verify uploaded certificates. If an officer finds a discrepancy, an online query is raised. You MUST respond within 24 hours.', '24 Hours Max', 'Check your candidate portal twice daily during reporting windows.', NOW()),
    (v_id, 7, 'Subsequent Rounds & CSAB Special Round Participation', 'Monitor rounds 2 through 6. Your seat may upgrade automatically based on your Float/Slide selection. After Round 6, participate in CSAB Special Rounds (csab.nic.in) for vacant NIT+ seats.', '1-2 Weeks', 'Seats secured in JoSAA can be retained while exploring upgrades in CSAB Special Rounds.', NOW()),
    (v_id, 8, 'Physical Reporting & Institutional Admission', 'Download Final Provisional Seat Allotment Letter. Pay remaining institutional admission tuition fees online, and report physically to the allotted campus with all original documents.', 'Designated Dates', 'Carry at least 3 sets of self-attested photocopies, 6 photos, and medical certificate.', NOW());

    INSERT INTO service_required_documents (service_id, document_name, description, mandatory, accepted_formats, how_to_obtain, created_at) VALUES
    (v_id, 'Class 10 Certificate / Marks Sheet', 'Statutory proof of candidate date of birth (DOB) and candidate name matching entrance application exactly.', true, 'Original PDF scan (under 500 KB)', 'Issued by CBSE / ICSE / State Secondary Board. DigiLocker verified copy is acceptable.', NOW()),
    (v_id, 'Class 12 Marksheet & Passing Certificate', 'Proof of qualifying examination and aggregate percentage across 5 compulsory subjects (Physics, Math, Chem/Vocational, Language, 5th subject).', true, 'Original PDF scan (under 500 KB)', 'Must clearly show individual subject marks and aggregate percentage.', NOW()),
    (v_id, 'Entrance Examination Admit Card & Rank Card', 'JEE Main, JEE Advanced, or AP EAPCET Admit Card and official Rank Score Card showing CRL and Category Rank.', true, 'Original PDF download', 'Download directly from NTA (jeemain.nta.ac.in), JEE Adv (jeeadv.ac.in), or APSCHE (cets.apsche.ap.gov.in).', NOW()),
    (v_id, 'Medical Fitness Certificate (Mandatory Annexure-7 Format)', 'Official JoSAA Annexure-7 medical certificate verifying physical fitness, visual acuity, color perception, and general health.', true, 'Original signed & stamped by MBBS Doctor', 'Download official Annexure-7 template from josaa.nic.in. Must be certified by a Registered Medical Practitioner (MBBS).', NOW()),
    (v_id, 'Valid Government Photo Identity Card', 'Aadhaar Card, Passport, Voter ID, or School/College Photo ID proving candidate identity.', true, 'Original color PDF scan', 'Download mask-free e-Aadhaar from uidai.gov.in.', NOW()),
    (v_id, 'Provisional Seat Allotment Letter & SAF Fee Payment Receipt', 'Official allotment letter generated by JoSAA / CSAB / AP EAPCET portal after seat allocation, together with payment acknowledgement receipt.', true, 'Portal generated PDF', 'Download from candidate dashboard immediately after paying the Seat Acceptance Fee.', NOW()),
    (v_id, 'OBC-NCL / EWS Certificate (STRICT: Issued ON or AFTER April 1)', 'Mandatory Central Government format certificate. Strict Rule: Must be issued ON or AFTER April 1 of the admission year. If issued prior, candidate is converted to General category.', false, 'Central DoPT Format PDF Scan', 'Obtain from Tahsildar / Revenue Divisional Officer (RDO) / Sub-Divisional Magistrate (SDM).', NOW()),
    (v_id, 'SC / ST Community & Tribe Certificate', 'Permanent caste/community certificate issued in the prescribed Central format by authorized revenue officer (Tahsildar / MRO).', false, 'Original PDF scan', 'Issued by revenue department / MeeSeva with verifiable digital certificate number and government seal.', NOW()),
    (v_id, 'PwD Benchmark Disability Certificate / UDID Card', 'Disability certificate issued by authorized government Medical Board confirming minimum 40% benchmark disability.', false, 'Unique Disability ID (UDID) Card', 'Swavlamban UDID card or certificate from District Chief Medical Officer (CMO).', NOW()),
    (v_id, 'State Code of Eligibility Proof / Domicile Certificate', 'Class 12 examination passing certificate proving the state where Class 12 was completed (for 50% Home State NIT quota and 85% AP local quota).', true, 'College Study / Bonafide / Passing Certificate', 'Class 12 school leaving / study certificate from intermediate college or residence certificate.', NOW()),
    (v_id, 'Crossed Bank Cancelled Cheque / Passbook Photocopy', 'Cancelled bank cheque or front page of passbook showing Account Holder Name, Account Number, and IFSC code (student or parent).', true, 'Original scan of cancelled cheque', 'Mandatory for verification in case of seat cancellation, round withdrawal, or refund of excess fee.', NOW());
  END IF;
END $$;
`;

execFile(psqlPath, ['-U', 'postgres', '-d', 'onepath_db', '-c', sql], { env }, (err, stdout, stderr) => {
  if (err) {
    console.error('Error:', err.message, stderr);
    return;
  }
  console.log('Seeded JoSAA successfully:\n', stdout);

  execFile(psqlPath, ['-U', 'postgres', '-d', 'onepath_db', '-c', 'SELECT s.id, s.name, count(st.id) as steps_count FROM services s LEFT JOIN service_steps st ON s.id = st.service_id GROUP BY s.id, s.name; SELECT s.id, s.name, count(d.id) as docs_count FROM services s LEFT JOIN service_required_documents d ON s.id = d.service_id GROUP BY s.id, s.name;'], { env }, (err2, stdout2) => {
    if (err2) console.error(err2);
    else console.log(stdout2);
  });
});
