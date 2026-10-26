import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { checklistApi, civicServicesApi } from '../services/api';
import { ChecklistItem } from '../types';
import {
  GraduationCap,
  ArrowLeft,
  CheckCircle2,
  AlertTriangle,
  Building2,
  ExternalLink,
  ShieldCheck,
  Clock,
  Calendar,
  FileCheck2,
  Sparkles,
  HelpCircle,
  BookOpen,
  Info,
  Check,
  ChevronDown,
  ChevronUp,
  MapPin,
  Lock,
  Layers,
  Award,
  Compass,
  AlertCircle,
  Banknote,
  RotateCcw,
  ListOrdered,
  FileText,
} from 'lucide-react';

// ==========================================
// INSTITUTION DIRECTORY DATA (VERIFIED SOURCES)
// ==========================================
interface VerifiedInstitute {
  id: string;
  name: string;
  shortName: string;
  type: 'IIT' | 'NIT' | 'IIIT' | 'AP_STATE';
  location: string;
  state: string;
  website: string;
  cutoffUrl: string;
  popularBranches: string[];
  referenceCutoffNote: string;
  description: string;
}

const VERIFIED_INSTITUTES: VerifiedInstitute[] = [
  // IITs
  {
    id: 'iit-bombay',
    name: 'Indian Institute of Technology Bombay',
    shortName: 'IIT Bombay',
    type: 'IIT',
    location: 'Powai, Mumbai',
    state: 'Maharashtra',
    website: 'https://www.iitb.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Electrical Engineering', 'Mechanical Engineering', 'Engineering Physics'],
    referenceCutoffNote: 'Previous Year Opening-Closing Reference: CSE (CRL ~1 - 67). Historical benchmark only.',
    description: 'Premier national technical research institution. Admissions strictly via JEE Advanced merit in JoSAA.',
  },
  {
    id: 'iit-delhi',
    name: 'Indian Institute of Technology Delhi',
    shortName: 'IIT Delhi',
    type: 'IIT',
    location: 'Hauz Khas, New Delhi',
    state: 'Delhi',
    website: 'https://home.iitd.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Mathematics and Computing', 'Electrical Engineering', 'Civil Engineering'],
    referenceCutoffNote: 'Previous Year Opening-Closing Reference: CSE (CRL ~25 - 118). Historical benchmark only.',
    description: 'Institute of Eminence offering top-tier engineering and research degrees.',
  },
  {
    id: 'iit-madras',
    name: 'Indian Institute of Technology Madras',
    shortName: 'IIT Madras',
    type: 'IIT',
    location: 'Chennai',
    state: 'Tamil Nadu',
    website: 'https://www.iitm.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Electrical Engineering', 'Aerospace Engineering', 'Mechanical Engineering'],
    referenceCutoffNote: 'Ranked #1 in NIRF Engineering. Previous Year CSE Opening-Closing: ~20 - 150.',
    description: 'NIRF consistently #1 overall institution in India with cutting-edge incubation facilities.',
  },
  {
    id: 'iit-tirupati',
    name: 'Indian Institute of Technology Tirupati',
    shortName: 'IIT Tirupati (AP)',
    type: 'IIT',
    location: 'Yerpedu, Tirupati',
    state: 'Andhra Pradesh',
    website: 'https://www.iittp.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Electrical Engineering', 'Chemical Engineering', 'Mechanical Engineering'],
    referenceCutoffNote: 'Fastest growing 3rd Gen IIT in Andhra Pradesh. Previous Year CSE Closing: ~3,400 - 4,000.',
    description: 'Premier 3rd Generation IIT located in Andhra Pradesh near the temple city of Tirupati.',
  },

  // NITs
  {
    id: 'nit-trichy',
    name: 'National Institute of Technology Tiruchirappalli',
    shortName: 'NIT Trichy',
    type: 'NIT',
    location: 'Tiruchirappalli',
    state: 'Tamil Nadu',
    website: 'https://www.nitt.edu',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Electronics & Communication', 'Electrical & Electronics', 'Mechanical'],
    referenceCutoffNote: 'Top-ranked NIT nationally. 50% Home State / 50% Other State quota split applies.',
    description: 'Consistently ranked #1 among all 31 National Institutes of Technology.',
  },
  {
    id: 'nit-warangal',
    name: 'National Institute of Technology Warangal',
    shortName: 'NIT Warangal',
    type: 'NIT',
    location: 'Warangal',
    state: 'Telangana',
    website: 'https://www.nitw.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'ECE', 'Data Science & AI', 'Mechanical Engineering'],
    referenceCutoffNote: 'Very high demand among Telugu students. Home state quota for Telangana Class 12 candidates.',
    description: 'Premier regional engineering college established in 1959, highly recognized for campus placements.',
  },
  {
    id: 'nit-andhra',
    name: 'National Institute of Technology Andhra Pradesh',
    shortName: 'NIT Andhra Pradesh (AP)',
    type: 'NIT',
    location: 'Tadepalligudem, West Godavari',
    state: 'Andhra Pradesh',
    website: 'https://www.nitandhra.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Electronics & Communication', 'Electrical Engineering', 'Civil Engineering'],
    referenceCutoffNote: 'Home State (AP) 50% Quota: Candidates passing Class 12 in Andhra Pradesh receive 50% seat reservation.',
    description: 'Andhra Pradesh flagship central technical institute located in Tadepalligudem on a 178-acre campus.',
  },
  {
    id: 'nit-surathkal',
    name: 'National Institute of Technology Karnataka, Surathkal',
    shortName: 'NIT Surathkal',
    type: 'NIT',
    location: 'Mangalore',
    state: 'Karnataka',
    website: 'https://www.nitk.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Information Technology', 'ECE', 'Artificial Intelligence'],
    referenceCutoffNote: 'Top 3 national NIT with its own private beach and stellar international placement records.',
    description: 'Pioneering technical institution with world-class faculty and industry partnerships.',
  },

  // IIITs
  {
    id: 'iiit-hyderabad',
    name: 'International Institute of Information Technology Hyderabad',
    shortName: 'IIIT Hyderabad',
    type: 'IIIT',
    location: 'Gachibowli, Hyderabad',
    state: 'Telangana',
    website: 'https://www.iiit.ac.in',
    cutoffUrl: 'https://www.iiit.ac.in/admissions',
    popularBranches: ['Computer Science & Engineering', 'Electronics & Communication Engineering', 'Dual Degree Research'],
    referenceCutoffNote: 'Conducts own portal admissions based on JEE Main percentile and UGEE. Top AI research in India.',
    description: 'Autonomous research university consistently ranked on par with top IITs for Computer Science.',
  },
  {
    id: 'iiit-sricity',
    name: 'Indian Institute of Information Technology Sri City, Chittoor',
    shortName: 'IIIT Sri City (AP)',
    type: 'IIIT',
    location: 'Sri City, Tirupati District',
    state: 'Andhra Pradesh',
    website: 'https://www.iiits.ac.in',
    cutoffUrl: 'https://josaa.nic.in',
    popularBranches: ['Computer Science & Engineering', 'Artificial Intelligence & Machine Learning', 'Cyber Security', 'ECE'],
    referenceCutoffNote: 'Admissions through JoSAA/CSAB based on JEE Main CRL Rank. Located in industrial business hub.',
    description: 'Institute of National Importance (PPP model) located in Sri City SEZ near Tada, Andhra Pradesh.',
  },

  // AP State Universities
  {
    id: 'jntu-kakinada',
    name: 'Jawaharlal Nehru Technological University Kakinada (UCEK)',
    shortName: 'JNTU Kakinada (JNTUK)',
    type: 'AP_STATE',
    location: 'Kakinada',
    state: 'Andhra Pradesh',
    website: 'https://www.jntuk.edu.in',
    cutoffUrl: 'https://cets.apsche.ap.gov.in',
    popularBranches: ['Computer Science & Engineering', 'ECE', 'Mechanical Engineering', 'Petroleum Engineering'],
    referenceCutoffNote: 'Top state university in Andhra University (AU) Local Area region. Admissions via AP EAPCET web counselling.',
    description: 'One of the oldest and most prestigious state technical universities in Andhra Pradesh.',
  },
  {
    id: 'jntu-anantapur',
    name: 'Jawaharlal Nehru Technological University Anantapur (UCEA)',
    shortName: 'JNTU Anantapur (JNTUA)',
    type: 'AP_STATE',
    location: 'Ananthapuramu',
    state: 'Andhra Pradesh',
    website: 'https://www.jntua.ac.in',
    cutoffUrl: 'https://cets.apsche.ap.gov.in',
    popularBranches: ['Computer Science & Engineering', 'Electronics & Communication', 'Civil Engineering', 'Electrical Engineering'],
    referenceCutoffNote: 'Premier state university in Sri Venkateswara University (SVU) Local Area region. AP EAPCET convenor seats.',
    description: 'Leading state engineering university serving the Rayalaseema region with historic campus facilities.',
  },
  {
    id: 'andhra-university',
    name: 'Andhra University College of Engineering (Autonomous)',
    shortName: 'AU College of Engineering (AU Vizag)',
    type: 'AP_STATE',
    location: 'Visakhapatnam',
    state: 'Andhra Pradesh',
    website: 'https://www.andhrauniversity.edu.in',
    cutoffUrl: 'https://cets.apsche.ap.gov.in',
    popularBranches: ['Computer Science & Systems Engineering', 'ECE', 'Chemical Engineering', 'Marine Engineering'],
    referenceCutoffNote: 'Historic 1955 autonomous university college. Most sought-after state college in AP EAPCET.',
    description: 'Flagship engineering faculty of Andhra University overlooking the Bay of Bengal in Visakhapatnam.',
  },
  {
    id: 'svu-tirupati',
    name: 'Sri Venkateswara University College of Engineering',
    shortName: 'SVU College of Engineering (SVUCE)',
    type: 'AP_STATE',
    location: 'Tirupati',
    state: 'Andhra Pradesh',
    website: 'https://svuce.edu.in',
    cutoffUrl: 'https://cets.apsche.ap.gov.in',
    popularBranches: ['Computer Science & Engineering', 'Electronics & Communication', 'Electrical & Electronics', 'Mechanical'],
    referenceCutoffNote: 'Premier college in SVU region. Complete government fee structure with fee reimbursement eligibility.',
    description: 'Renowned state university college established in 1959 with high academic standing.',
  },
];

