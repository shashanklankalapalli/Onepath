package com.onepath.config;

import com.onepath.entity.Role;
import com.onepath.entity.User;
import com.onepath.repository.CollegeRepository;
import com.onepath.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final com.onepath.repository.ServiceRepository serviceRepository;
    private final com.onepath.repository.ServiceCenterRepository serviceCenterRepository;
    private final com.onepath.repository.UpdateRepository updateRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Value("${app.seed.sample-data:false}")
    private boolean seedSampleData;

    public DataInitializer(
            UserRepository userRepository,
            CollegeRepository collegeRepository,
            com.onepath.repository.ServiceRepository serviceRepository,
            com.onepath.repository.ServiceCenterRepository serviceCenterRepository,
            com.onepath.repository.UpdateRepository updateRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.collegeRepository = collegeRepository;
        this.serviceRepository = serviceRepository;
        this.serviceCenterRepository = serviceCenterRepository;
        this.updateRepository = updateRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void migrateSchemaIfNeeded() {
        if (jdbcTemplate == null) {
            return;
        }
        try {
            log.info("Ensuring database columns for services and college_processes support long text...");
            jdbcTemplate.execute("ALTER TABLE services ALTER COLUMN fees TYPE TEXT");
            jdbcTemplate.execute("ALTER TABLE services ALTER COLUMN timeline TYPE TEXT");
            jdbcTemplate.execute("ALTER TABLE college_processes ALTER COLUMN fees TYPE TEXT");
            jdbcTemplate.execute("ALTER TABLE college_processes ALTER COLUMN timeline TYPE TEXT");
            log.info("Database schema migration completed successfully (columns 'fees' and 'timeline' are TEXT).");
        } catch (Exception e) {
            log.warn("Automatic column type migration notice (table might not exist yet or already altered): {}", e.getMessage());
        }
    }

    @Override
    public void run(String... args) {
        migrateSchemaIfNeeded();

        // Initialize default administrator accounts if none exist
        if (!userRepository.existsByEmail("admin@onepath.org")) {
            User admin = new User(
                    "System Administrator",
                    "admin@onepath.org",
                    passwordEncoder.encode("admin123"),
                    Role.SUPER_ADMIN
            );
            userRepository.save(admin);
            log.info("Default Super Admin created: admin@onepath.org / admin123");
        }

        if (!userRepository.existsByEmail("student@onepath.org")) {
            User student = new User(
                    "Demo Student",
                    "student@onepath.org",
                    passwordEncoder.encode("student123"),
                    Role.USER
            );
            userRepository.save(student);
            log.info("Default Demo Student created: student@onepath.org / student123");
        }

        // Demo Student Account: test1@onepath.com / test123 (Role: STUDENT -> USER)
        java.util.Optional<User> existingTest1 = userRepository.findByEmail("test1@onepath.com");
        if (existingTest1.isPresent()) {
            User s = existingTest1.get();
            s.setPasswordHash(passwordEncoder.encode("test123"));
            s.setRole(Role.USER);
            userRepository.save(s);
            log.info("Configured Demo Student account: test1@onepath.com");
        } else {
            User s = new User(
                    "Student",
                    "test1@onepath.com",
                    passwordEncoder.encode("test123"),
                    Role.USER
            );
            userRepository.save(s);
            log.info("Created Demo Student account: test1@onepath.com / test123 (Role: USER/STUDENT)");
        }

        // Demo Admin Account: admin@onepath.com / admin123 (Role: ADMIN -> SUPER_ADMIN)
        java.util.Optional<User> existingAdminCom = userRepository.findByEmail("admin@onepath.com");
        if (existingAdminCom.isPresent()) {
            User a = existingAdminCom.get();
            a.setPasswordHash(passwordEncoder.encode("admin123"));
            a.setRole(Role.SUPER_ADMIN);
            userRepository.save(a);
            log.info("Configured Demo Admin account: admin@onepath.com");
        } else {
            User a = new User(
                    "System Administrator",
                    "admin@onepath.com",
                    passwordEncoder.encode("admin123"),
                    Role.SUPER_ADMIN
            );
            userRepository.save(a);
            log.info("Created Demo Admin account: admin@onepath.com / admin123 (Role: SUPER_ADMIN/ADMIN)");
        }

        // Initialize Passport Application & Renewal service
        java.util.Optional<com.onepath.entity.Service> existingPassport = serviceRepository.findBySlug("passport-application-renewal");
        String officialFees = "Normal: ₹2,500 (36 pages) / ₹3,500 (60 pages) | Minor: ₹1,750 | Lost/Damaged: ₹5,000 / ₹6,000 | Tatkaal: ₹5,000 (36 pages) / ₹6,000 (60 pages) [Replacement: ₹7,500 / ₹8,500] | Fee Calculator: https://www.passportindia.gov.in/psp/FeeCalculator";
        if (existingPassport.isPresent()) {
            com.onepath.entity.Service passportService = existingPassport.get();
            passportService.setFees(officialFees);
            serviceRepository.save(passportService);
        } else {
            com.onepath.entity.Service passportService = new com.onepath.entity.Service();
            passportService.setName("Passport Application & Renewal");
            passportService.setSlug("passport-application-renewal");
            passportService.setCategory("Government & Civic Services");
            passportService.setDescription("Official Ministry of External Affairs (MEA) service for issuing new ordinary Indian passports (36/60 pages) and processing renewals or re-issues for students and citizens.");
            passportService.setEligibility("Indian citizen by birth, descent, registration, or naturalization. Adults (18+) receive 10-year validity passports. Minors (<18) receive 5-year validity or until age 18. Special provisions allow university students residing in hostels/PGs to apply using student ID, bonafide, and hostel warden certificates.");
            passportService.setTimeline("Normal: 7–15 Working Days (post-Police Verification) | Tatkaal: 1–3 Working Days");
            passportService.setFees(officialFees);
            passportService.setImportantNotes("Student Dual Address Rule: Enter hostel address under Present Address and permanent parental address under Permanent Address. Name matching across Aadhaar, 10th Mark Sheet, and Application Form must match 100%. Keep 2 local witness references ready for police verification.");
            passportService.setOfficialPortalUrl("https://www.passportindia.gov.in");
            passportService.setHasQuestionnaire(true);

            // Steps
            passportService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "User Registration on Passport Portal",
                    "Register on official MEA portal (https://www.passportindia.gov.in) under New User section with name matching 10th marksheet and activate account via email.",
                    "10–15 Minutes",
                    "Always ensure you are using the official government domain .gov.in."
            ));
            passportService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Filling Online Application & Scheme Selection",
                    "Log in, select Fresh or Re-issue, choose scheme (Normal/Tatkaal) and booklet size (36/60 pages). Fill personal, residential, and family details to generate ARN.",
                    "20–30 Minutes",
                    "Students staying in hostels/PGs must fill campus address as Present Address and parents' home as Permanent Address."
            ));
            passportService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Payment & Appointment Slot Booking",
                    "Pay applicable fee online via UPI, Net Banking, or Card and book preferred PSK/POPSK appointment slot. Print Appointment Slip.",
                    "10 Minutes",
                    "Appointment slip must be carried physically to the PSK."
            ));
            passportService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "PSK Appointment Day Walkthrough (Counters A -> B -> C)",
                    "Arrive 15 mins prior with originals and self-attested photocopies. Go through Token Counter, Counter A (CSE biometrics, photo, scan), Counter B (VO verification), and Counter C (GO granting decision).",
                    "1–2 Hours",
                    "Collect Exit Slip / Acknowledgement Receipt before leaving the center."
            ));
            passportService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Police Verification (PV) Process",
                    "Pre-Police Verification occurs for standard fresh applications before printing; Post-Police Verification occurs for Tatkaal; Police verification is exempted for renewals with unchanged address and clear history.",
                    "3–7 Working Days",
                    "Have photocopies and 2 local witness references ready."
            ));
            passportService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "Printing, Speed Post Dispatch & In-Person Delivery",
                    "Once granted and cleared, passport is printed at Security Printing Press and dispatched exclusively via Speed Post with SMS tracking.",
                    "1–15 Working Days",
                    "Delivery rule: Envelope is handed over ONLY to the applicant in person upon verifying physical photo ID."
            ));

            // Documents
            passportService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Present Residential Address",
                    "Aadhaar Card / e-Aadhaar (UIDAI verifiable), Photo Bank Passbook, Utility bill (last 3 months), or Registered Rent Agreement.",
                    true,
                    "Original + 1 self-attested copy",
                    "Download mask-free e-Aadhaar from uidai.gov.in or update bank passbook at branch."
            ));
            passportService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Date of Birth (DOB)",
                    "Aadhaar Card, Birth Certificate, 10th Secondary School Marksheet/Leaving Certificate, PAN Card, or Driving Licence.",
                    true,
                    "Original + 1 self-attested copy",
                    "DigiLocker certificate or municipal corporation birth certificate."
            ));
            passportService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Non-ECR (ECNR Status)",
                    "10th Class Passing Certificate / Mark Sheet, Higher Secondary Certificate, or College Degree.",
                    true,
                    "Original + 1 self-attested copy",
                    "DigiLocker copy or educational board/university duplicate marksheet."
            ));
            passportService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "College/University Student ID & Hostel Proof",
                    "Special proof for students: Student ID Card, Bonafide Certificate from institution head/registrar, and Hostel Warden Certificate.",
                    false,
                    "Originals + 1 set photocopies",
                    "Obtain from college student affairs and hostel warden office."
            ));
            passportService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Original Old Passport & Self-Attested Photocopies",
                    "Mandatory for renewals/re-issues: Physical old booklet plus self-attested copies of first 2 pages, last 2 pages, Non-ECR page, and observation pages.",
                    false,
                    "Original booklet + self-attested copies",
                    "Required for cancellation and verification at PSK."
            ));

            serviceRepository.save(passportService);
            log.info("Passport Application & Renewal government service seeded successfully into PostgreSQL database.");
        }

        // Initialize Driving Licence & Vehicle Registration service
        java.util.Optional<com.onepath.entity.Service> existingDlService = serviceRepository.findBySlug("driving-licence-vehicle-registration");
        String dlFees = "Learner's Licence: ₹200 (Single) / ₹350 (Both) | Permanent DL: ₹700 (Single) / ₹1,000 (Both) | Renewal: ₹400 | RC: ₹300–₹600";
        if (existingDlService.isPresent()) {
            com.onepath.entity.Service dlService = existingDlService.get();
            dlService.setFees(dlFees);
            serviceRepository.save(dlService);
        } else {
            com.onepath.entity.Service dlService = new com.onepath.entity.Service();
            dlService.setName("Driving Licence & Vehicle Registration");
            dlService.setSlug("driving-licence-vehicle-registration");
            dlService.setCategory("Government & Civic Services");
            dlService.setDescription("Official Ministry of Road Transport & Highways (MoRTH) Parivahan service for Learner's Licences, Permanent DL tests, renewals, duplicate smart cards, and Vehicle Registration (RC).");
            dlService.setEligibility("Indian citizens and residents. Minimum age: 16 years for MCWOG (up to 50cc) with parental consent; 18 years for MCWG two-wheelers and LMV cars; 20 years for transport/commercial vehicles. Permanent DL requires holding an active Learner's Licence for at least 30 days.");
            dlService.setTimeline("Learner's Licence: Same Day (Contactless online test) | Permanent DL: 7–15 Working Days (post-driving test) | RC: 7–14 Working Days");
            dlService.setFees(dlFees);
            dlService.setImportantNotes("Mandatory 30-Day Practice: You must hold an active Learner's Licence for at least 30 days before taking the permanent driving test. Driving under LL requires displaying red 'L' boards and accompaniment by a licensed driver. Digital DL & RC on DigiLocker carry 100% legal validity.");
            dlService.setOfficialPortalUrl("https://sarathi.parivahan.gov.in");
            dlService.setHasQuestionnaire(true);

            // Default base steps
            dlService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Online Application on Sarathi / Vahan Portal",
                    "Access official MoRTH portal (https://sarathi.parivahan.gov.in for DL or https://vahan.parivahan.gov.in for RC), select your state, and complete online application form.",
                    "15–20 Minutes",
                    "Use Aadhaar authentication for faceless contactless processing without physical RTO queues."
            ));
            dlService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Document Upload & Digital Signatures",
                    "Upload scanned age proof, address proof, self-declaration Form 1 (or Form 1A medical certificate if 40+ years old), and clear signature.",
                    "10 Minutes",
                    "Ensure files are under 200 KB in JPEG or PDF format."
            ));
            dlService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Government Fee Payment & Appointment Booking",
                    "Pay prescribed government fees online via UPI or Net Banking. For Permanent DL, schedule a driving competence test appointment slot at your regional RTO automated track.",
                    "10 Minutes",
                    "Download and print the Payment Receipt and Appointment Confirmation Slip."
            ));
            dlService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Test & Physical Verification (Online / RTO Track)",
                    "Complete the online road safety LL test from home, or appear at the RTO Automated Driving Test Track with vehicle of applied class for practical driving evaluation.",
                    "30 Mins (LL) / 1–2 Hours (DL Test)",
                    "Practical test evaluates figure-8 track, parallel parking, and gradient hill stop without rollback."
            ));
            dlService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Approval, Smart Card Printing & Speed Post Delivery",
                    "Once approved by the Licensing / Registering Authority, your polycarbonate Smart Card DL/RC is printed and dispatched via Speed Post. Digital copy syncs immediately to DigiLocker and mParivahan.",
                    "7–15 Working Days",
                    "Track real-time Speed Post delivery on India Post portal using ARN tracking number."
            ));

            // Default base documents
            dlService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Date of Birth & Age",
                    "Aadhaar Card, 10th Standard School Marksheet, Municipal Birth Certificate, or Passport.",
                    true,
                    "Original + 1 self-attested copy",
                    "Download mask-free e-Aadhaar from uidai.gov.in or 10th marksheet via DigiLocker."
            ));
            dlService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Present Residential Address",
                    "Aadhaar Card, Voter ID, Electricity/Water Bill (last 3 months), or Student Bonafide with Hostel Certificate.",
                    true,
                    "Original + 1 self-attested copy",
                    "Students staying in hostels/PGs can obtain bonafide certificate from registrar."
            ));
            dlService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Form 1 (Self-Declaration as to Physical Fitness)",
                    "Mandatory statutory declaration regarding vision, color blindness, and physical fitness.",
                    true,
                    "Auto-generated on Sarathi portal",
                    "Answer online fitness questions during form filling; electronically signed."
            ));
            dlService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Form 1A (Medical Certificate for 40+ Years or Transport)",
                    "Statutory medical certificate signed by a registered medical practitioner (MBBS).",
                    false,
                    "Original signed & stamped by MBBS Doctor",
                    "Required for applicants aged 40 and above or those applying for transport vehicle licences."
            ));
            dlService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Valid Form 3 Learner's Licence (for Permanent DL)",
                    "Active Learner's Licence showing completion of mandatory 30-day practice period.",
                    false,
                    "Original / DigiLocker digital copy",
                    "Download from Sarathi Parivahan portal under 'Print Learner Licence'."
            ));
            dlService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Vehicle Registration Documents (Form 20, 21, 22 for RC)",
                    "Form 20 (Application), Form 21 (Sale Certificate), Form 22 (Roadworthiness), and Insurance Certificate.",
                    false,
                    "Originals from authorized vehicle dealership",
                    "Provided in vehicle documentation folder at showroom delivery."
            ));

            serviceRepository.save(dlService);
            log.info("Driving Licence & Vehicle Registration government service seeded successfully into PostgreSQL database.");
        }

        // Initialize EWS / Income & Caste Certificate Issuance service
        java.util.Optional<com.onepath.entity.Service> existingRevenueService = serviceRepository.findBySlug("ews-income-caste-certificate");
        if (existingRevenueService.isEmpty()) {
            com.onepath.entity.Service revService = new com.onepath.entity.Service();
            revService.setName("EWS / Income & Caste Certificate Issuance");
            revService.setSlug("ews-income-caste-certificate");
            revService.setCategory("Government & Civic Services");
            revService.setDescription("Official State Revenue Department & e-District service for Economically Weaker Section (EWS 10% Quota), Annual Family Income, and Caste / Community (SC, ST, OBC) Certificates.");
            revService.setEligibility("EWS: General category citizens with gross family income below ₹8,00,000 and within statutory asset limits. Income: All residents requiring certified financial validation for scholarships and fee concessions. Caste: SC, ST, and OBC communities with paternal lineage documentation.");
            revService.setTimeline("10 to 30 Working Days (Governed by State Right to Services / Sakala / Lok Seva Acts)");
            revService.setFees("₹0 to ₹60.00 (State portal specific; Delhi/Bihar Free, UP ₹15, MH ₹20, KA ₹25, TS/AP ₹45, TN ₹60)");
            revService.setImportantNotes("DoPT Central Format Requirement: For Central Government admissions (NEET, JEE, UPSC, Central Universities) and recruitment, EWS and OBC-NCL certificates must strictly follow the Central DoPT Memorandum format carrying digital QR verification. Paternal Lineage Rule: Caste certificates must be supported by paternal lineage documents (father's or grandfather's records). Land Clubbing: For EWS assessment, all family agricultural land and flats across multiple locations are clubbed together.");
            revService.setOfficialPortalUrl("https://serviceonline.gov.in");
            revService.setHasQuestionnaire(true);

            // Default base steps
            revService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Registration on State e-District / ServicePlus Portal",
                    "Visit designated State e-District portal (e.g. Aaple Sarkar, Nadakacheri, eDistrict UP, MeeSeva, or RTPS Bihar) and register using Aadhaar-linked mobile verification.",
                    "10–15 Minutes",
                    "Always select the citizen service corresponding to your ancestral or present revenue jurisdiction."
            ));
            revService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Application Form Filling & Property/Lineage Declaration",
                    "Complete online application form with applicant, parent, and family details. Declare gross annual family income, land holdings, or paternal caste particulars.",
                    "20 Minutes",
                    "Declare all family income sources and land holdings across districts to prevent rejection during scrutiny."
            ));
            revService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Document Upload & Statutory Fee Payment",
                    "Upload verified scanned copies of identity proof, address proof, income proof (ITR/Form 16/salary slips), land records, and notarized self-declaration affidavit. Pay statutory fee online.",
                    "10 Minutes",
                    "Download the Fee Receipt and preserve the 15-digit Application Reference Number (ARN)."
            ));
            revService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Desk Scrutiny & Field Survey by Talathi / Patwari",
                    "Revenue Inspector (RI) verifies uploaded documents against land registries and municipal databases. Village Revenue Officer (VRO / Talathi) conducts spot inquiry if triggered.",
                    "5–15 Working Days",
                    "Keep original physical records ready for inspection if summoned by revenue officials."
            ));
            revService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Digital Signature by Tehsildar / SDM & Certificate Download",
                    "Upon approval, the Tehsildar, Sub-Divisional Magistrate (SDM), or Revenue Officer signs the certificate using PKI Class-3 Digital Signature. Certificate with QR code is delivered via portal and SMS.",
                    "10–30 Working Days",
                    "Download and store the digital PDF on DigiLocker for instant authentic verification by universities."
            ));

            // Default base documents
            revService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Identity (Aadhaar Card / Voter ID / PAN)",
                    "Valid government photo identity proof of applicant and family head.",
                    true,
                    "Clear PDF / JPEG scan (under 200 KB)",
                    "Download mask-free e-Aadhaar from uidai.gov.in."
            ));
            revService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Present Residential Address",
                    "Ration Card, Electricity Bill, Water Bill, or Domicile Certificate proving continuous residence.",
                    true,
                    "Original scanned copy",
                    "Electricity bill from state DISCOM or Domicile Certificate from Tehsildar."
            ));
            revService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Family Income Evidence (ITR / Form 16 / Salary Slips / VRO Report)",
                    "ITR acknowledgement, employer salary certificate, Form 16, or Village Revenue Officer assessment.",
                    true,
                    "Income Tax Return (ITR-V) or Form 16",
                    "Download ITR acknowledgement from e-filing portal (incometax.gov.in)."
            ));
            revService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Immovable Property & Land Records (7/12, Khatauni, Patta)",
                    "Record of Rights (RoR) / digital 7/12 / Khatauni or Municipal property tax receipt.",
                    true,
                    "Digital Record of Rights (RoR)",
                    "Download certified digital 7/12 or Khatauni from your state's Bhulekh portal."
            ));
            revService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Paternal Caste Certificate / Historical Lineage Record",
                    "Father's, grandfather's, or paternal relative's caste certificate or school leaving certificate (for Caste applications).",
                    false,
                    "Certified copy from Tehsildar / School",
                    "Obtain copy from father's school records or previously certified paternal relative."
            ));
            revService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Self-Declaration Affidavit on Non-Judicial Stamp Paper",
                    "Statutory affidavit affirming community status, family income, and absence of excluded assets.",
                    true,
                    "Original notarized affidavit",
                    "Download official template from state e-District portal and notarize on stamp paper."
            ));

            serviceRepository.save(revService);
            log.info("EWS / Income & Caste Certificate Issuance government service seeded successfully into PostgreSQL database.");
        }

        // Initialize JoSAA & State College Admissions Counselling service
        java.util.Optional<com.onepath.entity.Service> existingJosaaService = serviceRepository.findBySlug("josaa-state-admissions-counselling");
        if (existingJosaaService.isEmpty()) {
            com.onepath.entity.Service josaaService = new com.onepath.entity.Service();
            josaaService.setName("JoSAA & State College Admissions Counselling");
            josaaService.setSlug("josaa-state-admissions-counselling");
            josaaService.setCategory("Education");
            josaaService.setDescription("Official centralized and state technical seat allocation framework covering 23 IITs, 31 NITs, 26 IIITs, IIEST Shibpur, 38 GFTIs, and Andhra Pradesh state engineering colleges (JNTUK, JNTUA, AU, SVU). Features guided choice filling, dual eligibility evaluation, Freeze/Float/Slide decision rules, and comprehensive document verification.");
            josaaService.setEligibility("Candidates must satisfy dual qualifications: (1) Competitive entrance exam qualification in JEE (Advanced) for IITs, JEE (Main) Paper 1 for NITs/IIITs/GFTIs, or AP EAPCET for state engineering; AND (2) Class 12 aggregate board score of >= 75% (or Top 20 percentile in board) for general central seats (65% for SC/ST/PwD) or >= 45% aggregate in PCM (40% for reserved) for AP state engineering.");
            josaaService.setTimeline("June - August (6 JoSAA Rounds + 2 CSAB Special Rounds / 3 AP State Rounds)");
            josaaService.setFees("SAF: Rs 35,000 (GEN/OBC/EWS) / Rs 15,000 (SC/ST/PwD) - Tuition adjusted | AP EAPCET: Rs 1,200 (OC/BC) / Rs 600 (SC/ST) | Registration: Free");
            josaaService.setImportantNotes("STRICT REPORTING MANDATE: If a seat is allotted in ANY JoSAA round, candidate must pay SAF and complete online reporting; failure to do so results in seat forfeiture and PERMANENT DEBARMENT from subsequent rounds. OBC-NCL and EWS certificates MUST be issued ON or AFTER April 1 of admission year in Central format. Medical certificate in mandatory Annexure-7 format required.");
            josaaService.setOfficialPortalUrl("https://josaa.nic.in");
            josaaService.setHasQuestionnaire(true);

            // Steps
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Portal Authentication & Profile Verification",
                    "Log into official JoSAA portal (https://josaa.nic.in) or AP EAPCET portal (https://cets.apsche.ap.gov.in) using JEE Application Number / EAPCET Hall Ticket and Password. Carefully verify state code of eligibility, gender, and category details.",
                    "15-20 Minutes",
                    "Ensure your state code of eligibility matches the state where you actually appeared for Class 12."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Strategic Choice Filling & Priority Arrangement",
                    "Fill college and branch preferences in STRICT order of your authentic personal preference. You have UNLIMITED choices. Put your dream programs at the top, realistic choices in the middle, and reliable backup options at the bottom.",
                    "2-3 Days",
                    "Golden Rule: Never place a lower-preferred option above a higher-preferred option simply because of previous year cutoffs."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Mock Allotment Review & Mandatory Choice Locking",
                    "Review the two official Mock Allotment results published to understand indicative seat trends. Make necessary adjustments. Ensure you LOCK your choices before the announced deadline timestamp.",
                    "1 Hour",
                    "If not manually locked, system auto-locks your last saved preferences at the deadline."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Seat Allotment Result & Mandatory Willingness Selection",
                    "Upon announcement of allotment results, log in immediately. If a seat is allotted, you MUST select one of three mandatory willingness options: Freeze (accept & exit), Float (accept & upgrade across any institute), or Slide (accept & upgrade within same institute).",
                    "Same Day",
                    "Selecting Float or Slide guarantees your current seat while keeping you in contention for higher choices."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Seat Acceptance Fee (SAF) Payment & Document Upload",
                    "Pay the mandatory Seat Acceptance Fee online (Rs 35,000 for Gen/OBC/EWS, Rs 15,000 for SC/ST/PwD) via Net Banking, UPI, or Card. Upload clear scans of required certificates.",
                    "1-2 Hours",
                    "CRITICAL: Failure to pay SAF or upload documents leads to permanent debarment from subsequent rounds."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "Online Document Verification & Query Resolution (24-Hour SLA)",
                    "Virtual Reporting Centre (VRC) officers verify uploaded certificates. If an officer finds a discrepancy, an online query is raised. You MUST respond within 24 hours.",
                    "24 Hours Max",
                    "Check your candidate portal twice daily during reporting windows."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    7,
                    "Subsequent Rounds & CSAB Special Round Participation",
                    "Monitor rounds 2 through 6. Your seat may upgrade automatically based on your Float/Slide selection. After Round 6, participate in CSAB Special Rounds (csab.nic.in) for vacant NIT+ seats.",
                    "1-2 Weeks",
                    "Seats secured in JoSAA can be retained while exploring upgrades in CSAB Special Rounds."
            ));
            josaaService.addStep(new com.onepath.entity.ServiceStep(
                    8,
                    "Physical Reporting & Institutional Admission",
                    "Download Final Provisional Seat Allotment Letter. Pay remaining institutional admission tuition fees online, and report physically to the allotted campus with all original documents.",
                    "Designated Dates",
                    "Carry at least 3 sets of self-attested photocopies, 6 photos, and medical certificate."
            ));

            // Documents
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Class 10 Certificate / Marks Sheet",
                    "Statutory proof of candidate date of birth (DOB) and candidate name matching entrance application exactly.",
                    true,
                    "Original PDF scan (under 500 KB)",
                    "Issued by CBSE / ICSE / State Secondary Board. DigiLocker verified copy is acceptable."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Class 12 Marksheet & Passing Certificate",
                    "Proof of qualifying examination and aggregate percentage across 5 compulsory subjects (Physics, Math, Chem/Vocational, Language, 5th subject).",
                    true,
                    "Original PDF scan (under 500 KB)",
                    "Must clearly show individual subject marks and aggregate percentage."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Entrance Examination Admit Card & Rank Card",
                    "JEE Main, JEE Advanced, or AP EAPCET Admit Card and official Rank Score Card showing CRL and Category Rank.",
                    true,
                    "Original PDF download",
                    "Download directly from NTA (jeemain.nta.ac.in), JEE Adv (jeeadv.ac.in), or APSCHE (cets.apsche.ap.gov.in)."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Medical Fitness Certificate (Mandatory Annexure-7 Format)",
                    "Official JoSAA Annexure-7 medical certificate verifying physical fitness, visual acuity, color perception, and general health.",
                    true,
                    "Original signed & stamped by MBBS Doctor",
                    "Download official Annexure-7 template from josaa.nic.in. Must be certified by a Registered Medical Practitioner (MBBS)."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Valid Government Photo Identity Card",
                    "Aadhaar Card, Passport, Voter ID, or School/College Photo ID proving candidate identity.",
                    true,
                    "Original color PDF scan",
                    "Download mask-free e-Aadhaar from uidai.gov.in."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Provisional Seat Allotment Letter & SAF Fee Payment Receipt",
                    "Official allotment letter generated by JoSAA / CSAB / AP EAPCET portal after seat allocation, together with payment acknowledgement receipt.",
                    true,
                    "Portal generated PDF",
                    "Download from candidate dashboard immediately after paying the Seat Acceptance Fee."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "OBC-NCL / EWS Certificate (STRICT: Issued ON or AFTER April 1)",
                    "Mandatory Central Government format certificate. Strict Rule: Must be issued ON or AFTER April 1 of the admission year. If issued prior, candidate is converted to General category.",
                    false,
                    "Central DoPT Format PDF Scan",
                    "Obtain from Tahsildar / Revenue Divisional Officer (RDO) / Sub-Divisional Magistrate (SDM)."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "SC / ST Community & Tribe Certificate",
                    "Permanent caste/community certificate issued in the prescribed Central format by authorized revenue officer (Tahsildar / MRO).",
                    false,
                    "Original PDF scan",
                    "Issued by revenue department / MeeSeva with verifiable digital certificate number and government seal."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "PwD Benchmark Disability Certificate / UDID Card",
                    "Disability certificate issued by authorized government Medical Board confirming minimum 40% benchmark disability.",
                    false,
                    "Unique Disability ID (UDID) Card",
                    "Swavlamban UDID card or certificate from District Chief Medical Officer (CMO)."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "State Code of Eligibility Proof / Domicile Certificate",
                    "Class 12 examination passing certificate proving the state where Class 12 was completed (for 50% Home State NIT quota and 85% AP local quota).",
                    true,
                    "College Study / Bonafide / Passing Certificate",
                    "Class 12 school leaving / study certificate from intermediate college or residence certificate."
            ));
            josaaService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Crossed Bank Cancelled Cheque / Passbook Photocopy",
                    "Cancelled bank cheque or front page of passbook showing Account Holder Name, Account Number, and IFSC code (student or parent).",
                    true,
                    "Original scan of cancelled cheque",
                    "Mandatory for verification in case of seat cancellation, round withdrawal, or refund of excess fee."
            ));

            serviceRepository.save(josaaService);
            log.info("JoSAA & State College Admissions Counselling service seeded successfully into PostgreSQL database.");
        }

        // Initialize Marriage Registration service
        java.util.Optional<com.onepath.entity.Service> existingMarriageService = serviceRepository.findBySlug("marriage-registration");
        if (existingMarriageService.isEmpty()) {
            com.onepath.entity.Service marriageService = new com.onepath.entity.Service();
            marriageService.setName("Marriage Registration");
            marriageService.setSlug("marriage-registration");
            marriageService.setCategory("Government & Civic Services");
            marriageService.setDescription("Official statutory registration service for marriages under the Hindu Marriage Act, 1955, Special Marriage Act, 1954, and Anand Marriage Act through the Andhra Pradesh Registration and Stamps Department (IGRS AP) and Sub-Registrar Offices (SRO). Offers guided pathway selection, eligibility validation, document verification, witness requirements, and SRO appointment booking.");
            marriageService.setEligibility("Groom must be at least 21 years old and Bride at least 18 years old. At least one party must reside in the Sub-Registrar jurisdiction for over 30 days, or the marriage must have been solemnized in the jurisdiction. Neither party should have a living spouse unless legally dissolved by a Family Court divorce decree. Both parties must give free, voluntary consent.");
            marriageService.setTimeline("HMA / Religious: 7 to 15 Working Days (Same-day post SRO visit) | SMA (Civil): 30 to 45 Days (Mandatory 30-day notice)");
            marriageService.setFees("AP SRO Standard Registration: ₹100 – ₹200 | Special Marriage Act (SMA): ₹150 – ₹500 | Certified Copy: ₹50 – ₹100");
            marriageService.setImportantNotes("COMPULSORY REGISTRATION MANDATE: Pursuant to Supreme Court ruling in Seema v. Ashwani Kumar (2006), marriage registration is legally compulsory in India. SMA 30-Day Notice: Under Special Marriage Act, a mandatory 30-day public notice is displayed on the SRO notice board. Witnesses: 2 adult witnesses required for HMA; 3 adult witnesses required for SMA with original Aadhaar/PAN. Physical appearance of couple and witnesses at SRO is mandatory.");
            marriageService.setOfficialPortalUrl("https://registration.ap.gov.in");
            marriageService.setHasQuestionnaire(true);

            // Steps
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Determine Legal Pathway & SRO Jurisdiction",
                    "Identify whether registration falls under Hindu Marriage Act, 1955 (already solemnized) or Special Marriage Act, 1954 (civil/court marriage). Locate jurisdictional Sub-Registrar Office (SRO) based on residence (>30 days) or wedding venue.",
                    "Step 1",
                    "Jurisdictional mismatch is the primary reason for application rejection."
            ));
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Online Application Form Entry & Slot Booking",
                    "Register on AP Registration & Stamps Department portal (IGRS AP - registration.ap.gov.in) or visit local MeeSeva centre. Enter details of husband, wife, solemnization date/venue, and witnesses. Book appointment slot.",
                    "30 Minutes",
                    "Ensure names match your Aadhaar, 10th marksheet, and wedding card."
            ));
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Document Upload & Statutory Fee Payment",
                    "Upload scanned age proofs (10th/birth certificate), address proofs (Aadhaar/utility bill), joint photos, ceremony ritual photos, wedding card, and affidavits. Pay statutory registration fee online.",
                    "15 Minutes",
                    "Preserve the fee payment receipt and appointment acknowledgement slip."
            ));
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Witness Organization & Physical Appearance Preparation",
                    "Arrange mandatory adult witnesses (2 for HMA, 3 for SMA). Witnesses must bring original Aadhaar Card / PAN Card and passport photographs to the SRO.",
                    "Before Visit",
                    "Parents, relatives, or adult friends who witnessed the rites can serve as witnesses."
            ));
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Mandatory 30-Day Public Notice Display (SMA Only)",
                    "For Special Marriage Act registrations, the Marriage Officer publishes a 30-day public notice on the SRO notice board. If no valid legal objection is received within 30 days, the marriage is solemnized.",
                    "30 Days Window",
                    "Skip this step if registering an already solemnized marriage under Hindu Marriage Act."
            ));
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "SRO Physical Verification & Biometric Capture",
                    "Husband, Wife, and all mandatory witnesses appear in person before the Sub-Registrar. Original documents are verified, and biometric fingerprints and webcam photographs are captured.",
                    "1-2 Hours",
                    "Physical appearance of both parties and witnesses is legally mandatory."
            ));
            marriageService.addStep(new com.onepath.entity.ServiceStep(
                    7,
                    "Marriage Register Entry & Certificate Download",
                    "Sub-Registrar registers the marriage into the official state register and issues the digitally signed Marriage Certificate with QR verification. Download from IGRS AP or DigiLocker.",
                    "Same Day / 7 Days",
                    "Certificate is automatically recognized for passport spouse endorsement, visas, and banking."
            ));

            // Documents
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Application Form / Memorandum of Marriage",
                    "Statutory application duly signed by both husband and wife with passport photos affixed.",
                    true,
                    "Duly signed printed form",
                    "Generated from AP IGRS / MeeSeva portal after online entry."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Age Proof of Groom & Bride",
                    "Statutory proof confirming Groom >= 21 years and Bride >= 18 years on the date of marriage.",
                    true,
                    "Birth Certificate / SSC Marks Sheet / Passport",
                    "Issued by Municipal Corporation, Secondary Education Board, or MEA."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Residence Proof of Husband & Wife",
                    "Proof establishing residence within the Sub-Registrar's jurisdiction for more than 30 days.",
                    true,
                    "Aadhaar Card / Voter ID / Gas or Electricity Bill / Registered Lease",
                    "UIDAI or state government utility bill."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Wedding Card / Religious Solemnization Certificate",
                    "Evidence of solemnized religious ceremony (Original wedding invitation card, temple receipt, or priest certificate).",
                    true,
                    "Original Wedding Card or Temple/Gurudwara Certificate",
                    "Preserve original physical wedding invitation."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Passport-Size Joint Photos & Wedding Ritual Photos",
                    "4 to 6 joint passport-size photos + 2 clear ceremony photos showing wedding rituals (garland exchange, mangalsutra / saptapadi).",
                    true,
                    "Original color photographic prints",
                    "Studio prints clearly showing faces of both bride and groom during rites."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Sworn Notarized Affidavits of Marital Status",
                    "Separate affidavits affirmed before a Notary Public declaring date & place of marriage, DOB, marital status at wedding, and non-prohibited relationship.",
                    true,
                    "Notarized Affidavit on Non-Judicial Stamp Paper",
                    "Prepared on stamp paper and notarized."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Witness Identity & Address Proofs",
                    "Valid Aadhaar Card and PAN Card for mandatory witnesses (2 for HMA, 3 for SMA). Witnesses must appear in person.",
                    true,
                    "Original ID + Self-attested copies",
                    "Aadhaar Card / Passport / Voter ID."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Certified Copy of Divorce Decree Absolute",
                    "Certified final judgment and decree from Family Court proving legal dissolution of prior marriage.",
                    false,
                    "Certified Court Copy with seal",
                    "Obtained from the registry of the Family Court that granted divorce."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Certified Death Certificate of Deceased Spouse",
                    "Official death certificate issued by Municipal Corporation or Registrar of Births & Deaths (for widowed applicants).",
                    false,
                    "Original / Certified Municipal Copy",
                    "Issued by local municipal corporation or panchayat."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Embassy NOC / Single Status Certificate & Passport",
                    "Certificate of No Impediment / Single Status Certificate issued by Embassy of foreign country + valid Passport & Indian Visa (for foreign national/NRI spouse).",
                    false,
                    "Official Embassy Document & Passport Copy",
                    "Obtain from foreign embassy / high commission in India."
            ));
            marriageService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Statutory Notice of Intended Marriage (Form-II)",
                    "Notice submitted for 30-day public display under Special Marriage Act, 1954.",
                    false,
                    "Form-II signed by both parties",
                    "Submitted online via AP IGRS or at SRO notice counter."
            ));

            serviceRepository.save(marriageService);
            log.info("Marriage Registration government service seeded successfully into PostgreSQL database.");
        }

        // Initialize Aadhaar Card Update & Correction service
        java.util.Optional<com.onepath.entity.Service> existingAadhaarService = serviceRepository.findBySlug("aadhaar-update-correction");
        if (existingAadhaarService.isEmpty()) {
            com.onepath.entity.Service aadhaarService = new com.onepath.entity.Service();
            aadhaarService.setName("Aadhaar Card Update & Correction");
            aadhaarService.setSlug("aadhaar-update-correction");
            aadhaarService.setCategory("Government Documents");
            aadhaarService.setDescription("Official UIDAI service for updating and correcting demographic details (name, residential address, date of birth, gender, mobile number, email ID) and biometrics (photograph, 10 fingerprints, dual iris) under the Aadhaar Act, 2016. Identifies whether requests can be completed online via the myAadhaar portal or mandate visiting an authorized Aadhaar Seva Kendra.");
            aadhaarService.setEligibility("All Indian residents holding a 12-digit Aadhaar number. Lifetime limits enforced by UIDAI: Name change maximum 2 times; Date of Birth change maximum 1 time; Gender change maximum 1 time; Address/Mobile/Email have no count limits subject to valid documentation. Children must undergo Mandatory Biometric Update (MBU) at age 5 and age 15 (statutory free).");
            aadhaarService.setTimeline("Online Address Update: 3 to 10 Working Days | In-Person Demographic & Mobile Update: 5 to 15 Working Days | Biometric Update: 7 to 21 Working Days");
            aadhaarService.setFees("Mandatory Biometric Update (Age 5 & 15): FREE (₹0) | Demographic & Mobile Update: ₹50.00 | Biometric Update (Photo/Iris/Fingerprints): ₹100.00 | Online Address Update: ₹50.00 | PVC Card Order: ₹50.00");
            aadhaarService.setImportantNotes("ONLINE UPDATE RESTRICTIONS: Updates to Mobile Number or Email ID CANNOT be done online; they legally require physical biometric authentication at an authorized Aadhaar Seva Kendra. Any website claiming online mobile linking is fraudulent. Strictly adhere to lifetime limits: Name (2x), DOB (1x). Utility bills and bank statements must be less than 3 months old. Never pay fees exceeding UIDAI prescribed limits printed on your acknowledgement receipt.");
            aadhaarService.setOfficialPortalUrl("https://myaadhaar.uidai.gov.in");
            aadhaarService.setHasQuestionnaire(true);

            // Steps
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Identify Update Channel & Check Applicable Procedure",
                    "Determine whether your update can be completed online (Address / Document Re-validation via myAadhaar) or mandates visiting an authorized Aadhaar Seva Kendra (Mobile number, Email ID, Name, DOB, Gender, or Biometrics/Photo).",
                    "5–10 Minutes",
                    "Mobile number or Email update strictly requires physical biometric verification."
            ));
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Verify Statutory Limits & Eligibility Rules",
                    "Review your lifetime modification limits before applying: Name (max 2 times), Date of Birth (max 1 time), Gender (max 1 time). Check that utility bills or bank statements are under 3 months old.",
                    "10 Minutes",
                    "Exceeding lifetime limits requires formal exception approval from the UIDAI Regional Office."
            ));
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Prepare Original Supporting Documents (PoI / PoA / DoB)",
                    "Gather original documentary proofs from the UIDAI Master List (Passport, PAN Card, Voter ID, Electricity bill, Bank passbook, or Head of Family consent). For Mobile or Biometric updates, no documents other than your 12-digit Aadhaar number are needed.",
                    "Before Visit",
                    "Operators and online portal scanners only accept original documents; photocopies will be rejected."
            ));
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Submit Request Online or Visit Aadhaar Seva Kendra",
                    "If updating address online, log in to myaadhaar.uidai.gov.in with mobile OTP and upload document. If visiting a centre, book appointment slot on appointments.uidai.gov.in or obtain a walk-in token at your local ASK.",
                    "15–30 Minutes",
                    "Online slot booking significantly reduces wait times at busy Aadhaar centres."
            ));
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Pay Prescribed Statutory Fee & Biometric Authentication",
                    "Pay official fee: FREE for Child MBU (Age 5/15), ₹50 for Demographic/Mobile/Online update, or ₹100 for Biometric/Photo update. For centre visits, complete fingerprint and iris scan on the ECMP client.",
                    "5 Minutes",
                    "Always collect the printed receipt and ensure the fee charged matches the official UIDAI rate."
            ));
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "Collect & Save Acknowledgement Slip (URN / EID)",
                    "Save your 14-digit Service Request Number (SRN/URN) for online requests, or collect the printed Enrolment ID (EID) slip containing your 28-digit tracking number from the centre operator.",
                    "2 Minutes",
                    "Preserve the EID/URN carefully to track request progress."
            ));
            aadhaarService.addStep(new com.onepath.entity.ServiceStep(
                    7,
                    "Track Status Online & Download Updated e-Aadhaar",
                    "Check update status on myaadhaar.uidai.gov.in under 'Check Enrolment & Update Status'. Once approved by UIDAI verification engines, download your password-protected digital e-Aadhaar PDF.",
                    "3–15 Days",
                    "Password to open e-Aadhaar PDF is first 4 letters of your name in CAPITAL + 4-digit birth year (e.g., ANIT1995)."
            ));

            // Documents
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Existing Aadhaar Card / 12-Digit Aadhaar Number",
                    "Current physical Aadhaar card, e-Aadhaar, or 12-digit number for retrieving your profile.",
                    true,
                    "Physical Card or 12-Digit Number",
                    "Any existing copy of your Aadhaar."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Identity (PoI) Document",
                    "Valid document confirming legal name and photo: Passport, PAN Card / e-PAN, Voter ID, Driving Licence, or Government Photo ID Card.",
                    true,
                    "Original Government Issued Photo ID",
                    "Issued by competent government authority."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Address (PoA) Document",
                    "Valid document confirming residential address: Electricity Bill (<3 months), Water Bill (<3 months), Gas Bill, Bank Passbook with photo & stamp (<3 months), Registered Rent Agreement, or Passport.",
                    true,
                    "Original document or color PDF scan under 2 MB",
                    "Utility bill or bank passbook with branch manager stamp."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Date of Birth (DoB) Document",
                    "Official document showing exact date of birth: Birth Certificate from Municipal Corporation/Panchayat, SSC/10th Board Certificate, or Passport.",
                    false,
                    "Original Municipal or Board Certificate",
                    "Issued by Registrar of Births & Deaths or Education Board."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Head of Family (HoF) Consent Form & Relationship Proof",
                    "For address update without own proof: Ration Card, Marriage Certificate, or Birth Certificate linking to HoF + HoF Aadhaar consent.",
                    false,
                    "Original Relationship Document + HoF OTP",
                    "Issued by Food & Civil Supplies or Revenue Department."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Standard Certificate for Aadhaar Enrolment/Update (Annexure-I)",
                    "Official UIDAI prescribed certificate format with resident photo cross-signed and stamped by Gazetted Officer, Tehsildar, or MP/MLA/Ward Councillor (if no other proof available).",
                    false,
                    "Original Certificate on Official Letterhead",
                    "Download blank template from uidai.gov.in and get certified."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Gazette Notification of Name Change (Exception Cases)",
                    "Published Central or State Gazette notification for 3rd name change exceeding standard UIDAI lifetime limits.",
                    false,
                    "Original Gazette Print or Certified Copy",
                    "Published via Department of Publication (egazette.gov.in)."
            ));
            aadhaarService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Parent / Guardian Aadhaar Card (For Child MBU)",
                    "Aadhaar card of parent or legal guardian for authenticating child's Mandatory Biometric Update at age 5 or 15.",
                    false,
                    "Original Aadhaar Card",
                    "Parent must accompany child at the Aadhaar centre."
            ));

            serviceRepository.save(aadhaarService);
            log.info("Aadhaar Card Update & Correction government service seeded successfully into PostgreSQL database.");
        }

        // Initialize PAN Card Application & Correction service
        java.util.Optional<com.onepath.entity.Service> existingPanService = serviceRepository.findBySlug("pan-card-application-correction");
        if (existingPanService.isEmpty()) {
            com.onepath.entity.Service panService = new com.onepath.entity.Service();
            panService.setName("PAN Card Application & Correction");
            panService.setSlug("pan-card-application-correction");
            panService.setCategory("Government Documents");
            panService.setDescription("Official statutory service for new PAN allotment (Form 49A/49AA), Instant e-PAN generation, and changes or correction in PAN data (CSF form) under Section 139A of the Income Tax Act, 1961 through Protean eGov Technologies, UTITSL, and the Income Tax Department.");
            panService.setEligibility("All Indian citizens, non-residents, and legal entities (Companies, LLPs, Firms, Trusts, HUFs). Mandatory Section 139A rule: It is illegal to possess more than one PAN card (Section 272B penalty of ₹10,000 for holding duplicate PANs). PAN must be linked with Aadhaar under Section 139AA to avoid becoming inoperative.");
            panService.setTimeline("Instant e-PAN: 10 to 30 Minutes | Paperless e-KYC (Form 49A): 2 to 4 Working Days | Physical / Correction (CSF): 7 to 15 Working Days | Foreign Dispatch: 15 to 25 Working Days");
            panService.setFees("Instant e-PAN: FREE (₹0.00) | Physical Card Dispatch (Domestic): ₹107.00 | e-PAN Only (Digital): ₹66.00 | Card Reprint (Lost/Damaged): ₹50.00 | Foreign Dispatch (Form 49AA): ₹1,017.00");
            panService.setImportantNotes("DUPLICATE PAN PROHIBITION: Possessing more than one PAN card is a punishable statutory violation under Section 272B with a mandatory ₹10,000 fine. Surrender duplicate PANs immediately via CSF Form. PAN-Aadhaar Linking: Unlinked PANs become inoperative under Section 234H, blocking ITR refunds and doubling TDS rates to 20%. Single names without surname must be entered entirely in the Last Name / Surname field.");
            panService.setOfficialPortalUrl("https://www.incometax.gov.in");
            panService.setHasQuestionnaire(true);

            // Steps
            panService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Identify Application Type & Appropriate Statutory Form",
                    "Determine whether you require a New PAN (Form 49A for Indian citizens / Form 49AA for foreign applicants), Instant e-PAN via Aadhaar, PAN Data Correction (CSF Form), or simple Card Reprint.",
                    "5–10 Minutes",
                    "Applying for a second PAN when you already possess one is illegal under Section 272B."
            ));
            panService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Verify Aadhaar Linking & Duplicate PAN Status",
                    "Confirm that you do not already possess a PAN. If correcting an existing PAN, ensure your name and date of birth match your Aadhaar card to avoid e-KYC mismatches.",
                    "5 Minutes",
                    "Check PAN-Aadhaar link status on incometax.gov.in."
            ));
            panService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Prepare Supporting Documents (PoI, PoA, PoB)",
                    "Gather original proofs from the Form 49A master list: Proof of Identity (Aadhaar/Passport/Voter ID), Proof of Address (Aadhaar/Utility bill/Bank passbook), and Proof of Date of Birth. For paperless e-KYC, Aadhaar serves as the sole document.",
                    "Before Visit",
                    "Utility bills and bank statements must be less than 3 months old."
            ));
            panService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Select Official Application Channel & Fill Form",
                    "Submit online via Protean (protean-tinpan.com), UTITSL (utiitsl.com), or the Income Tax e-Filing portal (incometax.gov.in for Instant e-PAN). Choose paperless Aadhaar e-KYC mode for fastest processing.",
                    "15–20 Minutes",
                    "In paperless mode, your photograph is pulled directly from UIDAI records."
            ));
            panService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Pay Statutory Processing Fee Online",
                    "Pay official fee: FREE for Instant e-PAN, ₹50 for Reprint, ₹107 for physical card dispatch in India, or ₹1,017 for overseas delivery via UPI, Net Banking, or Card.",
                    "3 Minutes",
                    "Save the payment receipt and note the 15-digit Acknowledgement Number."
            ));
            panService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "Save 15-Digit Acknowledgement Number & Complete e-Sign",
                    "Authenticate using the final Aadhaar OTP e-Sign. If using physical submission mode, print the acknowledgement form, affix 2 passport photos, sign across photo and signature box, and mail to Pune/Mumbai.",
                    "2–5 Minutes",
                    "Physical documents must reach the processing centre within 15 days of online submission."
            ));
            panService.addStep(new com.onepath.entity.ServiceStep(
                    7,
                    "Track Application Status & Receive Laminated PAN Card",
                    "Track status online using your 15-digit acknowledgement number. e-PAN PDF is emailed within 24–48 hours, and the physical tamper-proof card is delivered to your address via India Post Speed Post.",
                    "2–15 Days",
                    "Password to open e-PAN PDF is your Date of Birth in DDMMYYYY format."
            ));

            // Documents
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Aadhaar Card (Primary Identity, Address & DoB Proof)",
                    "Serves as sole comprehensive proof of identity, address, and date of birth for paperless e-KYC applications.",
                    true,
                    "Aadhaar Number + Mobile OTP Authentication",
                    "UIDAI issued e-Aadhaar."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Identity (PoI) Document",
                    "Valid document confirming legal name and photo: Passport, Voter ID Card, Driving Licence, Arm's Licence, or Pensioner Photo Card.",
                    true,
                    "Original color scan or photocopy (under 300 KB)",
                    "Issued by competent government authority."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Address (PoA) Document",
                    "Valid document confirming residential address: Electricity Bill (<3 months), Water/Gas Bill (<3 months), Bank Account Statement (<3 months), Domicile Certificate, or Passport.",
                    true,
                    "Original color scan or photocopy (under 300 KB)",
                    "Utility company or bank branch with official stamp."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Date of Birth (PoB) Document",
                    "Statutory birth proof: Municipal Birth Certificate, Class 10th Matriculation Marksheet/Certificate, Passport, or Pension Payment Order.",
                    true,
                    "Original Municipal or Board Certificate",
                    "Issued by Registrar of Births & Deaths or Education Board."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Copy of Existing PAN Card / Allotment Letter (For Correction/Reprint)",
                    "Copy of existing physical PAN card or official allotment letter proving the 10-character PAN number to be updated or reprinted.",
                    false,
                    "Photocopy or Clear PDF Scan",
                    "Existing PAN card or previous tax assessment order."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Recent Color Passport-Size Photographs (2 Photos)",
                    "Recent 3.5 x 2.5 cm color photographs with white background and clear frontal facial view (required for e-Sign and physical forwarding modes).",
                    false,
                    "Physical prints or digital JPEG (under 50 KB)",
                    "Photo studio print."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Change of Name (Marriage Certificate / Gazette)",
                    "For surname change post-marriage: Marriage Certificate or wedding card; for other legal name changes: Published Gazette notification.",
                    false,
                    "Original Gazette Notification or Marriage Certificate",
                    "Department of Publication or Registrar of Marriages."
            ));
            panService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Entity Incorporation Deed (For Companies, Firms, LLPs, Trusts)",
                    "Certificate of Incorporation issued by RoC for Companies/LLPs; Partnership Deed for Firms; Trust Deed for Trusts.",
                    false,
                    "Official Certified Document Copy",
                    "Issued by Ministry of Corporate Affairs or Charity Commissioner."
            ));

            serviceRepository.save(panService);
            log.info("PAN Card Application & Correction government service seeded successfully into PostgreSQL database.");
        }

        // Initialize Property / Land Revenue Registration service
        java.util.Optional<com.onepath.entity.Service> existingPropertyService = serviceRepository.findBySlug("property-land-revenue-registration");
        if (existingPropertyService.isEmpty()) {
            com.onepath.entity.Service propService = new com.onepath.entity.Service();
            propService.setName("Property / Land Revenue Registration");
            propService.setSlug("property-land-revenue-registration");
            propService.setCategory("Government & Civic Services");
            propService.setDescription("Official statutory guidance framework for property conveyancing, sale deed registration, Encumbrance Certificate (EC Form 15/16) verification, market guidance values (circle rates), and post-registration revenue land mutation (RoR-1B/Patta) through the Andhra Pradesh Registration and Stamps Department (AP IGRS) and AP Revenue Department.");
            propService.setEligibility("All Indian citizens, NRIs, and legal entities executing property transactions within Andhra Pradesh. Parties must be competent to contract under the Indian Contract Act (18+ years, sound mind). Crucial requirement: Property must NOT be listed under the Section 22A Prohibited Properties Register (assigned lands, ceiling surplus, or wakf/endowments).");
            propService.setTimeline("SRO Registration: Same-Day (1–3 Hours) | Auto-Mutation: 24 Hours | Final Revenue Mutation (Patta): 15–30 Days | Online EC: Instant to 24 Hours");
            propService.setFees("Typical AP: ~5% Stamp Duty + 1% Reg Fee + local cess (based on higher of consideration or circle rate). Online EC: ₹25–₹50. Verify: https://registration.ap.gov.in/");
            propService.setImportantNotes("Statutory Guidance Notice: This service provides general workflow guidance based on AP Registration & Stamps Department and Revenue Department procedures. It is not formal legal advice. Mandatory compliance: (1) Section 22A check is critical before entering sale agreements; (2) For transactions >= ₹50 Lakhs, buyer must deduct 1% TDS under Section 194-IA via Form 26QB; (3) Registration at SRO alone does not complete revenue ownership—revenue mutation in Webland/RoR-1B is essential.");
            propService.setOfficialPortalUrl("https://registration.ap.gov.in/");
            propService.setHasQuestionnaire(true);

            // Steps
            propService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Identify Required Service & Preliminary Due Diligence",
                    "Determine your exact transaction type (Sale deed, Gift deed, Lease, Encumbrance Certificate, or Mutation). Conduct preliminary title inspection and physical site inspection to verify possession.",
                    "1–3 Days",
                    "Ensure seller has registered legal title and clear possession without boundary disputes."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Section 22A Prohibited Properties Register Verification",
                    "Check the official AP IGRS portal (registration.ap.gov.in) to verify that the survey number / parcel is NOT listed under Section 22A prohibited categories (government-assigned, ceiling surplus, or wakf/endowments).",
                    "15 Minutes",
                    "Section 22A barred lands cannot be registered under AP law; any attempted registration will be summarily refused."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "30-Year Encumbrance Certificate (EC Form 15) Inspection",
                    "Search and download an unbroken 30-year Encumbrance Certificate via AP IGRS or MeeSeva to confirm title is free from mortgages, court decrees, or attachment orders.",
                    "1 Day",
                    "Check that the seller is the recorded owner and no unresolved bank loans exist on the property."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Sale Deed Drafting with Boundary Schedule & Survey Details",
                    "Engage a licensed deed writer or legal counsel to draft the conveyance deed incorporating complete survey numbers, sub-divisions, exact extent, and four-side boundary descriptions (North/South/East/West).",
                    "1–2 Days",
                    "Seller and buyer names must match Aadhaar cards and prior title deeds 100%."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Market Value (Circle Rate) Calculation & Stamp Duty Assessment",
                    "Use the AP IGRS Market Value Calculator to determine the statutory circle rate. Compute applicable stamp duty (~5%) and registration fee (~1%). Duty is payable on the higher of circle rate or sale price.",
                    "30 Minutes",
                    "Ensure consideration is not under-valued below official market guidance value."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "Section 194-IA 1% TDS Remittance via Form 26QB (if >= ₹50L)",
                    "For all property transactions valued at ₹50 Lakhs or above, the buyer must deduct 1% TDS from the total consideration and deposit it via Form 26QB on the Income Tax e-filing portal.",
                    "1 Day",
                    "The Sub-Registrar requires the Form 26QB payment challan before registering high-value deeds."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    7,
                    "E-Stamp / e-Challan Payment & SRO Appointment Slot Booking",
                    "Generate e-Stamp paper via Stock Holding Corporation (SHCIL) or state CFMS e-Challan payment portal. Book a physical appointment slot at the jurisdictional Sub-Registrar Office.",
                    "1 Day",
                    "Select an appointment date and slot convenient for all parties and witnesses."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    8,
                    "Physical Appearance, Biometric Capture & Execution at SRO",
                    "Buyer, seller, and two adult witnesses attend the SRO counter with originals. Complete live thumbprint scanning and webcam photo capture before the Sub-Registrar.",
                    "1–3 Hours",
                    "Sub-Registrar formally endorses deed numbers and records the transaction in Book 1 / Index II."
            ));
            propService.addStep(new com.onepath.entity.ServiceStep(
                    9,
                    "Collect Registered Deed (Index II) & Verify Auto-Mutation in Webland",
                    "Collect original registered sale deed with official seals; confirm electronic auto-mutation trigger to the Tahsildar for updating revenue land records (RoR-1B/Patta).",
                    "Same Day",
                    "Track Tahsildar mutation status on MeeSeva/Webland; updated passbook is issued within 15–30 days."
            ));

            // Documents
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Identity & Address Proofs (Aadhaar & PAN) of Buyer, Seller & 2 Witnesses",
                    "Mandatory biometric and identity verification documents for all executing parties appearing before the Sub-Registrar.",
                    true,
                    "Original Aadhaar & PAN Card + 1 self-attested photocopy each",
                    "Carry original cards physically to SRO counter."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Chain of Parent / Link Deeds (Minimum 30 Years)",
                    "Original title deeds establishing unbroken chronological sequence of ownership leading to the seller.",
                    true,
                    "Original deeds + complete photocopy sets",
                    "Procure from seller or financial institution release."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Drafted Sale Deed on Legal Stamp Paper / E-Stamp Certificate",
                    "Conveyance deed incorporating property schedule, boundaries, consideration, and witness attestations.",
                    true,
                    "Physical deed with e-Stamp certificate / CFMS challan",
                    "Drafted through licensed deed writer or legal counsel."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Encumbrance Certificate (EC Form 15 covering 30 Years)",
                    "Official search certificate proving title is unencumbered by mortgages, liens, or court attachments.",
                    true,
                    "Digitally signed Form 15 extract",
                    "Download from registration.ap.gov.in or MeeSeva."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Property Schedule with Survey Number, Sub-division & Boundaries",
                    "Detailed technical schedule identifying survey number, plot/flat number, extent, and exact 4-side boundaries.",
                    true,
                    "Schedule annexure in deed with layout sketch",
                    "Extracted from prior deeds, FMB sketch, or approved layout."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Latest Revenue Records (Pattadar Passbook / RoR-1B Extract & Adangal)",
                    "Official revenue records proving seller's registered ownership and physical possession of agricultural parcel.",
                    false,
                    "Original e-Pattadar Passbook + Meebhoomi 1B extract",
                    "Download from meebhoomi.ap.gov.in or MeeSeva."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Approved Building Sanction Plan / DTCP Layout Approval",
                    "Competent authority layout or building construction approval (APCRDA / DTCP / Municipal Corporation).",
                    false,
                    "Approved blueprint / sanction order copy",
                    "Obtain from builder or municipal town planning wing."
            ));
            propService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Section 194-IA 1% TDS Payment Challan (Form 26QB)",
                    "Statutory income tax compliance certificate required for all property sales valued at ₹50 Lakhs or above.",
                    false,
                    "Form 26QB acknowledgement challan printout",
                    "File online on Income Tax e-filing portal (eportal.incometax.gov.in)."
            ));

            serviceRepository.save(propService);
            log.info("Property / Land Revenue Registration government service seeded successfully into PostgreSQL database.");
        }

        // Initialize Voter ID Registration & Address Change service
        if (!serviceRepository.existsBySlug("voter-id-registration-address-change")) {
            com.onepath.entity.Service voterService = new com.onepath.entity.Service();
            voterService.setName("Voter ID Registration & Address Change");
            voterService.setSlug("voter-id-registration-address-change");
            voterService.setCategory("Government & Civic Services");
            voterService.setDescription("Official Election Commission of India (ECI) service for fresh voter enrollment (Form 6), shifting of residence / address modification (Form 8), correction of existing electoral roll entries, replacement of lost or damaged Voter ID (EPIC) cards, and instant digital e-EPIC download.");
            voterService.setEligibility("Indian citizen who has attained 18 years of age or will turn 18 on any of the 4 statutory qualifying dates (1st Jan, 1st Apr, 1st Jul, 1st Oct), ordinarily resident in the Assembly Constituency, and not disqualified under the Representation of the People Act, 1950.");
            voterService.setTimeline("15–30 Working Days (Physical BLO Field Verification & Speed Post Delivery) | Instant for e-EPIC Download & Status Search");
            voterService.setFees("Free of Cost (₹0.00 — No government fees are charged for Form 6, Form 8, or delivery of new PVC EPIC card)");
            voterService.setImportantNotes("Submitting an application does not automatically guarantee enrollment; inclusion in the electoral roll strictly requires verification by the Booth Level Officer (BLO) and approval by the Electoral Registration Officer (ERO). Shifting residence automatically deletes old polling station enrollment. For lost cards, an official Police Complaint / Lost Article Report copy is mandatory. Citizens can also seek physical assistance through local Village / Ward Secretariats (Grama Ward Sachivalayam in Andhra Pradesh).");
            voterService.setOfficialPortalUrl("https://voters.eci.gov.in/");
            voterService.setHasQuestionnaire(true);

            // Steps
            voterService.addStep(new com.onepath.entity.ServiceStep(
                    1,
                    "Register / Login on ECI Voters' Services Portal",
                    "Visit the official Election Commission portal (https://voters.eci.gov.in/) or download the Voter Helpline App. Register using your mobile number and verify via OTP to access citizen voter services.",
                    "5 Minutes",
                    "Always ensure you are using the official government domain (.gov.in) or verified Voter Helpline App."
            ));
            voterService.addStep(new com.onepath.entity.ServiceStep(
                    2,
                    "Select Relevant Form (Form 6 for New Voter / Form 8 for Shifting, Correction or Replacement)",
                    "Choose the appropriate statutory form: Form 6 for brand new voter registration; Form 8 for shifting of residence, correction of entries, or replacement of lost/damaged Voter ID.",
                    "5 Minutes",
                    "Do not submit Form 6 if you are already registered; use Form 8 to transfer or correct your existing EPIC number."
            ));
            voterService.addStep(new com.onepath.entity.ServiceStep(
                    3,
                    "Fill Application Details & Upload Supporting Proofs",
                    "Input personal, family, and address particulars. Upload clear self-attested copies of Proof of Age, Proof of Residence, or Proof of Correction, along with a recent color passport-size photograph.",
                    "15–20 Minutes",
                    "For lost cards, attaching an official Police Lost Report / FIR copy is mandatory."
            ));
            voterService.addStep(new com.onepath.entity.ServiceStep(
                    4,
                    "Submit Online & Note 11-Digit Reference Number",
                    "Review the completed application on screen, verify all spellings, and submit. Download the acknowledgment slip containing your 11-digit Reference ID for online status tracking.",
                    "2 Minutes",
                    "Preserve your 11-digit Reference ID carefully to monitor the verification stages."
            ));
            voterService.addStep(new com.onepath.entity.ServiceStep(
                    5,
                    "Booth Level Officer (BLO) Physical Field Verification",
                    "The Electoral Registration Officer (ERO) assigns your application to the designated Booth Level Officer (BLO), who visits your residence to verify original documents and confirm physical occupancy.",
                    "7–15 Days",
                    "Keep original documents ready at home for the BLO physical visit."
            ));
            voterService.addStep(new com.onepath.entity.ServiceStep(
                    6,
                    "Electoral Roll Publication & Free PVC Card Speed Post Delivery",
                    "Upon ERO statutory approval, your name is published in the electoral roll. You can immediately download your digital e-EPIC PDF, and a high-security PVC Voter Card is dispatched via Speed Post free of cost.",
                    "Within 30 Days",
                    "Delivery of the new PVC EPIC card via India Post Speed Post is completely free."
            ));

            // Documents
            voterService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Date of Birth & Age (Birth Certificate / Aadhaar / 10th Marksheet / PAN / DL)",
                    "Statutory document proving date of birth and age eligibility (18+ on qualifying date).",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official record verifying your exact date of birth.\nACCEPTED DOCUMENTS: Birth Certificate issued by Municipal Authority/Registrar of Births, Aadhaar Card, PAN Card, Driving Licence, or Class 10th / Matriculation Marksheet/Certificate showing DOB.\nWHERE TO OBTAIN: DigiLocker (digilocker.gov.in), municipal corporation, school education board, or respective issuing authorities.\nHOW TO OBTAIN: Download verified digital copy from DigiLocker or scan the original hardcopy.\nCOMMON MISTAKES: Submitting documents where the name or date of birth differs from your voter application."
            ));
            voterService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Proof of Ordinary Residence (Aadhaar / Utility Bill / Bank Passbook / Indian Passport / Rent Agreement)",
                    "Document proving ordinary residence in the applied Assembly Constituency.",
                    true,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official record establishing ordinary residence at the applied address within the constituency.\nACCEPTED DOCUMENTS: Latest Water/Electricity/Gas connection bill (under 3 months old in applicant's, parent's, or spouse's name), Aadhaar Card, Current Bank/Post Office Passbook, Indian Passport, or Registered Rent/Lease Deed.\nWHERE TO OBTAIN: Electricity distribution company, municipal water board, bank branch, or UIDAI.\nHOW TO OBTAIN: Download latest online bill or obtain bank passbook statement. If renting, ensure the rent agreement is registered.\nCOMMON MISTAKES: Submitting temporary hostel addresses without permanent parental proof, or utility bills older than 3 months."
            ));
            voterService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Recent Color Passport-Sized Photograph (White Background)",
                    "Recent color portrait photo taken within the last 3 months with a clean white/light background.",
                    true,
                    "JPG / JPEG (3.5 cm x 4.5 cm, Max 2MB)",
                    "WHAT IT IS: Formal passport photograph printed on your physical PVC Voter ID card and electoral roll.\nSPECIFICATIONS: 3.5 cm x 4.5 cm color photo, front-facing, neutral expression, eyes open, ears clearly visible, clean white background. No sunglasses, uniforms, or caps.\nWHERE TO OBTAIN: Any photo studio or high-resolution smartphone camera with good lighting against a plain white wall.\nCOMMON MISTAKES: Uploading casual selfies, cropped group photos, or low-resolution social media pictures."
            ));
            voterService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Existing Voter ID (EPIC) Number (For Form 8 / Shifting / Correction / Replacement)",
                    "10-character alphanumeric Electoral Photo Identity Card (EPIC) number currently allotted to you.",
                    false,
                    "10-Character Alphanumeric Code",
                    "WHAT IT IS: Your unique voter identification number (e.g., ABC1234567) printed on the front of your existing Voter Card.\nWHERE TO FIND IT: On your existing physical Voter Card, previous Voter Information Slip, or search by details at electoralsearch.eci.gov.in.\nHOW TO USE: Enter in Form 8 to automatically fetch your current polling station details."
            ));
            voterService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Police FIR / Online Lost Article Report Copy (Mandatory ONLY for Lost Cards)",
                    "Official police acknowledgement verifying reporting of the lost Voter ID card.",
                    false,
                    "Signed PDF / Police Stamp Copy",
                    "WHAT IT IS: Official report filed with local police station or via State Police Citizen Portal reporting the loss of your Voter ID.\nWHERE TO OBTAIN: Online citizen portal of State Police (e.g. AP Police Citizen Services at appolice.gov.in) or local police station.\nCONDITIONS: Mandatory ONLY if the replacement reason selected is 'Lost'. Not required if destroyed by fire/flood or mutilated."
            ));
            voterService.addDocument(new com.onepath.entity.ServiceRequiredDocument(
                    "Documentary Proof Supporting Requested Correction (Marksheet / Gazette / Aadhaar / Passport)",
                    "Statutory proof establishing the correct detail (Marksheet, Birth Certificate, Gazette, or Marriage Certificate).",
                    false,
                    "Self-Attested PDF / JPG (Max 2MB)",
                    "WHAT IT IS: Official government or educational record verifying the exact corrected data.\nFOR NAME CORRECTION: Official Gazette notification, Marriage Certificate (for surname change post-marriage), or Passport.\nFOR DOB CORRECTION: Birth Certificate, Class 10th marksheet, or Aadhaar Card.\nFOR RELATIVE NAME: School leaving certificate or family ration card."
            ));

            serviceRepository.save(voterService);
            log.info("Voter ID Registration & Address Change government service seeded successfully into PostgreSQL database.");
        }

        // Seed verified Andhra Pradesh physical service centres directory (Vijayawada priority + AP cities)
        seedServiceCenters();

        // Seed verified government updates and circulars
        seedPlatformUpdates();

        // NOTE: College and CollegeProcess tables are intentionally KEPT EMPTY.
        // As requested by user specifications: "The CollegeProcess table must initially be empty."
        log.info("OnePath system initialized successfully. College processes table is empty as expected.");
    }

    private void seedServiceCenters() {
        java.util.List<com.onepath.entity.ServiceCenter> centers = java.util.List.of(
            // --- VIJAYAWADA CENTRES (PRIORITY FOCUS - 8 CENTRES) ---
            new com.onepath.entity.ServiceCenter(
                "Regional Passport Office (RPO) Vijayawada",
                "rpo-vijayawada",
                "PASSPORT",
                "Regional Passport Office (RPO)",
                "4th Floor, Stalin Central, Door No. 27-37-158, Governorpet, M.G. Road, Vijayawada, Andhra Pradesh",
                "Governorpet",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520002",
                "0866-2570000",
                "Mon - Fri: 9:30 AM - 4:00 PM (Prior inquiry token required)",
                "Passport Jurisdiction Administration, Complex Inquiries, Grievances, Surrender & Revocation Inquiries, Appeals (Note: Routine new passport applications and biometrics are accepted at PSK and POPSK)",
                true,
                true,
                "https://www.passportindia.gov.in",
                16.5135,
                80.6276
            ),
            new com.onepath.entity.ServiceCenter(
                "Passport Seva Kendra (PSK) Vijayawada",
                "psk-vijayawada",
                "PASSPORT",
                "Passport Seva Kendra (PSK)",
                "Door No. 39-10-7, Opp. All India Radio (AIR) Station, Venkateswara Theatre Complex, M.G. Road (Bandar Road), Labbipet, Vijayawada, Andhra Pradesh",
                "Labbipet",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520010",
                "1800-258-1800",
                "Mon - Fri: 9:00 AM - 5:30 PM (Prior appointment booking mandatory)",
                "Fresh Passport Application, Passport Re-issue & Renewal, Tatkaal Passport, Minor Passport, Police Clearance Certificate (PCC), Biometric Capture & Document Verification",
                true,
                false,
                "https://www.passportindia.gov.in",
                16.5042,
                80.6433
            ),
            new com.onepath.entity.ServiceCenter(
                "Deputy Transport Commissioner & RTO Office, Vijayawada (AP-16)",
                "rto-vijayawada",
                "DRIVING_LICENCE",
                "Regional Transport Office (RTO)",
                "M.G. Road (Bandar Road), Opp. Indira Gandhi Municipal Stadium, Vijayawada, Andhra Pradesh",
                "Bandar Road",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520010",
                "0866-2476566",
                "Mon - Sat: 10:00 AM - 5:00 PM (2nd Saturdays & Sundays closed)",
                "Permanent Driving Licence (DL) Issuance, Driving Licence Renewal, Duplicate DL, International Driving Permit (IDP), Vehicle Registration (RC), RC Ownership Transfer, Hypothecation Endorsement",
                true,
                false,
                "https://www.aptransport.org",
                16.5074,
                80.6385
            ),
            new com.onepath.entity.ServiceCenter(
                "RTO Unit Office & Automated Driving Test Track, Auto Nagar",
                "rto-test-track-auto-nagar-vijayawada",
                "DRIVING_LICENCE",
                "RTA Unit Office / Test Facility",
                "7th Cross Road, Industrial Estate, Auto Nagar, Vijayawada, Andhra Pradesh",
                "Auto Nagar",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520007",
                "0866-2543322",
                "Mon - Sat: 10:00 AM - 4:30 PM (Slot booking required)",
                "Learner's Licence (LL) Computer-Based Test, Permanent DL Practical Driving Test (Two-Wheeler & Four-Wheeler Tracks), Vehicle Fitness Inspection",
                true,
                false,
                "https://www.aptransport.org",
                16.4950,
                80.6720
            ),
            new com.onepath.entity.ServiceCenter(
                "Tahsildar & Mandal Revenue Office (MRO), Vijayawada Central",
                "tahsildar-office-vijayawada-central",
                "CERTIFICATES",
                "Tahsildar / Mandal Revenue Office",
                "Revenue Complex, Opp. Sub-Collector Office Road, Gandhi Nagar, Vijayawada, Andhra Pradesh",
                "Gandhi Nagar",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520003",
                "0866-2574455",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "Verification and Statutory Approval of EWS Certificate, Annual Family Income Certificate, Integrated Caste Certificate (SC/ST/BC), Residence Certificate, Revenue Field Inquiries",
                true,
                false,
                "https://ap.meeseva.gov.in",
                16.5186,
                80.6225
            ),
            new com.onepath.entity.ServiceCenter(
                "Tahsildar & Mandal Revenue Office (MRO), Vijayawada West",
                "tahsildar-office-vijayawada-west",
                "CERTIFICATES",
                "Tahsildar / Mandal Revenue Office",
                "Near Main Road, Bhavanipuram, Vijayawada, Andhra Pradesh",
                "Bhavanipuram",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520012",
                "0866-2412345",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "EWS Certificate Approval, Income Verification, Caste & Community Certificate Verification for West Mandal Citizens",
                true,
                false,
                "https://ap.meeseva.gov.in",
                16.5270,
                80.5980
            ),
            new com.onepath.entity.ServiceCenter(
                "MeeSeva Model Government Citizen Service Centre, Vijayawada",
                "meeseva-model-centre-vijayawada",
                "CERTIFICATES",
                "MeeSeva Government Centre",
                "Collectorate Administrative Complex, Chilakalapudi / Bandar Road, Vijayawada, Andhra Pradesh",
                "Governorpet",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520002",
                "0866-2495566",
                "Mon - Sat: 9:30 AM - 5:30 PM",
                "EWS Certificate Application Submission, Income Certificate Request, Caste Certificate Application, Biometric Authentication, Document Scanning & Fee Payment",
                true,
                false,
                "https://ap.meeseva.gov.in",
                16.5110,
                80.6300
            ),
            new com.onepath.entity.ServiceCenter(
                "MeeSeva Authorized Franchise (Assisted-Service), Benz Circle",
                "meeseva-benz-circle-vijayawada",
                "CERTIFICATES",
                "MeeSeva Authorized Franchise (Assisted-Service)",
                "Shop No. 4, Ground Floor, Near Jyothi Mahal, Benz Circle, Vijayawada, Andhra Pradesh",
                "Benz Circle",
                "Vijayawada",
                "NTR District",
                "Andhra Pradesh",
                "520010",
                "0866-2481122",
                "Mon - Sat: 9:00 AM - 6:00 PM",
                "Assisted Online Application for EWS, Income and Caste Certificates, Challan Payment, Aadhaar Updation Assistance (Note: Authorized private franchise operator providing assisted citizen services)",
                false,
                false,
                "https://ap.meeseva.gov.in",
                16.5002,
                80.6480
            ),

            // --- GUNTUR CENTRES (3 CENTRES) ---
            new com.onepath.entity.ServiceCenter(
                "Post Office Passport Seva Kendra (POPSK) Guntur",
                "popsk-guntur",
                "PASSPORT",
                "Post Office Passport Seva Kendra (POPSK)",
                "Head Post Office, D.No. 4-5-67/3, Near BSNL Office, Chandra Mouli Nagar 1st Lane, Koretipadu, Guntur, Andhra Pradesh",
                "Koretipadu",
                "Guntur",
                "Guntur District",
                "Andhra Pradesh",
                "522007",
                "1800-258-1800",
                "Mon - Fri: 9:30 AM - 4:30 PM (Prior appointment mandatory)",
                "Normal Fresh Passport, Passport Re-issue, Document Verification & Biometrics (Note: Tatkaal and complex categories require PSK Vijayawada)",
                true,
                false,
                "https://www.passportindia.gov.in",
                16.3120,
                80.4340
            ),
            new com.onepath.entity.ServiceCenter(
                "Regional Transport Office (RTO), Guntur (AP-07)",
                "rto-guntur",
                "DRIVING_LICENCE",
                "Regional Transport Office (RTO)",
                "JKC College Road, Swarna Bharati Nagar, Guntur, Andhra Pradesh",
                "Swarna Bharati Nagar",
                "Guntur",
                "Guntur District",
                "Andhra Pradesh",
                "522006",
                "0863-2355194",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "Learner's Licence, Permanent Driving Licence, DL Renewal, Driving Practical Test, Vehicle Registration (RC), NOC, Fitness Certificate",
                true,
                false,
                "https://www.aptransport.org",
                16.3260,
                80.4180
            ),
            new com.onepath.entity.ServiceCenter(
                "Integrated Citizen Service Centre (MeeSeva), Guntur Collectorate",
                "meeseva-collectorate-guntur",
                "CERTIFICATES",
                "MeeSeva Government Centre",
                "District Collectorate Compound, Nagarampalem, Guntur, Andhra Pradesh",
                "Nagarampalem",
                "Guntur",
                "Guntur District",
                "Andhra Pradesh",
                "522004",
                "0863-2234001",
                "Mon - Sat: 9:30 AM - 5:30 PM",
                "EWS Application, Income Certificate Submission, Caste / Community Certificate, Land Record Adangal, Citizen Certificate Delivery",
                true,
                false,
                "https://ap.meeseva.gov.in",
                16.3020,
                80.4480
            ),

            // --- TIRUPATI CENTRES (3 CENTRES) ---
            new com.onepath.entity.ServiceCenter(
                "Passport Seva Kendra (PSK) Tirupati",
                "psk-tirupati",
                "PASSPORT",
                "Passport Seva Kendra (PSK)",
                "Door No. 19-4-9, R101 to 105, Prince Avenue, Annamaya Marg, AIR Bypass Road, STV Nagar, Tirupati, Andhra Pradesh",
                "STV Nagar",
                "Tirupati",
                "Tirupati District",
                "Andhra Pradesh",
                "517501",
                "1800-258-1800",
                "Mon - Fri: 9:00 AM - 5:00 PM (Prior appointment mandatory)",
                "Fresh Passport Application, Passport Renewal, Tatkaal Passport, Minor Passport, PCC, Biometric Capture & Verification",
                true,
                false,
                "https://www.passportindia.gov.in",
                13.6300,
                79.4200
            ),
            new com.onepath.entity.ServiceCenter(
                "District Transport Office (DTO/RTO), Tirupati (AP-03)",
                "rto-tirupati",
                "DRIVING_LICENCE",
                "Regional Transport Office (RTO)",
                "Karakambadi Road, Mangalam, Tirupati, Tirupati District, Andhra Pradesh",
                "Mangalam",
                "Tirupati",
                "Tirupati District",
                "Andhra Pradesh",
                "517507",
                "0877-2287733",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "Learner's Licence, Driving Licence Test, Commercial Badge, Permanent DL, Vehicle Registration, Address Change, Road Tax Collection",
                true,
                false,
                "https://www.aptransport.org",
                13.6550,
                79.4480
            ),
            new com.onepath.entity.ServiceCenter(
                "Tahsildar Office & MeeSeva Citizen Centre, Tirupati Urban",
                "tahsildar-meeseva-tirupati",
                "CERTIFICATES",
                "Tahsildar Office & MeeSeva Centre",
                "Revenue Ward Complex, Near Sub-Collector Office, Tirupati, Andhra Pradesh",
                "Sub-Collector Office Road",
                "Tirupati",
                "Tirupati District",
                "Andhra Pradesh",
                "517501",
                "0877-2250100",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "EWS Certificate Application & Revenue Inquiry, Income Attestation, Integrated Caste & Nativity Certificates",
                true,
                false,
                "https://ap.meeseva.gov.in",
                13.6288,
                79.4192
            ),

            // --- VISAKHAPATNAM CENTRES (2 CENTRES) ---
            new com.onepath.entity.ServiceCenter(
                "Passport Seva Kendra (PSK) Visakhapatnam",
                "psk-visakhapatnam",
                "PASSPORT",
                "Passport Seva Kendra (PSK)",
                "Botcha Square, Door No. 39-6-71, Kapparada Village, Opp. Birla Junction, Murali Nagar, Visakhapatnam, Andhra Pradesh",
                "Murali Nagar",
                "Visakhapatnam",
                "Visakhapatnam District",
                "Andhra Pradesh",
                "530007",
                "1800-258-1800",
                "Mon - Fri: 9:00 AM - 5:30 PM (Prior appointment mandatory)",
                "Fresh Passport Application, Passport Renewal, Tatkaal, Police Clearance Certificate (PCC), Biometrics & Document Verification",
                true,
                false,
                "https://www.passportindia.gov.in",
                17.7420,
                83.2750
            ),
            new com.onepath.entity.ServiceCenter(
                "Office of Deputy Transport Commissioner & RTO-1, Visakhapatnam (AP-31)",
                "rto-visakhapatnam",
                "DRIVING_LICENCE",
                "Regional Transport Office (RTO)",
                "Madhavadhara VUDA Layout, Behind R&B Office, NH-16, Visakhapatnam, Andhra Pradesh",
                "Madhavadhara",
                "Visakhapatnam",
                "Visakhapatnam District",
                "Andhra Pradesh",
                "530018",
                "0891-2559777",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "Learner's Licence, Permanent DL, Track Driving Tests, RC Book Issuance, Ownership Transfer, Commercial Transport Permits",
                true,
                false,
                "https://www.aptransport.org",
                17.7550,
                83.2680
            ),

            // --- KADAPA CENTRES (2 CENTRES) ---
            new com.onepath.entity.ServiceCenter(
                "Post Office Passport Seva Kendra (POPSK) Kadapa",
                "popsk-kadapa",
                "PASSPORT",
                "Post Office Passport Seva Kendra (POPSK)",
                "Kadapa Head Post Office, Near One Town Police Station, Christian Lane, Kadapa, Andhra Pradesh",
                "Christian Lane",
                "Kadapa",
                "YSR Kadapa District",
                "Andhra Pradesh",
                "516001",
                "1800-258-1800",
                "Mon - Fri: 9:30 AM - 4:30 PM",
                "Normal Fresh Passport, Passport Renewal, Biometrics & Document Verification",
                true,
                false,
                "https://www.passportindia.gov.in",
                14.4750,
                78.8250
            ),
            new com.onepath.entity.ServiceCenter(
                "Regional Transport Office (RTO), Kadapa (AP-04)",
                "rto-kadapa",
                "DRIVING_LICENCE",
                "Regional Transport Office (RTO)",
                "Yerramukkapalli, RTA Complex, Kadapa, Andhra Pradesh",
                "Yerramukkapalli",
                "Kadapa",
                "YSR Kadapa District",
                "Andhra Pradesh",
                "516004",
                "08562-244222",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "Learner's Licence, Permanent Driving Licence, DL Renewal, Vehicle Registration & Transfer",
                true,
                false,
                "https://www.aptransport.org",
                14.4580,
                78.8120
            ),

            // --- KURNOOL CENTRE (1 CENTRE) ---
            new com.onepath.entity.ServiceCenter(
                "Post Office Passport Seva Kendra (POPSK) Kurnool",
                "popsk-kurnool",
                "PASSPORT",
                "Post Office Passport Seva Kendra (POPSK)",
                "Kurnool Head Post Office, Near Konda Reddy Buruju, Kurnool, Andhra Pradesh",
                "Konda Reddy Buruju Area",
                "Kurnool",
                "Kurnool District",
                "Andhra Pradesh",
                "518001",
                "1800-258-1800",
                "Mon - Fri: 9:30 AM - 4:30 PM",
                "Normal Fresh Passport, Passport Renewal, Biometrics & Document Verification",
                true,
                false,
                "https://www.passportindia.gov.in",
                15.8280,
                78.0370
            ),

            // --- NELLORE CENTRE (1 CENTRE) ---
            new com.onepath.entity.ServiceCenter(
                "Regional Transport Office (RTO), Nellore (AP-26)",
                "rto-nellore",
                "DRIVING_LICENCE",
                "Regional Transport Office (RTO)",
                "Podalakur Road, Ramamurthy Nagar, Nellore, Andhra Pradesh",
                "Ramamurthy Nagar",
                "Nellore",
                "SPSR Nellore District",
                "Andhra Pradesh",
                "524004",
                "0861-2326788",
                "Mon - Sat: 10:00 AM - 5:00 PM",
                "Learner's Licence, Permanent Driving Licence, Driving Test Track, RC Transfer, Fitness Certificate",
                true,
                false,
                "https://www.aptransport.org",
                14.4320,
                79.9650
            )
        );

        int addedCount = 0;
        for (com.onepath.entity.ServiceCenter center : centers) {
            if (!serviceCenterRepository.existsBySlug(center.getSlug())) {
                serviceCenterRepository.save(center);
                addedCount++;
            }
        }
        if (addedCount > 0) {
            log.info("Seeded {} verified Andhra Pradesh physical service centres into PostgreSQL database.", addedCount);
        }
    }

    private void seedPlatformUpdates() {
        if (!updateRepository.existsByTitle("UIDAI Revised Standard Operating Procedure (SOP) for Aadhaar Name Changes")) {
            com.onepath.entity.Update aadhaarUpdate = new com.onepath.entity.Update(
                    "UIDAI Revised Standard Operating Procedure (SOP) for Aadhaar Name Changes",
                    "The Unique Identification Authority of India (UIDAI) has implemented a revised Standard Operating Procedure (SOP) governing resident name modifications in the Aadhaar database, effective 18 September 2026.\n\n" +
                    "• Minor Typographical Corrections: Minor spelling, phonetics, spacing, or transliteration adjustments between English and regional languages require standard Proof of Identity (PoI) documentation (such as Passport, PAN Card, or Voter ID).\n" +
                    "• Formal Legal Name Changes: Major name changes, surname additions post-marriage, or complete legal name alterations require specialized legal validation—including an official Central/State Gazette Notification and original Marriage Certificate where applicable.\n" +
                    "• Verification Tiers: Aadhaar Seva Kendras (ASK) now enforce tiered biometric verification and document scrutiny based on the applicant's lifetime update threshold (maximum 2 lifetime updates permitted without Regional Office exception approval).",
                    "Policy & Rules",
                    "Aadhaar Card Update & Correction",
                    java.time.LocalDate.of(2026, 9, 18),
                    false,
                    "https://uidai.gov.in/"
            );
            updateRepository.save(aadhaarUpdate);
            log.info("Aadhaar Name Change SOP government update seeded successfully.");
        }

        if (!updateRepository.existsByTitle("MoRTH Draft Notification: Proposed Driving Licence Rules & Documentation Amendments")) {
            com.onepath.entity.Update dlUpdate = new com.onepath.entity.Update(
                    "MoRTH Draft Notification: Proposed Driving Licence Rules & Documentation Amendments",
                    "[STATUS: DRAFT PROPOSAL — NOT CURRENTLY IN FORCE]\n\n" +
                    "The Ministry of Road Transport and Highways (MoRTH) has published proposed draft amendments to the Central Motor Vehicles Rules (CMVR) regarding driving licence administration and citizen documentation:\n\n" +
                    "• Online Service Streamlining: Proposals seek to eliminate in-person RTO visits for DL address updates, badge renewals, and duplicate licence requests through paperless Aadhaar e-KYC integration.\n" +
                    "• Automated Testing Standardisation: Driving skill tests on accredited Automated Driving Test Tracks (ADTT) will follow standardized sensor-based evaluation metrics nationwide.\n" +
                    "• Digital Medical Certificate (Form 1A): Direct electronic transmission of medical fitness certifications from registered medical practitioners to the Parivahan Sarathi portal.\n\n" +
                    "IMPORTANT NOTICE: This measure is an official draft amendment issued for public stakeholder objections and suggestions; it does NOT constitute an active statutory rule until final notification is gazetted.",
                    "Policy & Rules",
                    "Driving Licence & Vehicle Registration",
                    java.time.LocalDate.of(2026, 10, 15),
                    false,
                    "https://parivahan.gov.in/"
            );
            updateRepository.save(dlUpdate);
            log.info("MoRTH DL draft amendment government update seeded successfully.");
        }
    }
}