// ==========================================
// DOCUMENT CHECKLIST DEFINITION
// ==========================================
interface MasterDocument {
  id: string;
  name: string;
  category: 'COMMON' | 'CATEGORY' | 'STATE' | 'ALLOTMENT';
  mandatory: boolean;
  description: string;
  howToObtain: string;
  formats: string;
  strictRule?: string;
}

const MASTER_DOCUMENTS: MasterDocument[] = [
  {
    id: 'doc-10th',
    name: 'Class 10 Certificate / Marks Sheet',
    category: 'COMMON',
    mandatory: true,
    description: 'Primary statutory proof of candidate date of birth (DOB) and candidate name matching entrance application exactly.',
    howToObtain: 'Issued by CBSE / ICSE / State Secondary Board. DigiLocker verified digital copy is legally valid.',
    formats: 'Original PDF scan (under 500 KB)',
  },
  {
    id: 'doc-12th',
    name: 'Class 12 Marksheet & Passing Certificate',
    category: 'COMMON',
    mandatory: true,
    description: 'Proof of qualifying examination and aggregate percentage across 5 compulsory subjects (Physics, Math, Chem/Vocational, Language, 5th subject).',
    howToObtain: 'From intermediate college / higher secondary board. Download digital copy from DigiLocker.',
    formats: 'Original PDF scan (under 500 KB)',
  },
  {
    id: 'doc-rank',
    name: 'Entrance Examination Admit Card & Rank Score Card',
    category: 'COMMON',
    mandatory: true,
    description: 'Official JEE Main, JEE Advanced, or AP EAPCET Rank Score Card showing Common Rank List (CRL) and Category Rank.',
    howToObtain: 'Download directly from NTA (jeemain.nta.ac.in), JEE Adv (jeeadv.ac.in), or APSCHE (cets.apsche.ap.gov.in).',
    formats: 'Official downloaded PDF',
  },
  {
    id: 'doc-medical',
    name: 'Medical Fitness Certificate (Mandatory Annexure-7 Format)',
    category: 'COMMON',
    mandatory: true,
    description: 'Official JoSAA Annexure-7 medical certificate verifying physical fitness, visual acuity, color perception, and general health.',
    howToObtain: 'Download official Annexure-7 template from josaa.nic.in. Must be certified and stamped by a Registered Medical Practitioner (MBBS degree or higher) with registration number.',
    formats: 'Original signed & stamped PDF scan',
    strictRule: 'Mandatory for all candidates. Must carry MBBS doctor official seal and state council registration number.',
  },
  {
    id: 'doc-identity',
    name: 'Valid Government Photo Identity Card (Aadhaar / Passport)',
    category: 'COMMON',
    mandatory: true,
    description: 'Official proof of candidate identity matching application particulars.',
    howToObtain: 'Download mask-free e-Aadhaar from UIDAI portal (uidai.gov.in) or valid Passport / Voter ID.',
    formats: 'Color PDF / JPEG scan',
  },
  {
    id: 'doc-allotment',
    name: 'Provisional Seat Allotment Letter & SAF Fee Receipt',
    category: 'ALLOTMENT',
    mandatory: true,
    description: 'Official allotment letter generated by JoSAA / CSAB / AP EAPCET portal after seat allocation, together with payment receipt.',
    howToObtain: 'Download from candidate dashboard immediately after paying the Seat Acceptance Fee (SAF).',
    formats: 'Portal generated PDF',
  },
  {
    id: 'doc-obc-ews',
    name: 'OBC-NCL / EWS Certificate (STRICT: Issued ON or AFTER April 1)',
    category: 'CATEGORY',
    mandatory: false,
    description: 'Statutory certificate for OBC-NCL (27% Central Quota) or General-EWS (10% Central Quota).',
    howToObtain: 'Obtain from Tahsildar / Revenue Divisional Officer (RDO) / SDM in Central Government DoPT format.',
    formats: 'Central DoPT format PDF scan',
    strictRule: 'CRITICAL RULE: Must be issued ON or AFTER April 1 of the admission year. If issued prior, category status is canceled and converted to General.',
  },
  {
    id: 'doc-sc-st',
    name: 'SC / ST Community & Tribe Certificate',
    category: 'CATEGORY',
    mandatory: false,
    description: 'Permanent statutory caste/community certificate issued in prescribed format by authorized revenue officer (Tahsildar / MRO).',
    howToObtain: 'Issued by revenue department / MeeSeva with verifiable digital certificate number and government seal.',
    formats: 'Original PDF scan',
  },
  {
    id: 'doc-pwd',
    name: 'PwD Benchmark Disability Certificate / UDID Card',
    category: 'CATEGORY',
    mandatory: false,
    description: 'Disability certificate issued by authorized government Medical Board confirming minimum 40% benchmark disability.',
    howToObtain: 'Swavlamban UDID card or certificate from District Chief Medical Officer (CMO).',
    formats: 'Unique Disability ID (UDID) Card / PDF',
  },
  {
    id: 'doc-domicile',
    name: 'State Code of Eligibility Proof / Domicile Certificate',
    category: 'STATE',
    mandatory: true,
    description: 'Class 12 examination passing certificate proving the state where Class 12 was completed (for 50% Home State NIT quota and 85% AP local quota).',
    howToObtain: 'Class 12 school leaving / study certificate from intermediate college or residence certificate.',
    formats: 'College Study / Bonafide / Passing Certificate',
  },
  {
    id: 'doc-cheque',
    name: 'Crossed Bank Cancelled Cheque / Passbook Photocopy',
    category: 'ALLOTMENT',
    mandatory: true,
    description: 'Cancelled bank cheque or front page of passbook showing Account Holder Name, Account Number, and IFSC code (student or parent).',
    howToObtain: 'Take blank cheque leaf from student or parent bank account, cross with two parallel lines and write "CANCELLED".',
    formats: 'Original color scan of cancelled cheque',
    strictRule: 'Mandatory in case of seat withdrawal or refund of excess seat acceptance fee.',
  },
  {
    id: 'doc-tc-migration',
    name: 'Transfer Certificate (TC), Conduct & Migration Certificate',
    category: 'STATE',
    mandatory: false,
    description: 'Certificates issued by junior college / intermediate board required at the time of final physical institutional admission.',
    howToObtain: 'Obtain from intermediate college principal office upon completion of Class 12.',
    formats: 'Original physical certificates',
  },
];

// ==========================================
// ROADMAP TASKS DEFINITION
// ==========================================
interface RoadmapTask {
  id: string;
  stepNumber: number;
  title: string;
  description: string;
  category: 'PREPARATION' | 'CHOICE_FILLING' | 'ALLOTMENT' | 'REPORTING';
  portal: string;
}

const ROADMAP_TASKS: RoadmapTask[] = [
  {
    id: 'task-1',
    stepNumber: 1,
    title: 'Verify Dual Eligibility & 75% / 45% Board Aggregate',
    description: 'Verify your marks across 5 compulsory Class 12 subjects (Physics, Math, Chem/Vocational, Language, 5th subject) against the 75% central rule (65% for SC/ST/PwD) or 45% AP state rule.',
    category: 'PREPARATION',
    portal: 'OnePath Eligibility Checker',
  },
  {
    id: 'task-2',
    stepNumber: 2,
    title: 'Obtain Medical Fitness Certificate in Annexure-7 Format',
    description: 'Download the official Annexure-7 template from josaa.nic.in, visit a Registered Medical Practitioner (MBBS Doctor), and get physical fitness and vision certified with doctor stamp and registration number.',
    category: 'PREPARATION',
    portal: 'josaa.nic.in',
  },
  {
    id: 'task-3',
    stepNumber: 3,
    title: 'Validate OBC-NCL / EWS Certificate Financial Year (On or After April 1)',
    description: 'If claiming OBC-NCL (27%) or Gen-EWS (10%) quota, verify that your certificate issue date is strictly ON or AFTER April 1 of the current admission year in Central Government format.',
    category: 'PREPARATION',
    portal: 'MeeSeva / Revenue Portal',
  },
  {
    id: 'task-4',
    stepNumber: 4,
    title: 'Prepare Crossed Cancelled Cheque and Digitize Documents',
    description: 'Scan all certificates (Class 10, Class 12, Category, PwD, Medical) in high-resolution PDF format under 500 KB. Prepare a crossed cancelled cheque in candidate or parent name for refund verification.',
    category: 'PREPARATION',
    portal: 'Bank / Scanner',
  },
  {
    id: 'task-5',
    stepNumber: 5,
    title: 'Authenticate on Official Portal & Confirm Profile Details',
    description: 'Log into JoSAA (josaa.nic.in) or AP EAPCET (cets.apsche.ap.gov.in) using your application number and password. Double-check your State Code of Eligibility, Gender, and Category.',
    category: 'CHOICE_FILLING',
    portal: 'josaa.nic.in / cets.apsche.ap.gov.in',
  },
  {
    id: 'task-6',
    stepNumber: 6,
    title: 'Fill Unlimited College & Branch Choices in Priority Order',
    description: 'Fill all eligible college-program choices in STRICT order of your true personal preference. Put dream programs at top, realistic in middle, and solid backups below. Never arrange based on cutoffs.',
    category: 'CHOICE_FILLING',
    portal: 'Candidate Portal',
  },
  {
    id: 'task-7',
    stepNumber: 7,
    title: 'Review Mock Allotments & Lock Choices Before Deadline',
    description: 'Review Mock Seat Allocation 1 and Mock Seat Allocation 2. Adjust choice sequence if desired. Manually LOCK your choices before the announced deadline timestamp.',
    category: 'CHOICE_FILLING',
    portal: 'Candidate Portal',
  },
  {
    id: 'task-8',
    stepNumber: 8,
    title: 'Check Seat Allotment & Select Mandatory Willingness (Freeze/Float/Slide)',
    description: 'Upon round result declaration, log in immediately. If a seat is allotted, choose: FREEZE (accept and exit), FLOAT (accept and upgrade to any college), or SLIDE (accept and upgrade within same college).',
    category: 'ALLOTMENT',
    portal: 'Candidate Portal',
  },
  {
    id: 'task-9',
    stepNumber: 9,
    title: 'Pay Seat Acceptance Fee (SAF) & Upload Certificates',
    description: 'Pay Seat Acceptance Fee online (Rs 35,000 for Gen/OBC/EWS, Rs 15,000 for SC/ST/PwD). Upload required PDFs. WARNING: Failure to pay SAF cancels seat and debars you permanently.',
    category: 'ALLOTMENT',
    portal: 'Candidate Portal (Online Reporting)',
  },
  {
    id: 'task-10',
    stepNumber: 10,
    title: 'Monitor Dashboard for Verification Officer Queries (24-Hour SLA)',
    description: 'Virtual Reporting Centre (VRC) officers verify your documents online. Check your candidate portal twice daily. If a query is raised, respond and resolve within 24 hours.',
    category: 'REPORTING',
    portal: 'Candidate Portal',
  },
  {
    id: 'task-11',
    stepNumber: 11,
    title: 'Track Subsequent Rounds & Register for CSAB Special Rounds',
    description: 'Monitor seat upgrades across regular rounds. After Round 6, if seeking vacant seats in NITs, IIITs, or GFTIs, register on CSAB portal (csab.nic.in) using your JEE Main CRL rank.',
    category: 'REPORTING',
    portal: 'csab.nic.in',
  },
  {
    id: 'task-12',
    stepNumber: 12,
    title: 'Complete Final Physical Institutional Reporting & Campus Admission',
    description: 'Download the Final Allotment Letter. Pay remaining institutional tuition fees online, and report physically to the allotted campus with 3 sets of original certificates, photographs, and medical fitness form.',
    category: 'REPORTING',
    portal: 'Allotted Campus',
  },
];

export const JosaaCounsellingPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  // Route selector state
  const [selectedRoute, setSelectedRoute] = useState<'ALL' | 'JOSAA' | 'CSAB' | 'AP_EAPCET'>('ALL');

  // Interactive Eligibility Checker State
  const [exam, setExam] = useState<string>('JEE_MAIN');
  const [category, setCategory] = useState<string>('GEN');
  const [boardScore, setBoardScore] = useState<string>('ABOVE_75');
  const [homeState, setHomeState] = useState<string>('ANDHRA_PRADESH');
  const [gender, setGender] = useState<'NEUTRAL' | 'FEMALE'>('NEUTRAL');
  const [has5Subjects, setHas5Subjects] = useState<boolean>(true);
  const [rankInput, setRankInput] = useState<string>('');

  // Checklist state
  const [checkedDocs, setCheckedDocs] = useState<Record<string, boolean>>({});
  const [missingDocs, setMissingDocs] = useState<Record<string, boolean>>({});
  const [expandedDocInfo, setExpandedDocInfo] = useState<Record<string, boolean>>({});
  const [docCategoryFilter, setDocCategoryFilter] = useState<'ALL' | 'COMMON' | 'CATEGORY' | 'STATE'>('ALL');

  // Roadmap tasks state
  const [completedTasks, setCompletedTasks] = useState<Record<string, boolean>>({});

  // Active section scroll state
  const [activeTabSection, setActiveTabSection] = useState<string>('overview');

  // Load saved checklist from localStorage / backend
  useEffect(() => {
    // 1. Load local cache
    try {
      const savedDocs = localStorage.getItem('onepath_josaa_docs');
      if (savedDocs) setCheckedDocs(JSON.parse(savedDocs));

      const savedMissing = localStorage.getItem('onepath_josaa_missing');
      if (savedMissing) setMissingDocs(JSON.parse(savedMissing));

      const savedTasks = localStorage.getItem('onepath_josaa_tasks');
      if (savedTasks) setCompletedTasks(JSON.parse(savedTasks));
    } catch (e) {
      console.warn('Could not read from localStorage', e);
    }

    // 2. Load from backend if authenticated
    if (isAuthenticated) {
      checklistApi
        .getByTarget('SERVICE', 4) // Service ID 4 is JoSAA Counselling
        .then((items) => {
          if (Array.isArray(items) && items.length > 0) {
            const docMap: Record<string, boolean> = {};
            const missingMap: Record<string, boolean> = {};
            const taskMap: Record<string, boolean> = {};

            items.forEach((item) => {
              if (item.documentName.startsWith('TASK:')) {
                const taskId = item.documentName.replace('TASK:', '');
                taskMap[taskId] = item.completed;
              } else {
                docMap[item.documentName] = item.completed;
                if (item.isMissing) missingMap[item.documentName] = true;
              }
            });

            setCheckedDocs((prev) => ({ ...prev, ...docMap }));
            setMissingDocs((prev) => ({ ...prev, ...missingMap }));
            setCompletedTasks((prev) => ({ ...prev, ...taskMap }));
          }
        })
        .catch(() => {
          // Ignore backend fetch errors in case offline
        });
    }
  }, [isAuthenticated]);

  // Toggle document check
  const toggleDoc = async (docName: string) => {
    const nextState = !checkedDocs[docName];
    const updated = { ...checkedDocs, [docName]: nextState };
    setCheckedDocs(updated);
    try {
      localStorage.setItem('onepath_josaa_docs', JSON.stringify(updated));
    } catch (e) {}

    if (isAuthenticated) {
      try {
        await checklistApi.saveItem({
          targetType: 'SERVICE',
          targetId: 4,
          documentName: docName,
          completed: nextState,
          isMissing: Boolean(missingDocs[docName]),
          userNotes: 'JoSAA Document Checklist item',
        } as any);
      } catch (err) {}
    }
  };

  // Toggle missing doc
  const toggleMissing = async (docName: string) => {
    const nextState = !missingDocs[docName];
    const updated = { ...missingDocs, [docName]: nextState };
    setMissingDocs(updated);
    try {
      localStorage.setItem('onepath_josaa_missing', JSON.stringify(updated));
    } catch (e) {}

    if (isAuthenticated) {
      try {
        await checklistApi.saveItem({
          targetType: 'SERVICE',
          targetId: 4,
          documentName: docName,
          completed: Boolean(checkedDocs[docName]),
          isMissing: nextState,
          userNotes: 'JoSAA Document Checklist item',
        } as any);
      } catch (err) {}
    }
  };

  // Toggle roadmap task
  const toggleTask = async (taskId: string) => {
    const nextState = !completedTasks[taskId];
    const updated = { ...completedTasks, [taskId]: nextState };
    setCompletedTasks(updated);
    try {
      localStorage.setItem('onepath_josaa_tasks', JSON.stringify(updated));
    } catch (e) {}

    if (isAuthenticated) {
      try {
        await checklistApi.saveItem({
          targetType: 'SERVICE',
          targetId: 4,
          documentName: `TASK:${taskId}`,
          completed: nextState,
          isMissing: false,
          userNotes: 'JoSAA Counselling Roadmap step',
        } as any);
      } catch (err) {}
    }
  };

  // Calculated eligibility assessment
  const eligibilityAssessment = useMemo(() => {
    const isCentralReserved = category === 'SC' || category === 'ST' || category === 'PWD';
    const meetsCentralBoard = boardScore === 'ABOVE_75' || (isCentralReserved && boardScore === 'BETWEEN_65_75');
    const meetsStateBoard = boardScore !== 'BELOW_45';

    const canIit = exam === 'JEE_ADVANCED' && meetsCentralBoard && has5Subjects;
    const canNit = (exam === 'JEE_MAIN' || exam === 'JEE_ADVANCED' || exam === 'BOTH_JEE_AND_EAPCET') && meetsCentralBoard && has5Subjects;
    const canApState = (exam === 'AP_EAPCET' || exam === 'BOTH_JEE_AND_EAPCET' || homeState === 'ANDHRA_PRADESH') && meetsStateBoard;

    let quotaDescription = '';
    if (homeState === 'ANDHRA_PRADESH') {
      quotaDescription = '50% Home State Quota at NIT Andhra Pradesh (Tadepalligudem) + 50% All-India Other State Quota across 30 national NITs + 85% Local Area Quota (AU / SVU regions) in AP State Colleges.';
    } else if (homeState === 'TELANGANA') {
      quotaDescription = '50% Home State Quota at NIT Warangal + 50% Other State Quota across 30 national NITs + Local Area Quota in Telangana State Counselling.';
    } else {
      quotaDescription = '50% Home State Quota at your state respective NIT + 50% Other State Quota across all national NITs + All-India Open Seats in IITs and IIITs.';
    }

    return {
      canIit,
      canNit,
      canApState,
      meetsCentralBoard,
      meetsStateBoard,
      quotaDescription,
      hasFemaleSupernumerary: gender === 'FEMALE',
    };
  }, [exam, category, boardScore, homeState, gender, has5Subjects]);

  // Filtered documents
  const filteredDocs = useMemo(() => {
    return MASTER_DOCUMENTS.filter((doc) => {
      if (docCategoryFilter === 'ALL') return true;
      return doc.category === docCategoryFilter;
    });
  }, [docCategoryFilter]);

  const totalDocsCount = MASTER_DOCUMENTS.length;
  const completedDocsCount = Object.values(checkedDocs).filter(Boolean).length;
  const docsProgressPercent = Math.round((completedDocsCount / totalDocsCount) * 100);

  const totalTasksCount = ROADMAP_TASKS.length;
  const completedTasksCount = Object.values(completedTasks).filter(Boolean).length;
  const roadmapProgressPercent = Math.round((completedTasksCount / totalTasksCount) * 100);

  // Quick section scroll handler
  const scrollToSection = (id: string) => {
    setActiveTabSection(id);
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-12">
      {/* 1. BREADCRUMB & BACK */}
      <div className="flex items-center justify-between">
        <Link
          to="/services"
          className="inline-flex items-center text-xs sm:text-sm font-semibold text-slate-600 hover:text-slate-900 transition"
        >
          <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Services Directory
        </Link>
        <span className="text-xs font-semibold text-brand-700 bg-brand-50 border border-brand-100 px-3 py-1 rounded-full">
          Education & Engineering Admissions
        </span>
      </div>

      {/* 2. PAGE HERO BANNER */}
      <div
        id="overview"
        className="relative rounded-3xl bg-gradient-to-br from-slate-950 via-brand-950 to-indigo-950 p-6 sm:p-10 text-white shadow-xl overflow-hidden border border-slate-800"
      >
        <div className="absolute top-0 right-0 -mr-20 -mt-20 w-80 h-80 rounded-full bg-brand-500/10 blur-3xl pointer-events-none" />
        <div className="absolute bottom-0 left-1/3 -mb-24 w-96 h-96 rounded-full bg-indigo-500/10 blur-3xl pointer-events-none" />

        <div className="relative z-10 max-w-4xl space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-brand-500/20 border border-brand-400/30 text-brand-200 text-xs font-bold tracking-wide">
            <GraduationCap className="w-4 h-4 text-brand-300" />
            <span>Central & State Technical Admissions Architecture</span>
          </div>

          <h1 className="text-2xl sm:text-4xl lg:text-5xl font-black tracking-tight text-white leading-tight">
            JoSAA & State College Admissions Counselling
          </h1>

          <p className="text-slate-300 text-sm sm:text-base lg:text-lg leading-relaxed max-w-3xl">
            Unified step-by-step guidance for engineering admissions after JEE Main, JEE Advanced, and AP EAPCET.
            Master choice filling, dual eligibility verification, Freeze/Float/Slide decision rules, and document reporting.
          </p>

          {/* Quick Pill Badges */}
          <div className="pt-2 flex flex-wrap gap-2 text-xs text-slate-300 font-medium">
            <span className="inline-flex items-center px-3 py-1 rounded-xl bg-white/10 backdrop-blur-sm border border-white/10">
              🏛️ 23 IITs, 31 NITs, 26 IIITs, 38 GFTIs
            </span>
            <span className="inline-flex items-center px-3 py-1 rounded-xl bg-white/10 backdrop-blur-sm border border-white/10">
              🎓 Andhra Pradesh EAPCET (JNTU & AU)
            </span>
            <span className="inline-flex items-center px-3 py-1 rounded-xl bg-white/10 backdrop-blur-sm border border-white/10">
              ⚡ 6 JoSAA Rounds + 2 CSAB Special Rounds
            </span>
            <span className="inline-flex items-center px-3 py-1 rounded-xl bg-white/10 backdrop-blur-sm border border-white/10">
              ⚖️ 75% Board Rule & 20% Female Pool
            </span>
          </div>
        </div>
      </div>

      {/* 3. SECTION NAVIGATION PILLS */}
      <div className="sticky top-16 z-30 bg-white/95 backdrop-blur-md py-3 border-b border-slate-200 -mx-4 px-4 sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8">
        <div className="flex items-center space-x-2 overflow-x-auto scrollbar-none pb-1">
          {[
            { id: 'overview', label: 'Overview' },
            { id: 'counselling-routes', label: 'Counselling Routes' },
            { id: 'eligibility-checker', label: 'Eligibility Checker' },
            { id: 'step-guide', label: 'Step-by-Step Guide' },
            { id: 'documents-checklist', label: 'Documents Checklist' },
            { id: 'choice-guidance', label: 'College & Branch Guidance' },
            { id: 'important-dates', label: 'Important Dates' },
            { id: 'official-links', label: 'Official Links' },
            { id: 'counselling-roadmap', label: 'My Roadmap' },
          ].map((item) => (
            <button
              key={item.id}
              onClick={() => scrollToSection(item.id)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
                activeTabSection === item.id
                  ? 'bg-brand-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {/* ======================================================== */}
      {/* SECTION 2: CHOOSE YOUR COUNSELLING ROUTE */}
      {/* ======================================================== */}
      <section id="counselling-routes" className="space-y-6 pt-2">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
            <Compass className="w-3.5 h-3.5" /> Admission Portals & Institutional Scope
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            Choose Your Counselling Route
          </h2>
          <p className="text-xs sm:text-sm text-slate-600">
            Select an admission framework below to understand its participating institutes, seat allocation split, registration fees, and round structure.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Route 1: JoSAA Central */}
          <div
            className={`p-6 rounded-3xl border-2 transition-all cursor-pointer flex flex-col justify-between ${
              selectedRoute === 'JOSAA'
                ? 'border-brand-600 bg-brand-50/20 shadow-md ring-2 ring-brand-600/10'
                : 'border-slate-200 hover:border-slate-300 bg-white shadow-sm'
            }`}
            onClick={() => setSelectedRoute(selectedRoute === 'JOSAA' ? 'ALL' : 'JOSAA')}
          >
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-extrabold uppercase tracking-wider text-brand-700 bg-brand-100/60 px-2.5 py-0.5 rounded-full">
                  Central Authority
                </span>
                <span className="text-xs font-bold text-slate-500">6 Regular Rounds</span>
              </div>

              <div>
                <h3 className="text-lg font-bold text-slate-900">JoSAA Counselling</h3>
                <p className="text-xs text-brand-600 font-semibold mt-0.5">josaa.nic.in</p>
              </div>

              <div className="space-y-2 text-xs text-slate-600 leading-relaxed border-t border-slate-100 pt-3">
                <p>
                  <strong>Participating Institutes:</strong> 23 IITs, 31 NITs, IIEST Shibpur, 26 IIITs, and 38 Other-GFTIs (118 premier institutes).
                </p>
                <p>
                  <strong>Qualifying Exams:</strong> JEE (Advanced) for IITs; JEE (Main) Paper 1 for NITs, IIITs, and GFTIs.
                </p>
                <p>
                  <strong>Quota Split:</strong> 50% Home State (HS) / 50% Other State (OS) in NITs; All-India Open in IITs & IIITs.
                </p>
                <p>
                  <strong>Fees:</strong> Registration is Free. Seat Acceptance Fee (SAF): ₹35,000 (GEN/OBC/EWS) / ₹15,000 (SC/ST/PwD) — fully adjusted in tuition.
                </p>
              </div>
            </div>

            <div className="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-brand-700 font-bold">IIT & NIT+ System</span>
              <span className="text-slate-400">Click to filter</span>
            </div>
          </div>

          {/* Route 2: CSAB Special Rounds */}
          <div
            className={`p-6 rounded-3xl border-2 transition-all cursor-pointer flex flex-col justify-between ${
              selectedRoute === 'CSAB'
                ? 'border-brand-600 bg-brand-50/20 shadow-md ring-2 ring-brand-600/10'
                : 'border-slate-200 hover:border-slate-300 bg-white shadow-sm'
            }`}
            onClick={() => setSelectedRoute(selectedRoute === 'CSAB' ? 'ALL' : 'CSAB')}
          >
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-extrabold uppercase tracking-wider text-purple-700 bg-purple-100/60 px-2.5 py-0.5 rounded-full">
                  Vacant Seat Spot
                </span>
                <span className="text-xs font-bold text-slate-500">2 Special Rounds</span>
              </div>

              <div>
                <h3 className="text-lg font-bold text-slate-900">CSAB Special Rounds</h3>
                <p className="text-xs text-purple-600 font-semibold mt-0.5">csab.nic.in</p>
              </div>

              <div className="space-y-2 text-xs text-slate-600 leading-relaxed border-t border-slate-100 pt-3">
                <p>
                  <strong>Institutional Scope:</strong> Vacant/unfilled seats across NITs, IIITs, and GFTIs after the conclusion of JoSAA Round 6.
                </p>
                <p>
                  <strong>Qualifying Exam:</strong> JEE (Main) Paper 1 CRL Rank (IIT seats are NOT included in CSAB).
                </p>
                <p>
                  <strong>Strategic Benefit:</strong> Candidates holding a JoSAA seat can participate without surrendering their seat unless an upgrade is secured.
                </p>
                <p>
                  <strong>Fees:</strong> Special Round Participation Fee: ₹45,000 (GEN/OBC) / ₹25,000 (SC/ST/PwD), including ₹5,000 non-refundable charge.
                </p>
              </div>
            </div>

            <div className="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-purple-700 font-bold">Post-JoSAA Recovery</span>
              <span className="text-slate-400">Click to filter</span>
            </div>
          </div>

          {/* Route 3: AP State Counselling */}
          <div
            className={`p-6 rounded-3xl border-2 transition-all cursor-pointer flex flex-col justify-between ${
              selectedRoute === 'AP_EAPCET'
                ? 'border-brand-600 bg-brand-50/20 shadow-md ring-2 ring-brand-600/10'
                : 'border-slate-200 hover:border-slate-300 bg-white shadow-sm'
            }`}
            onClick={() => setSelectedRoute(selectedRoute === 'AP_EAPCET' ? 'ALL' : 'AP_EAPCET')}
          >
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-extrabold uppercase tracking-wider text-emerald-700 bg-emerald-100/60 px-2.5 py-0.5 rounded-full">
                  AP State Body
                </span>
                <span className="text-xs font-bold text-slate-500">3 Web Rounds</span>
              </div>

              <div>
                <h3 className="text-lg font-bold text-slate-900">AP EAPCET Counselling</h3>
                <p className="text-xs text-emerald-600 font-semibold mt-0.5">cets.apsche.ap.gov.in</p>
              </div>

              <div className="space-y-2 text-xs text-slate-600 leading-relaxed border-t border-slate-100 pt-3">
                <p>
                  <strong>Institutional Scope:</strong> JNTU Kakinada, JNTU Anantapur, Andhra University, SVU Tirupati, and private engineering colleges in AP.
                </p>
                <p>
                  <strong>Qualifying Exam:</strong> AP EAPCET / EAMCET state rank. (Also Category-B JEE Main seats).
                </p>
                <p>
                  <strong>Quota Split:</strong> 85% Local Area (Andhra University AU / Sri Venkateswara SVU regions) / 15% Unreserved.
                </p>
                <p>
                  <strong>Fees:</strong> Web Processing Fee: ₹1,200 (OC/BC) / ₹600 (SC/ST). Tuition fee varies by university/college tier.
                </p>
              </div>
            </div>

            <div className="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-emerald-700 font-bold">AP State Universities</span>
              <span className="text-slate-400">Click to filter</span>
            </div>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 3: ELIGIBILITY CHECKER */}
      {/* ======================================================== */}
      <section id="eligibility-checker" className="space-y-6 pt-2">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
            <Sparkles className="w-3.5 h-3.5" /> Interactive Evaluation
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            Eligibility Checker
          </h2>
          <p className="text-xs sm:text-sm text-slate-600">
            Candidates must satisfy dual qualifications: entrance exam score/rank and Class 12 aggregate board marks across 5 compulsory subjects.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Input Questionnaire Form */}
          <div className="lg:col-span-7 bg-white rounded-3xl border border-slate-200/90 p-6 sm:p-8 shadow-sm space-y-5">
            {/* Field 1: Exam Taken */}
            <div className="space-y-2">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">
                1. Qualifying Entrance Examination
              </label>
              <select
                value={exam}
                onChange={(e) => setExam(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="JEE_ADVANCED">JEE (Advanced) Qualified — Eligible for 23 IITs & NITs</option>
                <option value="JEE_MAIN">JEE (Main) Paper 1 Qualified — Eligible for 31 NITs, 26 IIITs, 38 GFTIs</option>
                <option value="AP_EAPCET">AP EAPCET Qualified — Eligible for AP State Engineering</option>
                <option value="BOTH_JEE_AND_EAPCET">Both JEE & AP EAPCET (Dual Track National + State)</option>
              </select>
            </div>

            {/* Field 2: Category */}
            <div className="space-y-2">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">
                2. Candidate Reservation Category
              </label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                {[
                  { id: 'GEN', label: 'General / Open' },
                  { id: 'GEN_EWS', label: 'General-EWS (10%)' },
                  { id: 'OBC_NCL', label: 'OBC-NCL (27%)' },
                  { id: 'SC', label: 'SC (15%)' },
                  { id: 'ST', label: 'ST (7.5%)' },
                  { id: 'PWD', label: 'PwD (5% Quota)' },
                ].map((item) => (
                  <button
                    type="button"
                    key={item.id}
                    onClick={() => setCategory(item.id)}
                    className={`py-2 px-3 text-xs font-bold rounded-xl border text-center transition ${
                      category === item.id
                        ? 'border-brand-600 bg-brand-50 text-brand-900 ring-2 ring-brand-600/10'
                        : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Field 3: Class 12 Board Score */}
            <div className="space-y-2">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">
                3. Class 12th Board Marks Aggregate
              </label>
              <select
                value={boardScore}
                onChange={(e) => setBoardScore(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="ABOVE_75">≥ 75% Aggregate in Class 12 (or Top 20 Percentile in Board)</option>
                <option value="BETWEEN_65_75">65.0% – 74.9% Aggregate (Meets SC/ST/PwD Central Cutoff)</option>
                <option value="BETWEEN_45_65">45.0% – 64.9% Aggregate (Meets AP State Engineering Cutoff)</option>
                <option value="BELOW_45">&lt; 45% Aggregate in Class 12</option>
              </select>
            </div>

            {/* Field 4: State Code of Eligibility */}
            <div className="space-y-2">
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">
                4. State Where You Passed Class 12 (State Code of Eligibility)
              </label>
              <select
                value={homeState}
                onChange={(e) => setHomeState(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="ANDHRA_PRADESH">Andhra Pradesh (Home State for NIT AP & 85% Local AP Quota)</option>
                <option value="TELANGANA">Telangana (Home State for NIT Warangal & TS Quota)</option>
                <option value="OTHER_STATE">Other Indian State / Union Territory</option>
              </select>
            </div>

            {/* Field 5: Gender & 5 Compulsory Subjects Toggle */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                  5. Gender Pool
                </label>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setGender('NEUTRAL')}
                    className={`flex-1 py-2 text-xs font-bold rounded-xl border ${
                      gender === 'NEUTRAL'
                        ? 'border-brand-600 bg-brand-50 text-brand-900'
                        : 'border-slate-200 text-slate-600'
                    }`}
                  >
                    Gender-Neutral
                  </button>
                  <button
                    type="button"
                    onClick={() => setGender('FEMALE')}
                    className={`flex-1 py-2 text-xs font-bold rounded-xl border ${
                      gender === 'FEMALE'
                        ? 'border-brand-600 bg-brand-50 text-brand-900'
                        : 'border-slate-200 text-slate-600'
                    }`}
                  >
                    Female (20% Pool)
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                  Compulsory 5 Subjects
                </label>
                <label className="flex items-center space-x-2 text-xs text-slate-700 font-medium p-2 rounded-xl bg-slate-50 border border-slate-200 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={has5Subjects}
                    onChange={(e) => setHas5Subjects(e.target.checked)}
                    className="rounded text-brand-600 focus:ring-brand-500 w-4 h-4"
                  />
                  <span>Passed: Physics, Math, Chem/Voc, Lang, 5th</span>
                </label>
              </div>
            </div>
          </div>

          {/* Assessment Output Card */}
          <div className="lg:col-span-5 bg-gradient-to-br from-slate-900 via-brand-950 to-slate-950 text-white rounded-3xl p-6 sm:p-8 shadow-xl flex flex-col justify-between space-y-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wider text-brand-300 bg-brand-500/20 border border-brand-400/30 px-3 py-1 rounded-full">
                  Eligibility Evaluation Result
                </span>
                <span className="text-xs text-slate-400">Preliminary Guidance</span>
              </div>

              <div className="space-y-3 pt-2">
                {/* IIT Eligibility */}
                <div className="flex items-start gap-3 p-3 rounded-2xl bg-white/5 border border-white/10">
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5 text-xs font-bold ${
                      eligibilityAssessment.canIit
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        : 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                    }`}
                  >
                    {eligibilityAssessment.canIit ? '✓' : '✕'}
                  </div>
                  <div className="text-xs leading-relaxed">
                    <p className="font-bold text-white">23 IITs (Indian Institutes of Technology)</p>
                    <p className="text-slate-300">
                      {eligibilityAssessment.canIit
                        ? 'Eligible: Qualified in JEE Advanced and satisfies 75% / Top 20 percentile rule.'
                        : 'Not Eligible: Requires JEE Advanced qualification and >= 75% Class 12 aggregate.'}
                    </p>
                  </div>
                </div>

                {/* NIT / IIIT Eligibility */}
                <div className="flex items-start gap-3 p-3 rounded-2xl bg-white/5 border border-white/10">
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5 text-xs font-bold ${
                      eligibilityAssessment.canNit
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        : 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                    }`}
                  >
                    {eligibilityAssessment.canNit ? '✓' : '✕'}
                  </div>
                  <div className="text-xs leading-relaxed">
                    <p className="font-bold text-white">31 NITs, 26 IIITs, IIEST & 38 GFTIs</p>
                    <p className="text-slate-300">
                      {eligibilityAssessment.canNit
                        ? 'Eligible: Valid JEE Main rank and satisfies Class 12 aggregate requirement.'
                        : 'Requires valid JEE Main rank and >= 75% in 5 compulsory subjects (65% for SC/ST/PwD).'}
                    </p>
                  </div>
                </div>

                {/* AP State Engineering Eligibility */}
                <div className="flex items-start gap-3 p-3 rounded-2xl bg-white/5 border border-white/10">
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5 text-xs font-bold ${
                      eligibilityAssessment.canApState
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        : 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                    }`}
                  >
                    {eligibilityAssessment.canApState ? '✓' : '✕'}
                  </div>
                  <div className="text-xs leading-relaxed">
                    <p className="font-bold text-white">Andhra Pradesh State Engineering (EAPCET)</p>
                    <p className="text-slate-300">
                      {eligibilityAssessment.canApState
                        ? 'Eligible for AP State Web Counselling for JNTUK, JNTUA, AU, SVU (85% Local Quota).'
                        : 'Requires AP EAPCET rank and minimum 45% aggregate in PCM (40% for reserved).'}
                    </p>
                  </div>
                </div>
              </div>

              {/* Quota Distribution Notice */}
              <div className="p-3.5 rounded-2xl bg-blue-950/60 border border-blue-500/30 text-xs text-blue-200 space-y-1">
                <p className="font-bold text-blue-100">Applicable Quota Split:</p>
                <p className="leading-relaxed">{eligibilityAssessment.quotaDescription}</p>
                {eligibilityAssessment.hasFemaleSupernumerary && (
                  <p className="text-emerald-300 font-semibold pt-1">
                    ★ Female-Only Supernumerary: Considered across both neutral and 20% female-exclusive quota!
                  </p>
                )}
              </div>
            </div>

            <div className="text-[11px] text-slate-400 leading-snug border-t border-white/10 pt-3">
              <span className="font-bold text-slate-300">Disclaimer:</span> This is preliminary guidance based on official regulations. Eligibility rules vary by counselling authority, institute, course, and admission year. Check the official portal for final admission criteria.
            </div>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 4: STEP-BY-STEP GUIDE */}
      {/* ======================================================== */}
      <section id="step-guide" className="space-y-6 pt-2">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
            <ListOrdered className="w-3.5 h-3.5" /> Multi-Round Workflow
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            Step-by-Step Counselling Guide
          </h2>
          <p className="text-xs sm:text-sm text-slate-600">
            Follow the complete operational lifecycle from portal registration to physical institutional joining.
          </p>
        </div>

        {/* Highlighted Warning Box */}
        <div className="p-4 sm:p-5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-950 flex items-start gap-3 shadow-sm">
          <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0 mt-0.5" />
          <div className="text-xs sm:text-sm leading-relaxed">
            <strong className="block font-extrabold text-rose-900 mb-0.5">
              CRITICAL COUNSELLING SYSTEM WARNING:
            </strong>
            If a seat is allotted in ANY JoSAA round and the candidate fails to complete online reporting (payment of Seat Acceptance Fee + uploading documents + selecting willingness) within the round deadline, the allotted seat is automatically <strong>CANCELLED</strong> and the candidate is <strong>PERMANENTLY DEBARRED</strong> from all subsequent JoSAA rounds.
          </div>
        </div>

        {/* Decision Choices Visual: FREEZE vs FLOAT vs SLIDE */}
        <div className="bg-white rounded-3xl border border-slate-200/90 p-6 sm:p-8 shadow-sm space-y-4">
          <div>
            <h3 className="text-base sm:text-lg font-bold text-slate-900">
              Seat Allotment Willingness Options: Freeze, Float, and Slide Explained
            </h3>
            <p className="text-xs sm:text-sm text-slate-600 mt-0.5">
              Whenever you receive an allotment, you MUST submit your willingness choice. Understand the exact differences:
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
            {/* Freeze */}
            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-2">
              <div className="inline-flex items-center space-x-1.5 px-2.5 py-0.5 rounded-full bg-slate-200 text-slate-800 text-xs font-bold">
                <Lock className="w-3 h-3 text-slate-700" />
                <span>FREEZE OPTION</span>
              </div>
              <h4 className="text-sm font-bold text-slate-900">Lock Allotted Seat & Exit</h4>
              <p className="text-xs text-slate-600 leading-relaxed">
                Candidate accepts the allotted seat and opts OUT of all subsequent rounds. No further choice upgrading occurs. You are confirmed for this seat.
              </p>
            </div>

            {/* Float */}
            <div className="p-4 rounded-2xl bg-blue-50/70 border border-blue-200 space-y-2">
              <div className="inline-flex items-center space-x-1.5 px-2.5 py-0.5 rounded-full bg-blue-200 text-blue-900 text-xs font-bold">
                <Sparkles className="w-3 h-3 text-blue-700" />
                <span>FLOAT OPTION</span>
              </div>
              <h4 className="text-sm font-bold text-blue-950">Upgrade to ANY Institute</h4>
              <p className="text-xs text-blue-900/80 leading-relaxed">
                Candidate accepts the allotted seat but wishes to upgrade to a higher-preference academic program at <strong>ANY institute</strong> in subsequent rounds. (Safest upgrade option).
              </p>
            </div>

            {/* Slide */}
            <div className="p-4 rounded-2xl bg-purple-50/70 border border-purple-200 space-y-2">
              <div className="inline-flex items-center space-x-1.5 px-2.5 py-0.5 rounded-full bg-purple-200 text-purple-900 text-xs font-bold">
                <Building2 className="w-3 h-3 text-purple-700" />
                <span>SLIDE OPTION</span>
              </div>
              <h4 className="text-sm font-bold text-purple-950">Upgrade WITHIN SAME Institute</h4>
              <p className="text-xs text-purple-900/80 leading-relaxed">
                Candidate accepts the allotted seat but wishes to upgrade to a higher-preference program <strong>WITHIN THE SAME institute</strong> only (e.g. Mechanical to CSE at NIT Trichy).
              </p>
            </div>
          </div>
        </div>

        {/* Detailed 8 Steps Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[
            {
              step: 1,
              title: 'Portal Authentication & Profile Verification',
              time: '15–20 Minutes',
              desc: 'Log into josaa.nic.in or cets.apsche.ap.gov.in using your application number and password. Verify state code of eligibility, gender, and category details.',
              tip: 'State code of eligibility is where you passed Class 12, not your permanent home address.',
            },
            {
              step: 2,
              title: 'Strategic Choice Filling & Ordering',
              time: '2–3 Days',
              desc: 'Fill college and branch preferences in STRICT order of your authentic personal preference. Choices are unlimited. Highest preferred program must be Choice #1.',
              tip: 'Golden Rule: Never place a lower-preferred branch above a higher-preferred branch because of cutoffs.',
            },
            {
              step: 3,
              title: 'Mock Allotment Review & Choice Locking',
              time: '1 Hour',
              desc: 'Review the two official Mock Allotment results published to understand indicative seat trends. Adjust sequence and manually LOCK before the official deadline.',
              tip: 'If not manually locked, system auto-locks your last saved preferences at the deadline.',
            },
            {
              step: 4,
              title: 'Seat Allotment Result & Willingness Selection',
              time: 'Same Day',
              desc: 'Check round results. If a seat is allotted, select FREEZE, FLOAT, or SLIDE. Selecting Float or Slide retains your current seat while seeking upgrades.',
              tip: 'Willingness selection is mandatory upon receiving any seat allotment.',
            },
            {
              step: 5,
              title: 'Seat Acceptance Fee (SAF) & Document Upload',
              time: '1–2 Hours',
              desc: 'Pay SAF online (₹35,000 for Gen/OBC/EWS, ₹15,000 for SC/ST/PwD). Upload clear PDF scans of Class 10, Class 12, Medical Certificate (Annexure-7), and Category proof.',
              tip: 'SAF is 100% adjusted toward your first-year institute tuition fee upon admission.',
            },
            {
              step: 6,
              title: 'Online Verification & Query Resolution (24h)',
              time: '24 Hours Max',
              desc: 'Virtual Reporting Centre (VRC) officers verify uploaded certificates. If a discrepancy query is raised on your dashboard, you must respond within 24 hours.',
              tip: 'Check your portal twice daily during reporting windows to avoid missing queries.',
            },
            {
              step: 7,
              title: 'Subsequent Rounds & CSAB Spot Rounds',
              time: '1–2 Weeks',
              desc: 'Monitor rounds 2 through 6 for automatic upgrades. After Round 6, participate in CSAB Special Rounds (csab.nic.in) for vacant seats across NITs, IIITs, and GFTIs.',
              tip: 'Seats secured in JoSAA can be retained while exploring upgrades in CSAB Special Rounds.',
            },
            {
              step: 8,
              title: 'Physical Reporting & Campus Admission',
              time: 'Designated Dates',
              desc: 'Download Final Provisional Seat Allotment Letter. Pay remaining institutional admission tuition fees online, and report physically to the campus with original documents.',
              tip: 'Carry 3 sets of self-attested photocopies, 6 passport photos, and original certificates.',
            },
          ].map((item) => (
            <div
              key={item.step}
              className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-2 flex flex-col justify-between"
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="w-7 h-7 rounded-lg bg-brand-50 text-brand-700 font-extrabold text-xs flex items-center justify-center border border-brand-100">
                    {item.step}
                  </span>
                  <span className="text-[11px] font-semibold text-slate-400 flex items-center gap-1">
                    <Clock className="w-3 h-3" /> {item.time}
                  </span>
                </div>
                <h4 className="text-sm font-bold text-slate-900">{item.title}</h4>
                <p className="text-xs text-slate-600 leading-relaxed">{item.desc}</p>
              </div>

              <div className="pt-2 text-[11px] text-brand-700 bg-brand-50/60 p-2 rounded-xl border border-brand-100">
                <strong>Tip:</strong> {item.tip}
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 5: DOCUMENTS CHECKLIST */}
      {/* ======================================================== */}
      <section id="documents-checklist" className="space-y-6 pt-2">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
              <FileCheck2 className="w-3.5 h-3.5" /> Document Verification Protocols
            </div>
            <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
              Comprehensive Documents Checklist
            </h2>
            <p className="text-xs sm:text-sm text-slate-600">
              Mark documents as you prepare them. When signed in, your progress is automatically saved to your OnePath student profile.
            </p>
          </div>

          {/* Progress Bar Badge */}
          <div className="p-3 bg-white rounded-2xl border border-slate-200 shadow-sm flex items-center gap-3 flex-shrink-0">
            <div className="text-right">
              <p className="text-[11px] text-slate-400 font-semibold">Document Readiness</p>
              <p className="text-sm font-extrabold text-brand-700">
                {completedDocsCount} of {totalDocsCount} Completed ({docsProgressPercent}%)
              </p>
            </div>
            <div className="w-12 h-12 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-700 font-bold text-xs">
              {docsProgressPercent}%
            </div>
          </div>
        </div>

        {/* Filter Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-1">
          {[
            { id: 'ALL', label: 'All Documents' },
            { id: 'COMMON', label: 'Mandatory Common' },
            { id: 'CATEGORY', label: 'Category & Quota Proofs' },
            { id: 'STATE', label: 'State & Institutional Proofs' },
          ].map((cat) => (
            <button
              key={cat.id}
              onClick={() => setDocCategoryFilter(cat.id as any)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition ${
                docCategoryFilter === cat.id
                  ? 'bg-brand-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {/* Document Cards List */}
        <div className="space-y-3">
          {filteredDocs.map((doc) => {
            const isCompleted = Boolean(checkedDocs[doc.name]);
            const isMissing = Boolean(missingDocs[doc.name]);
            const isExpanded = Boolean(expandedDocInfo[doc.name]);

            return (
              <div
                key={doc.id}
                className={`p-4 sm:p-5 rounded-2xl border transition-all ${
                  isCompleted
                    ? 'bg-emerald-50/30 border-emerald-200'
                    : isMissing
                    ? 'bg-rose-50/30 border-rose-200'
                    : 'bg-white border-slate-200/90 hover:border-slate-300 shadow-sm'
                }`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <button
                      type="button"
                      onClick={() => toggleDoc(doc.name)}
                      className={`w-6 h-6 rounded-lg border flex items-center justify-center flex-shrink-0 mt-0.5 transition ${
                        isCompleted
                          ? 'border-emerald-600 bg-emerald-600 text-white'
                          : 'border-slate-300 bg-white hover:border-brand-500'
                      }`}
                    >
                      {isCompleted && <Check className="w-4 h-4 stroke-[3]" />}
                    </button>

                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <h4
                          className={`text-sm font-bold ${
                            isCompleted ? 'text-emerald-950 line-through opacity-75' : 'text-slate-900'
                          }`}
                        >
                          {doc.name}
                        </h4>
                        {doc.mandatory ? (
                          <span className="text-[10px] font-bold text-rose-700 bg-rose-50 border border-rose-100 px-2 py-0.2 rounded-full">
                            Mandatory
                          </span>
                        ) : (
                          <span className="text-[10px] font-bold text-slate-600 bg-slate-100 px-2 py-0.2 rounded-full">
                            Conditional
                          </span>
                        )}
                        <span className="text-[10px] text-slate-400">{doc.formats}</span>
                      </div>

                      <p className="text-xs text-slate-600 leading-relaxed">{doc.description}</p>

                      {/* Strict Rule Notice if applicable */}
                      {doc.strictRule && (
                        <div className="p-2 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs flex items-center gap-1.5 mt-1">
                          <AlertCircle className="w-3.5 h-3.5 text-amber-600 flex-shrink-0" />
                          <span>{doc.strictRule}</span>
                        </div>
                      )}

                      {/* How to Obtain Accordion */}
                      {isExpanded && (
                        <div className="mt-2 p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-700 space-y-1">
                          <p className="font-bold text-slate-900">How to Obtain:</p>
                          <p className="leading-relaxed">{doc.howToObtain}</p>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 flex-shrink-0">
                    <button
                      type="button"
                      onClick={() =>
                        setExpandedDocInfo((prev) => ({ ...prev, [doc.name]: !prev[doc.name] }))
                      }
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 text-xs flex items-center gap-1"
                      title="How to obtain this document"
                    >
                      <Info className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      onClick={() => toggleMissing(doc.name)}
                      className={`px-2.5 py-1 rounded-lg text-[11px] font-bold border transition ${
                        isMissing
                          ? 'border-rose-400 bg-rose-100 text-rose-800'
                          : 'border-slate-200 text-slate-500 hover:bg-slate-100'
                      }`}
                    >
                      {isMissing ? 'Flagged Missing' : 'Mark Missing'}
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 6: COLLEGE & BRANCH CHOICE GUIDANCE */}
      {/* ======================================================== */}
      <section id="choice-guidance" className="space-y-6 pt-2">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
            <Building2 className="w-3.5 h-3.5" /> Choice Strategy & Reference Directory
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            College and Branch Choice Guidance
          </h2>
          <p className="text-xs sm:text-sm text-slate-600">
            Learn how to shortlist branches strategically. Review premier verified IITs, NITs, IIITs, and Andhra Pradesh state engineering universities.
          </p>
        </div>

        {/* Strategy Advisory Banner */}
        <div className="bg-slate-900 text-white rounded-3xl p-6 sm:p-8 space-y-4">
          <h3 className="text-base sm:text-lg font-bold text-white flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-brand-400" /> Golden Rules of Choice Filling
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs text-slate-300">
            <div className="p-3 rounded-2xl bg-white/5 border border-white/10 space-y-1">
              <strong className="text-white block">1. True Preference Order:</strong>
              The system scans your list from top (#1) to bottom. Always put the branch you love most at the very top.
            </div>
            <div className="p-3 rounded-2xl bg-white/5 border border-white/10 space-y-1">
              <strong className="text-white block">2. Unlimited Choices:</strong>
              There is no fee per choice. Fill 100+ choices including dream, moderate, and safe backup options.
            </div>
            <div className="p-3 rounded-2xl bg-white/5 border border-white/10 space-y-1">
              <strong className="text-white block">3. Historical Cutoffs:</strong>
              Previous-year cutoffs are strictly benchmark indicators. Cutoffs fluctuate based on candidate pool dynamics.
            </div>
            <div className="p-3 rounded-2xl bg-white/5 border border-white/10 space-y-1">
              <strong className="text-white block">4. Home State Advantage:</strong>
              NIT Andhra Pradesh reserves 50% seats for AP Class 12 candidates. Ranks close significantly higher in HS quota.
            </div>
          </div>
        </div>

        {/* Verified Directory of Institutes */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {VERIFIED_INSTITUTES.map((inst) => (
            <div
              key={inst.id}
              className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-sm hover:shadow-md transition flex flex-col justify-between space-y-4"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span
                    className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full border ${
                      inst.type === 'IIT'
                        ? 'text-amber-800 bg-amber-50 border-amber-200'
                        : inst.type === 'NIT'
                        ? 'text-blue-800 bg-blue-50 border-blue-200'
                        : inst.type === 'IIIT'
                        ? 'text-purple-800 bg-purple-50 border-purple-200'
                        : 'text-emerald-800 bg-emerald-50 border-emerald-200'
                    }`}
                  >
                    {inst.type === 'AP_STATE' ? 'AP State University' : inst.type}
                  </span>
                  <span className="text-xs text-slate-400 flex items-center gap-1">
                    <MapPin className="w-3 h-3" /> {inst.location}
                  </span>
                </div>

                <div>
                  <h4 className="text-base font-bold text-slate-900 leading-snug">{inst.name}</h4>
                  <p className="text-xs text-slate-500 mt-0.5">{inst.description}</p>
                </div>

                {/* Popular Branches */}
                <div className="space-y-1">
                  <p className="text-[11px] font-bold uppercase tracking-wider text-slate-400">
                    Flagship Programmes:
                  </p>
                  <div className="flex flex-wrap gap-1.5">
                    {inst.popularBranches.map((br) => (
                      <span
                        key={br}
                        className="text-[11px] font-medium text-slate-700 bg-slate-100 px-2 py-0.5 rounded-md"
                      >
                        {br}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Reference Note */}
                <div className="p-2.5 rounded-xl bg-slate-50 border border-slate-100 text-xs text-slate-600">
                  <p className="font-semibold text-slate-800">Historical Benchmark Note:</p>
                  <p className="text-[11px] text-slate-500 mt-0.5 leading-relaxed">{inst.referenceCutoffNote}</p>
                </div>
              </div>

              {/* Action Links */}
              <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                <a
                  href={inst.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center text-brand-600 hover:text-brand-800 font-bold"
                >
                  Official Website <ExternalLink className="w-3 h-3 ml-1" />
                </a>

                <a
                  href={inst.cutoffUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center text-slate-700 hover:text-slate-900 font-bold bg-slate-100 px-3 py-1.5 rounded-lg"
                >
                  Official Cutoff Portal <ExternalLink className="w-3 h-3 ml-1" />
                </a>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 7: IMPORTANT DATES & DEADLINES */}
      {/* ======================================================== */}
      <section id="important-dates" className="space-y-6 pt-2">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
            <Calendar className="w-3.5 h-3.5" /> Verified Timelines & Milestones
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            Important Dates & Counselling Milestones
          </h2>
          <p className="text-xs sm:text-sm text-slate-600">
            Every date indicates the relevant counselling authority and admission cycle. Tentative schedule reference for the upcoming admission session.
          </p>
        </div>

        {/* Milestone Schedule Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            {
              phase: 'Phase 1: Registration',
              title: 'Portal Opens & Choice Filling',
              timing: 'Mid June (Post-JEE Advanced)',
              authority: 'JoSAA / NTA',
              desc: 'Online registration begins immediately following JEE Advanced result announcement. Unlimited choice filling window opens.',
            },
            {
              phase: 'Phase 2: Mock Allotments',
              title: 'Mock Seat Allocations 1 & 2',
              timing: 'Day 5 & Day 7 of Choice Filling',
              authority: 'JoSAA Authority',
              desc: 'Indicative mock results published to guide students on choices. Final day choice locking closes at 5:00 PM.',
            },
            {
              phase: 'Phase 3: Regular Rounds',
              title: 'JoSAA Rounds 1 to 6 Allotments',
              timing: 'Late June – Late July',
              authority: 'Joint Seat Allocation',
              desc: '6 sequential allocation rounds. Online reporting, SAF payment, and query response active for each round.',
            },
            {
              phase: 'Phase 4: Special Rounds',
              title: 'CSAB Special Rounds & AP Web Rounds',
              timing: 'Early August',
              authority: 'CSAB & APSCHE',
              desc: 'Spot allocation for vacant seats in NITs/IIITs. AP EAPCET web counselling rounds active concurrently for state seats.',
            },
          ].map((item, idx) => (
            <div
              key={idx}
              className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-2 flex flex-col justify-between"
            >
              <div className="space-y-1.5">
                <span className="text-[10px] font-extrabold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-full border border-brand-100">
                  {item.phase}
                </span>
                <h4 className="text-sm font-bold text-slate-900 pt-1">{item.title}</h4>
                <p className="text-xs font-semibold text-brand-600">{item.timing}</p>
                <p className="text-xs text-slate-500 leading-relaxed pt-1">{item.desc}</p>
              </div>

              <div className="text-[11px] text-slate-400 border-t border-slate-100 pt-2 font-medium">
                Authority: {item.authority}
              </div>
            </div>
          ))}
        </div>

        {/* Schedule Release Disclaimer */}
        <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs text-slate-600">
          <div>
            <strong className="text-slate-900 block font-bold">
              Official Schedule Status for 2026 Admission Cycle:
            </strong>
            <span>
              Counselling dates are notified officially by JoSAA, CSAB, and APSCHE post-examination results. Always check the official portal announcements.
            </span>
          </div>

          <a
            href="https://josaa.nic.in"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center text-xs font-bold text-white bg-slate-900 hover:bg-black px-4 py-2 rounded-xl transition flex-shrink-0 shadow-sm"
          >
            Check Official JoSAA Schedule <ExternalLink className="w-3.5 h-3.5 ml-1.5" />
          </a>
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 8: OFFICIAL LINKS */}
      {/* ======================================================== */}
      <section id="official-links" className="space-y-6 pt-2">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
            <ExternalLink className="w-3.5 h-3.5" /> Direct Regulatory Access
          </div>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            Official Portals & Regulatory Links
          </h2>
          <p className="text-xs sm:text-sm text-slate-600">
            Direct authenticated links to official government and counselling authority portals. Links open securely in a new browser tab.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            {
              name: 'JoSAA Official Portal',
              url: 'https://josaa.nic.in',
              domain: 'josaa.nic.in',
              badge: 'Central Authority',
              desc: 'Joint Seat Allocation Authority for 23 IITs, 31 NITs, 26 IIITs, and 38 GFTIs.',
            },
            {
              name: 'CSAB Special Rounds',
              url: 'https://csab.nic.in',
              domain: 'csab.nic.in',
              badge: 'Special Rounds',
              desc: 'Central Seat Allocation Board filling vacant seats across NIT+ system post-JoSAA.',
            },
            {
              name: 'AP EAPCET (APSCHE)',
              url: 'https://cets.apsche.ap.gov.in',
              domain: 'cets.apsche.ap.gov.in',
              badge: 'Andhra Pradesh',
              desc: 'Andhra Pradesh State Council of Higher Education technical web counselling.',
            },
            {
              name: 'JEE (Advanced) Official',
              url: 'https://jeeadv.ac.in',
              domain: 'jeeadv.ac.in',
              badge: 'IIT Entrance',
              desc: 'Organizing IIT entrance portal with syllabus, answer keys, and rank results.',
            },
            {
              name: 'National Testing Agency (NTA)',
              url: 'https://www.nta.ac.in',
              domain: 'nta.ac.in',
              badge: 'NTA Portal',
              desc: 'Official conducting agency for JEE Main, publishing scorecards and CRL percentiles.',
            },
            {
              name: 'AP State Council (APSCHE)',
              url: 'https://apsche.ap.gov.in',
              domain: 'apsche.ap.gov.in',
              badge: 'State Higher Ed',
              desc: 'Apex statutory body overseeing collegiate technical education in Andhra Pradesh.',
            },
            {
              name: 'DigiLocker Marksheets',
              url: 'https://www.digilocker.gov.in',
              domain: 'digilocker.gov.in',
              badge: 'Digital Proofs',
              desc: 'Ministry of Electronics & IT portal for legally valid Class 10 & 12 digital marksheets.',
            },
            {
              name: 'Swavlamban PwD Portal',
              url: 'https://www.swavlambancard.gov.in',
              domain: 'swavlambancard.gov.in',
              badge: 'UDID Card',
              desc: 'Department of Empowerment of Persons with Disabilities UDID card issuance portal.',
            },
          ].map((item, idx) => (
            <a
              key={idx}
              href={item.url}
              target="_blank"
              rel="noopener noreferrer"
              className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm hover:shadow-md hover:border-brand-300 transition flex flex-col justify-between space-y-3 group"
            >
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-extrabold uppercase tracking-wider text-brand-700 bg-brand-50 px-2 py-0.5 rounded-md border border-brand-100">
                    {item.badge}
                  </span>
                  <ExternalLink className="w-3.5 h-3.5 text-slate-400 group-hover:text-brand-600 transition" />
                </div>
                <h4 className="text-sm font-bold text-slate-900 group-hover:text-brand-700 transition">
                  {item.name}
                </h4>
                <p className="text-[11px] text-slate-500 leading-relaxed">{item.desc}</p>
              </div>

              <div className="text-xs font-semibold text-brand-600 truncate border-t border-slate-100 pt-2">
                {item.domain}
              </div>
            </a>
          ))}
        </div>
      </section>

      {/* ======================================================== */}
      {/* SECTION 9: MY COUNSELLING ROADMAP */}
      {/* ======================================================== */}
      <section id="counselling-roadmap" className="space-y-6 pt-2 pb-12">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded-md border border-brand-100">
              <Award className="w-3.5 h-3.5" /> Personalized Progress Tracker
            </div>
            <h2 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
              My Counselling Roadmap
            </h2>
            <p className="text-xs sm:text-sm text-slate-600">
              Track your individual progress through the 12 critical milestones of centralized and state admissions.
            </p>
          </div>

          {/* Progress Tracker Card */}
          <div className="p-3.5 bg-brand-50 rounded-2xl border border-brand-200 shadow-sm flex items-center gap-4 flex-shrink-0">
            <div className="text-right">
              <p className="text-[11px] text-brand-700 font-semibold">Roadmap Readiness</p>
              <p className="text-sm font-extrabold text-brand-950">
                {completedTasksCount} of {totalTasksCount} Milestones ({roadmapProgressPercent}%)
              </p>
            </div>
            <div className="w-12 h-12 rounded-xl bg-brand-600 text-white flex items-center justify-center font-bold text-xs shadow-md shadow-brand-500/20">
              {roadmapProgressPercent}%
            </div>
          </div>
        </div>

        {/* Roadmap Tasks List */}
        <div className="space-y-3">
          {ROADMAP_TASKS.map((task) => {
            const isCompleted = Boolean(completedTasks[task.id]);

            return (
              <div
                key={task.id}
                className={`p-4 sm:p-5 rounded-2xl border transition-all ${
                  isCompleted
                    ? 'bg-emerald-50/40 border-emerald-200'
                    : 'bg-white border-slate-200/90 hover:border-slate-300 shadow-sm'
                }`}
              >
                <div className="flex items-start gap-3">
                  <button
                    type="button"
                    onClick={() => toggleTask(task.id)}
                    className={`w-6 h-6 rounded-lg border flex items-center justify-center flex-shrink-0 mt-0.5 transition ${
                      isCompleted
                        ? 'border-emerald-600 bg-emerald-600 text-white'
                        : 'border-slate-300 bg-white hover:border-brand-500'
                    }`}
                  >
                    {isCompleted && <Check className="w-4 h-4 stroke-[3]" />}
                  </button>

                  <div className="space-y-1 flex-1">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <span className="w-5 h-5 rounded-md bg-slate-100 text-slate-700 text-[11px] font-bold flex items-center justify-center">
                          {task.stepNumber}
                        </span>
                        <h4
                          className={`text-sm font-bold ${
                            isCompleted ? 'text-emerald-950 line-through opacity-75' : 'text-slate-900'
                          }`}
                        >
                          {task.title}
                        </h4>
                      </div>

                      <span className="text-[10px] font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
                        {task.portal}
                      </span>
                    </div>

                    <p className="text-xs text-slate-600 leading-relaxed pl-7">{task.description}</p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
};

export default JosaaCounsellingPage;
