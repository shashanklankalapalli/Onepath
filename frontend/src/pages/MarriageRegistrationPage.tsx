import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { checklistApi } from '../services/api';
import {
  Heart,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  FileText,
  Clock,
  Banknote,
  Users,
  Building2,
  Calendar,
  ExternalLink,
  ChevronDown,
  ChevronUp,
  HelpCircle,
  Check,
  X,
  Sparkles,
  ArrowRight,
  Info,
  Scale,
  Camera,
  MapPin,
  FileCheck2,
  FileWarning,
  Scroll,
} from 'lucide-react';

interface DocumentItem {
  id: string;
  name: string;
  category: 'COUPLE' | 'WITNESSES' | 'SPECIAL';
  mandatory: boolean;
  acceptedFormats: string;
  description: string;
  howToObtain: string;
  legalImportance: string;
}

const MARRIAGE_DOCUMENTS: DocumentItem[] = [
  {
    id: 'app_form_memo',
    name: 'Application Form / Memorandum of Marriage',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: 'Duly filled & signed physical printout',
    description: 'Statutory memorandum of marriage prescribed under the relevant Act, signed by both husband and wife with passport photos affixed.',
    howToObtain: 'Generated automatically from the AP Registration & Stamps (IGRS AP) or MeeSeva portal after submitting online details. Take a clean printout.',
    legalImportance: 'Official statutory entry document required by the Sub-Registrar to open the register.',
  },
  {
    id: 'groom_age_proof',
    name: 'Groom Statutory Age Proof (Minimum 21 Years)',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: 'Birth Certificate / SSC (10th) Marksheet / Valid Passport',
    description: 'Statutory documentary proof confirming that the husband completed 21 years of age on or before the date of marriage solemnization.',
    howToObtain: 'Birth certificate issued by Municipal Corporation/Panchayat, or Class 10 Matriculation certificate from recognized board.',
    legalImportance: 'Under Section 5(iii) HMA and Section 4(c) SMA, marriage below 21 for groom is a statutory violation.',
  },
  {
    id: 'bride_age_proof',
    name: 'Bride Statutory Age Proof (Minimum 18 Years)',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: 'Birth Certificate / SSC (10th) Marksheet / Valid Passport',
    description: 'Statutory documentary proof confirming that the wife completed 18 years of age on or before the date of marriage solemnization.',
    howToObtain: 'Birth certificate issued by Municipal Corporation/Panchayat, or Class 10 Matriculation certificate from recognized board.',
    legalImportance: 'Under prevailing statutory law, marriage below 18 for bride is illegal and cannot be registered.',
  },
  {
    id: 'residence_proof_jurisdiction',
    name: 'Jurisdictional Residence Proof (> 30 Days)',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: 'Aadhaar Card / Voter ID / Gas or Electricity Bill / Registered Lease',
    description: 'Documentary evidence showing that either the husband or wife has resided within the jurisdiction of the Sub-Registrar Office for at least 30 days.',
    howToObtain: 'Download e-Aadhaar from UIDAI, electricity bill from AP DISCOM (CPDCL/EPDCL/SPDCL), or registered rental agreement.',
    legalImportance: 'Statutory prerequisite: Sub-Registrars only possess jurisdiction over persons resident in their circle for > 30 days or where ceremony occurred.',
  },
  {
    id: 'wedding_card_proof',
    name: 'Wedding Invitation Card / Religious Solemnization Proof',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: 'Original Printed Wedding Card or Temple/Church/Gurudwara Certificate',
    description: 'Original physical invitation card or certificate from religious institution (Temple Trust, Church, or Gurudwara) proving solemnized ceremony.',
    howToObtain: 'Original physical copy of wedding card or priest solemnization receipt bearing date, venue, and names of couple.',
    legalImportance: 'Required under Hindu Marriage Act to establish that religious ceremonies (Saptapadi, etc.) took place.',
  },
  {
    id: 'joint_ritual_photos',
    name: 'Joint Passport Photos & Wedding Ritual Photographs',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: '6 Passport Photos + 2 Ceremony Prints (4x6 inch)',
    description: '4 to 6 joint passport-size color photos of husband and wife + 2 distinct ceremony prints showing essential rituals (garland exchange, mangalsutra/saptapadi).',
    howToObtain: 'Professional photo studio prints. Ceremony photos must clearly show faces of bride and groom in wedding attire performing rites.',
    legalImportance: 'Pasted into the official physical marriage register and scanned into the state database.',
  },
  {
    id: 'notarized_affidavits',
    name: 'Sworn Notarized Affidavits of Marital Status & Nationality',
    category: 'COUPLE',
    mandatory: true,
    acceptedFormats: 'Original Notarized Affidavits on Non-Judicial Stamp Paper',
    description: 'Separate sworn affidavits by bride and groom affirming date/place of marriage, DOB, marital status at time of wedding, Indian nationality, and non-prohibited relationship.',
    howToObtain: 'Draft on ₹20/₹50 Non-Judicial Stamp Paper from court notary or MeeSeva centre and attest before a Notary Public.',
    legalImportance: 'Legal self-declaration; false declarations incur criminal perjury prosecution.',
  },
  {
    id: 'witnesses_id_presence',
    name: 'Witness Identity Proofs & Physical Attendance (2 for HMA, 3 for SMA)',
    category: 'WITNESSES',
    mandatory: true,
    acceptedFormats: 'Original Aadhaar Card & PAN Card + Self-Attested Photocopies',
    description: 'Adult witnesses who attended the ceremony (2 under Hindu Marriage Act; 3 under Special Marriage Act). Witnesses MUST be physically present before SRO.',
    howToObtain: 'Witnesses must carry their original government photo ID (Aadhaar, Passport, Voter ID) and 2 passport-size photographs.',
    legalImportance: 'Witnesses legally vouch for the authenticity of the solemnization and sign the state register.',
  },
  {
    id: 'divorce_decree_absolute',
    name: 'Certified Copy of Divorce Decree Absolute (For Divorced Applicants)',
    category: 'SPECIAL',
    mandatory: false,
    acceptedFormats: 'Certified Court Copy with Seal and Watermark',
    description: 'Final judgment and decree absolute issued by a competent Family Court dissolving the previous marriage.',
    howToObtain: 'Obtain certified true copy from the filing registry of the Family Court that granted the decree.',
    legalImportance: 'Mandatory proof that prior marriage is legally dissolved and no bigamy exists. Interim orders or mutual agreements are NOT accepted.',
  },
  {
    id: 'death_certificate_spouse',
    name: 'Official Death Certificate of Former Spouse (For Widowed Applicants)',
    category: 'SPECIAL',
    mandatory: false,
    acceptedFormats: 'Certified Copy from Registrar of Births and Deaths',
    description: 'Government death certificate confirming the passing of the previous husband or wife.',
    howToObtain: 'Issued by local Municipal Corporation, Municipality, or Gram Panchayat under the Registration of Births & Deaths Act.',
    legalImportance: 'Proof that former marital bond ended by death, permitting lawful remarriage.',
  },
  {
    id: 'foreign_embassy_noc',
    name: 'Embassy NOC / Single Status Certificate (For Foreign Spouse / NRI)',
    category: 'SPECIAL',
    mandatory: false,
    acceptedFormats: 'Official Embassy Seal / Apostilled Certificate + Valid Passport & Visa',
    description: 'Certificate of No Impediment / Single Status Certificate issued by the foreign embassy/consulate in India confirming capacity to marry.',
    howToObtain: 'Apply through the embassy of the foreign country located in New Delhi or regional consulate.',
    legalImportance: 'Required under Special Marriage Act when one party is a non-citizen.',
  },
];

interface RoadmapTask {
  id: string;
  stepNumber: number;
  title: string;
  description: string;
  timeline: string;
  actScope: 'ALL' | 'HMA' | 'SMA';
}

const ROADMAP_TASKS: RoadmapTask[] = [
  {
    id: 'task_1',
    stepNumber: 1,
    title: 'Verify Statutory Age & Mutual Consent',
    description: 'Confirm Groom has completed 21 years and Bride has completed 18 years of age. Ensure both parties give free, voluntary consent without coercion.',
    timeline: 'Day 1',
    actScope: 'ALL',
  },
  {
    id: 'task_2',
    stepNumber: 2,
    title: 'Determine Applicable Legal Framework (HMA vs SMA)',
    description: 'If marriage is already solemnized under Hindu/Sikh rites, choose Hindu Marriage Act (no notice). If seeking civil/court marriage, choose Special Marriage Act (30-day notice).',
    timeline: 'Day 1',
    actScope: 'ALL',
  },
  {
    id: 'task_3',
    stepNumber: 3,
    title: 'Identify Jurisdictional Sub-Registrar Office (SRO)',
    description: 'Locate the exact SRO in Andhra Pradesh where husband or wife has resided for over 30 days, or where the wedding ceremony occurred (use registration.ap.gov.in).',
    timeline: 'Day 2',
    actScope: 'ALL',
  },
  {
    id: 'task_4',
    stepNumber: 4,
    title: 'Draft & Notarize Statutory Affidavits',
    description: 'Prepare individual sworn affidavits declaring marital status, age, nationality, and absence of prohibited relationship on stamp paper before a Notary Public.',
    timeline: 'Day 3',
    actScope: 'ALL',
  },
  {
    id: 'task_5',
    stepNumber: 5,
    title: 'Collect Wedding Invitation & Ritual Ceremony Photographs',
    description: 'Assemble original printed wedding invitation card and at least 2 clear ceremony photos (garland exchange, mangalsutra/saptapadi) showing couple clearly.',
    timeline: 'Day 4',
    actScope: 'HMA',
  },
  {
    id: 'task_6',
    stepNumber: 6,
    title: 'File Notice of Intended Marriage (SMA Only)',
    description: 'For civil marriages under Special Marriage Act, submit Form-II notice to SRO and pay ₹150 notice fee. SRO displays 30-day notice on public board.',
    timeline: 'Day 5 (30-Day Window)',
    actScope: 'SMA',
  },
  {
    id: 'task_7',
    stepNumber: 7,
    title: 'Coordinate 2 (HMA) or 3 (SMA) Adult Witnesses',
    description: 'Confirm attendance of credible adult witnesses who personally saw the marriage. Collect their original Aadhaar Card, PAN Card, and passport photos.',
    timeline: 'Before SRO Visit',
    actScope: 'ALL',
  },
  {
    id: 'task_8',
    stepNumber: 8,
    title: 'Submit Online Application on IGRS AP / MeeSeva',
    description: 'Fill Memorandum of Marriage online via registration.ap.gov.in, upload scanned certificates, pay statutory fee (~₹100-₹200), and book appointment slot.',
    timeline: '1-2 Days Prior',
    actScope: 'ALL',
  },
  {
    id: 'task_9',
    stepNumber: 9,
    title: 'In-Person Appearance at Sub-Registrar Office',
    description: 'Husband, Wife, and mandatory witnesses appear with original documents. Officials verify documents and capture biometrics (fingerprints & webcam photos).',
    timeline: 'Appointed Date',
    actScope: 'ALL',
  },
  {
    id: 'task_10',
    stepNumber: 10,
    title: 'Receive Certificate & Verify DigiLocker Push',
    description: 'Sub-Registrar registers the marriage in the official register and issues the digitally signed QR-coded certificate. Download digital copy and verify DigiLocker sync.',
    timeline: 'Same Day / 7 Days',
    actScope: 'ALL',
  },
];

export const MarriageRegistrationPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  // Selected Registration Pathway
  const [selectedPathway, setSelectedPathway] = useState<'HMA' | 'SMA' | 'ANAND' | 'GUIDE'>('HMA');

  // Questionnaire state
  const [qCeremonyStatus, setQCeremonyStatus] = useState<string>('ALREADY_SOLEMNIZED');
  const [qApplicableAct, setQApplicableAct] = useState<string>('HMA_1955');
  const [qLegalAge, setQLegalAge] = useState<string>('LEGAL_AGE_MET');
  const [qResidenceState, setQResidenceState] = useState<string>('ANDHRA_PRADESH');
  const [qMaritalStatus, setQMaritalStatus] = useState<string>('BOTH_UNMARRIED');
  const [qSpecialCircumstances, setQSpecialCircumstances] = useState<string>('NONE');
  const [evalSubmitted, setEvalSubmitted] = useState<boolean>(false);

  // Document Checklist state
  const [docFilter, setDocFilter] = useState<'ALL' | 'COUPLE' | 'WITNESSES' | 'SPECIAL'>('ALL');
  const [completedDocs, setCompletedDocs] = useState<Record<string, boolean>>({});
  const [missingDocs, setMissingDocs] = useState<Record<string, boolean>>({});
  const [expandedDocs, setExpandedDocs] = useState<Record<string, boolean>>({});

  // Roadmap task state
  const [completedTasks, setCompletedTasks] = useState<Record<string, boolean>>({});

  // Load persistence from backend or localStorage
  useEffect(() => {
    const savedDocs = localStorage.getItem('onepath_marriage_docs');
    if (savedDocs) {
      try {
        setCompletedDocs(JSON.parse(savedDocs));
      } catch (e) {}
    }

    const savedTasks = localStorage.getItem('onepath_marriage_tasks');
    if (savedTasks) {
      try {
        setCompletedTasks(JSON.parse(savedTasks));
      } catch (e) {}
    }

    if (isAuthenticated) {
      checklistApi.getByTarget('SERVICE', 5)
        .then((items) => {
          if (items && items.length > 0) {
            const map: Record<string, boolean> = {};
            items.forEach((it) => {
              map[it.documentName] = it.completed;
            });
            setCompletedDocs((prev) => ({ ...prev, ...map }));
          }
        })
        .catch(() => {});
    }
  }, [isAuthenticated]);

  const toggleDocComplete = async (docId: string, docName: string) => {
    const nextVal = !completedDocs[docId];
    const updated = { ...completedDocs, [docId]: nextVal };
    setCompletedDocs(updated);
    localStorage.setItem('onepath_marriage_docs', JSON.stringify(updated));

    if (isAuthenticated) {
      try {
        await checklistApi.saveItem({
          targetType: 'SERVICE',
          targetId: 5,
          documentName: docName,
          completed: nextVal,
          isMissing: !!missingDocs[docId],
        });
      } catch (e) {}
    }
  };

  const toggleDocMissing = (docId: string) => {
    setMissingDocs((prev) => ({ ...prev, [docId]: !prev[docId] }));
  };

  const toggleDocExpand = (docId: string) => {
    setExpandedDocs((prev) => ({ ...prev, [docId]: !prev[docId] }));
  };

  const toggleTaskComplete = (taskId: string) => {
    const nextVal = !completedTasks[taskId];
    const updated = { ...completedTasks, [taskId]: nextVal };
    setCompletedTasks(updated);
    localStorage.setItem('onepath_marriage_tasks', JSON.stringify(updated));
  };

  const filteredDocs = MARRIAGE_DOCUMENTS.filter((doc) => {
    if (docFilter === 'ALL') return true;
    return doc.category === docFilter;
  });

  const completedCount = Object.values(completedDocs).filter(Boolean).length;
  const docProgressPercent = Math.round((completedCount / MARRIAGE_DOCUMENTS.length) * 100);

  const completedTasksCount = Object.values(completedTasks).filter(Boolean).length;
  const taskProgressPercent = Math.round((completedTasksCount / ROADMAP_TASKS.length) * 100);

  // Preliminary evaluation calculation
  const isUnderage = qLegalAge === 'UNDERAGE_ALERT';
  const isSeparatedWithoutDecree = qMaritalStatus === 'SEPARATED_WITHOUT_DECREE';
  const isSmaRecommended =
    qCeremonyStatus === 'INTENDED_FUTURE' ||
    qApplicableAct === 'SMA_1954' ||
    qSpecialCircumstances === 'INTER_FAITH';

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 pb-20">
      {/* 1. HERO & OVERVIEW */}
      <section className="bg-gradient-to-br from-rose-950 via-slate-900 to-indigo-950 text-white pt-10 pb-16 px-4 sm:px-6 lg:px-8 border-b border-rose-900/40">
        <div className="max-w-7xl mx-auto">
          {/* Breadcrumb */}
          <div className="flex items-center gap-2 text-xs font-medium text-rose-300/80 mb-6">
            <Link to="/services" className="hover:text-rose-100 transition">Services Directory</Link>
            <span>/</span>
            <span className="text-white">Marriage Registration & Certification</span>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            <div className="lg:col-span-8 space-y-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-rose-500/20 text-rose-300 border border-rose-500/30">
                <Heart className="w-3.5 h-3.5 fill-rose-400 text-rose-400" />
                Statutory Civic & Legal Service • Andhra Pradesh Priority
              </div>

              <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-white leading-tight">
                Marriage Registration & Certification
              </h1>

              <p className="text-base sm:text-lg text-slate-300 leading-relaxed max-w-3xl">
                Official statutory guidance for couples registering under the <span className="text-rose-300 font-semibold">Hindu Marriage Act, 1955</span> (already solemnized religious ceremony) or the <span className="text-indigo-300 font-semibold">Special Marriage Act, 1954</span> (civil/court marriage with mandatory 30-day notice). Complete with eligibility checking, document verification, witness requirements, and SRO appointment booking in Andhra Pradesh.
              </p>

              {/* Compulsory Registration Banner */}
              <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-200 text-xs sm:text-sm flex items-start gap-3">
                <Scale className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
                <div>
                  <span className="font-bold text-rose-100">Compulsory Marriage Registration Mandate: </span>
                  Pursuant to the Supreme Court of India ruling in <span className="italic font-medium text-white">Seema v. Ashwani Kumar (2006)</span>, marriage registration is legally compulsory in all Indian states regardless of religion. A valid certificate is required for passport spouse-name endorsement, international visas, joint property rights, insurance nomination, and legal security.
                </div>
              </div>

              {/* Core Badges */}
              <div className="flex flex-wrap gap-2.5 pt-2">
                <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800/80 border border-slate-700 text-xs font-medium text-slate-200">
                  <Clock className="w-3.5 h-3.5 text-rose-400" />
                  Groom ≥ 21 Yrs • Bride ≥ 18 Yrs
                </div>
                <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800/80 border border-slate-700 text-xs font-medium text-slate-200">
                  <Users className="w-3.5 h-3.5 text-indigo-400" />
                  2 Witnesses (HMA) / 3 Witnesses (SMA)
                </div>
                <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800/80 border border-slate-700 text-xs font-medium text-slate-200">
                  <MapPin className="w-3.5 h-3.5 text-emerald-400" />
                  &gt; 30 Days Jurisdictional Residence
                </div>
                <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800/80 border border-slate-700 text-xs font-medium text-slate-200">
                  <Banknote className="w-3.5 h-3.5 text-amber-400" />
                  Statutory Fee: ₹100 – ₹500
                </div>
              </div>
            </div>

            {/* Quick Action Card */}
            <div className="lg:col-span-4">
              <div className="bg-white/10 backdrop-blur-md p-6 rounded-2xl border border-white/15 space-y-4 text-white shadow-xl">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold uppercase tracking-wider text-rose-300">Quick Navigator</span>
                  <Sparkles className="w-4 h-4 text-rose-400" />
                </div>
                <div className="space-y-2">
                  <a
                    href="#pathway-selector"
                    className="flex items-center justify-between p-3 rounded-xl bg-white/10 hover:bg-white/20 transition text-sm font-medium"
                  >
                    <span>1. Choose Registration Pathway</span>
                    <ArrowRight className="w-4 h-4 text-rose-300" />
                  </a>
                  <a
                    href="#eligibility-checker"
                    className="flex items-center justify-between p-3 rounded-xl bg-white/10 hover:bg-white/20 transition text-sm font-medium"
                  >
                    <span>2. Check Statutory Eligibility</span>
                    <ArrowRight className="w-4 h-4 text-rose-300" />
                  </a>
                  <a
                    href="#document-checklist"
                    className="flex items-center justify-between p-3 rounded-xl bg-white/10 hover:bg-white/20 transition text-sm font-medium"
                  >
                    <span>3. Verified Documents Checklist</span>
                    <ArrowRight className="w-4 h-4 text-rose-300" />
                  </a>
                  <a
                    href="#step-by-step-guide"
                    className="flex items-center justify-between p-3 rounded-xl bg-white/10 hover:bg-white/20 transition text-sm font-medium"
                  >
                    <span>4. Step-by-Step SRO Workflow</span>
                    <ArrowRight className="w-4 h-4 text-rose-300" />
                  </a>
                  <a
                    href="#roadmap-section"
                    className="flex items-center justify-between p-3 rounded-xl bg-white/10 hover:bg-white/20 transition text-sm font-medium"
                  >
                    <span>5. My Registration Roadmap</span>
                    <ArrowRight className="w-4 h-4 text-rose-300" />
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-10 space-y-12">
        {/* 2. CHOOSE YOUR REGISTRATION PATHWAY */}
        <section id="pathway-selector" className="space-y-4">
          <div className="border-b border-slate-200 pb-3">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <Scroll className="w-6 h-6 text-rose-600" />
              Choose Your Registration Pathway
            </h2>
            <p className="text-sm text-slate-600 mt-1">
              In India, marriage registration is governed under distinct statutory acts based on religious rites or civil preference. Select your pathway to view tailored requirements:
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Card 1: Hindu Marriage Act */}
            <div
              onClick={() => setSelectedPathway('HMA')}
              className={`cursor-pointer p-5 rounded-2xl border-2 transition relative ${
                selectedPathway === 'HMA'
                  ? 'border-rose-500 bg-rose-50/50 shadow-md ring-2 ring-rose-500/20'
                  : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
              }`}
            >
              {selectedPathway === 'HMA' && (
                <span className="absolute top-3 right-3 px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-600 text-white">
                  Active Focus
                </span>
              )}
              <div className="space-y-2">
                <span className="text-xs font-bold uppercase tracking-wider text-rose-600">Most Common</span>
                <h3 className="font-bold text-slate-900 text-base">Hindu Marriage Act, 1955</h3>
                <p className="text-xs text-slate-600 leading-relaxed">
                  For Hindus, Buddhists, Jains, and Sikhs whose marriage ceremony has <span className="font-semibold text-slate-800">already taken place</span>.
                </p>
                <div className="pt-2 text-[11px] space-y-1 text-slate-500 border-t border-slate-200/60">
                  <div>• <span className="font-semibold text-slate-700">Notice:</span> None (Direct SRO Slot)</div>
                  <div>• <span className="font-semibold text-slate-700">Witnesses:</span> 2 adult witnesses</div>
                  <div>• <span className="font-semibold text-slate-700">Fee:</span> ₹100 – ₹200</div>
                  <div>• <span className="font-semibold text-slate-700">Timeline:</span> 7–15 Days (Often same-day)</div>
                </div>
              </div>
            </div>

            {/* Card 2: Special Marriage Act */}
            <div
              onClick={() => setSelectedPathway('SMA')}
              className={`cursor-pointer p-5 rounded-2xl border-2 transition relative ${
                selectedPathway === 'SMA'
                  ? 'border-indigo-500 bg-indigo-50/50 shadow-md ring-2 ring-indigo-500/20'
                  : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
              }`}
            >
              {selectedPathway === 'SMA' && (
                <span className="absolute top-3 right-3 px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-600 text-white">
                  Active Focus
                </span>
              )}
              <div className="space-y-2">
                <span className="text-xs font-bold uppercase tracking-wider text-indigo-600">Civil / Court</span>
                <h3 className="font-bold text-slate-900 text-base">Special Marriage Act, 1954</h3>
                <p className="text-xs text-slate-600 leading-relaxed">
                  For <span className="font-semibold text-slate-800">any two citizens</span> (inter-faith, civil, or court marriage solemnized directly before the Marriage Officer).
                </p>
                <div className="pt-2 text-[11px] space-y-1 text-slate-500 border-t border-slate-200/60">
                  <div>• <span className="font-semibold text-slate-700">Notice:</span> <span className="text-indigo-700 font-bold">Mandatory 30-Day Public Notice</span></div>
                  <div>• <span className="font-semibold text-slate-700">Witnesses:</span> 3 adult witnesses</div>
                  <div>• <span className="font-semibold text-slate-700">Fee:</span> ₹150 – ₹500</div>
                  <div>• <span className="font-semibold text-slate-700">Timeline:</span> 30–45 Days</div>
                </div>
              </div>
            </div>

            {/* Card 3: Anand Marriage Act */}
            <div
              onClick={() => setSelectedPathway('ANAND')}
              className={`cursor-pointer p-5 rounded-2xl border-2 transition relative ${
                selectedPathway === 'ANAND'
                  ? 'border-amber-500 bg-amber-50/50 shadow-md ring-2 ring-amber-500/20'
                  : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
              }`}
            >
              {selectedPathway === 'ANAND' && (
                <span className="absolute top-3 right-3 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-600 text-white">
                  Active Focus
                </span>
              )}
              <div className="space-y-2">
                <span className="text-xs font-bold uppercase tracking-wider text-amber-600">Sikh / Religious</span>
                <h3 className="font-bold text-slate-900 text-base">Anand Marriage Act & Others</h3>
                <p className="text-xs text-slate-600 leading-relaxed">
                  For Sikh marriages solemnized via <span className="font-semibold text-slate-800">Anand Karaj rites</span>, and Christian/Parsi religious solemnizations.
                </p>
                <div className="pt-2 text-[11px] space-y-1 text-slate-500 border-t border-slate-200/60">
                  <div>• <span className="font-semibold text-slate-700">Notice:</span> None for already solemnized</div>
                  <div>• <span className="font-semibold text-slate-700">Witnesses:</span> 2 witnesses + Gurudwara / Priest proof</div>
                  <div>• <span className="font-semibold text-slate-700">Fee:</span> ₹100 – ₹250</div>
                  <div>• <span className="font-semibold text-slate-700">Timeline:</span> 7–15 Days</div>
                </div>
              </div>
            </div>

            {/* Card 4: Pathway Finder */}
            <div
              onClick={() => {
                setSelectedPathway('GUIDE');
                const el = document.getElementById('eligibility-checker');
                if (el) el.scrollIntoView({ behavior: 'smooth' });
              }}
              className={`cursor-pointer p-5 rounded-2xl border-2 transition relative ${
                selectedPathway === 'GUIDE'
                  ? 'border-emerald-500 bg-emerald-50/50 shadow-md ring-2 ring-emerald-500/20'
                  : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
              }`}
            >
              <div className="space-y-2">
                <span className="text-xs font-bold uppercase tracking-wider text-emerald-600">Interactive</span>
                <h3 className="font-bold text-slate-900 text-base">Pathway & Eligibility Advisor</h3>
                <p className="text-xs text-slate-600 leading-relaxed">
                  Not sure which act applies or have special circumstances (divorce, foreign spouse, inter-faith)? Take the questionnaire.
                </p>
                <div className="pt-2 text-[11px] space-y-1 text-emerald-700 font-semibold">
                  <div>→ Interactive 6-step questionnaire</div>
                  <div>→ Immediate preliminary advice</div>
                  <div>→ SRO jurisdiction guidance</div>
                </div>
              </div>
            </div>
          </div>

          {/* Dynamic Pathway Summary Banner */}
          <div className="p-4 rounded-xl bg-slate-100 border border-slate-200 text-sm flex items-start gap-3">
            <Info className="w-5 h-5 text-slate-600 shrink-0 mt-0.5" />
            <div className="text-slate-700">
              {selectedPathway === 'HMA' && (
                <span>
                  <strong className="text-slate-900">Hindu Marriage Act Path:</strong> Suitable when religious ceremonies are already completed. Both husband and wife must appear with 2 adult witnesses who witnessed the ceremony at the Sub-Registrar Office. Carry original wedding card and ceremony ritual photographs.
                </span>
              )}
              {selectedPathway === 'SMA' && (
                <span>
                  <strong className="text-slate-900">Special Marriage Act Path:</strong> Does not require any religious ceremony or conversion. Both parties give a Notice of Intended Marriage to the Marriage Officer. A statutory 30-day public notice is displayed. Afterward, solemnization occurs before the Marriage Officer with 3 witnesses.
                </span>
              )}
              {selectedPathway === 'ANAND' && (
                <span>
                  <strong className="text-slate-900">Anand Marriage Act Path:</strong> Specifically validates Sikh Anand Karaj marriages solemnized in a Gurudwara. Requires Anand Karaj certificate issued by the Gurudwara/Granthi and 2 credible witnesses.
                </span>
              )}
              {selectedPathway === 'GUIDE' && (
                <span>
                  <strong className="text-slate-900">Guided Decision Mode:</strong> Complete the interactive eligibility checker below to determine your statutory path, competent Sub-Registrar Office, and required document suite.
                </span>
              )}
            </div>
          </div>
        </section>

        {/* 3. ELIGIBILITY QUESTIONNAIRE */}
        <section id="eligibility-checker" className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          <div className="border-b border-slate-200 pb-4">
            <div className="flex items-center gap-2 text-rose-600 font-bold text-xs uppercase tracking-wider">
              <Scale className="w-4 h-4" />
              Statutory Evaluation
            </div>
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight mt-1">
              Interactive Eligibility & Pathway Questionnaire
            </h2>
            <p className="text-sm text-slate-600 mt-1">
              Answer these 6 questions to evaluate statutory eligibility, identify your competent Sub-Registrar Office, and discover whether a 30-day notice is required.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Q1: Ceremony Status */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-900 block">
                1. Has the marriage ceremony already taken place?
              </label>
              <div className="grid grid-cols-1 gap-2">
                {[
                  { id: 'ALREADY_SOLEMNIZED', label: 'Yes, ceremony already solemnized', sub: 'Religious rites (Hindu, Sikh, Christian) already held' },
                  { id: 'INTENDED_FUTURE', label: 'No, planning to marry / Court Marriage', sub: 'Solemnization directly before Marriage Officer (SMA)' },
                ].map((opt) => (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() => {
                      setQCeremonyStatus(opt.id);
                      setEvalSubmitted(true);
                    }}
                    className={`text-left p-3 rounded-xl border text-sm transition ${
                      qCeremonyStatus === opt.id
                        ? 'border-rose-500 bg-rose-50/50 font-medium text-rose-950'
                        : 'border-slate-200 hover:border-slate-300 text-slate-700'
                    }`}
                  >
                    <div className="font-semibold">{opt.label}</div>
                    <div className="text-xs text-slate-500 mt-0.5">{opt.sub}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Q2: Applicable Act */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-900 block">
                2. Which registration pathway do you believe applies?
              </label>
              <select
                value={qApplicableAct}
                onChange={(e) => {
                  setQApplicableAct(e.target.value);
                  setEvalSubmitted(true);
                }}
                className="w-full p-3 rounded-xl border border-slate-200 bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 font-medium"
              >
                <option value="HMA_1955">Hindu Marriage Act, 1955 (Hindus, Buddhists, Jains, Sikhs - 2 Witnesses)</option>
                <option value="SMA_1954">Special Marriage Act, 1954 (Civil / Inter-Faith / Court Marriage - 3 Witnesses + 30-Day Notice)</option>
                <option value="ANAND_2012">Anand Marriage Act (Sikh Anand Karaj Ceremony)</option>
                <option value="CHRISTIAN_PARSI">Christian or Parsi Marriage Acts</option>
                <option value="UNSURE">Unsure / Need Guidance on Legal Framework</option>
              </select>
              <p className="text-xs text-slate-500">
                SMA applies to all citizens; HMA strictly requires both spouses to belong to eligible faiths.
              </p>
            </div>

            {/* Q3: Statutory Legal Age */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-900 block">
                3. Are both parties at or above statutory legal minimum age?
              </label>
              <div className="grid grid-cols-1 gap-2">
                {[
                  { id: 'LEGAL_AGE_MET', label: 'Yes, Groom is ≥ 21 and Bride is ≥ 18', sub: 'Both meet prevailing statutory marriage age criteria' },
                  { id: 'UNDERAGE_ALERT', label: 'No, one or both are below minimum age', sub: 'Groom < 21 years or Bride < 18 years on marriage date' },
                ].map((opt) => (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() => {
                      setQLegalAge(opt.id);
                      setEvalSubmitted(true);
                    }}
                    className={`text-left p-3 rounded-xl border text-sm transition ${
                      qLegalAge === opt.id
                        ? opt.id === 'UNDERAGE_ALERT'
                          ? 'border-red-500 bg-red-50/60 font-medium text-red-950'
                          : 'border-rose-500 bg-rose-50/50 font-medium text-rose-950'
                        : 'border-slate-200 hover:border-slate-300 text-slate-700'
                    }`}
                  >
                    <div className="font-semibold">{opt.label}</div>
                    <div className="text-xs text-slate-500 mt-0.5">{opt.sub}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Q4: Registration Location / Jurisdiction */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-900 block">
                4. State & District of Residence or Intended Registration?
              </label>
              <div className="grid grid-cols-1 gap-2">
                {[
                  { id: 'ANDHRA_PRADESH', label: 'Andhra Pradesh (Resident or Marriage Venue)', sub: 'Eligible for AP IGRS (registration.ap.gov.in) & local SRO' },
                  { id: 'OTHER_INDIAN_STATE', label: 'Other Indian State / Union Territory', sub: 'Requires application via respective state IGR portal' },
                ].map((opt) => (
                  <button
                    key={opt.id}
                    type="button"
                    onClick={() => {
                      setQResidenceState(opt.id);
                      setEvalSubmitted(true);
                    }}
                    className={`text-left p-3 rounded-xl border text-sm transition ${
                      qResidenceState === opt.id
                        ? 'border-rose-500 bg-rose-50/50 font-medium text-rose-950'
                        : 'border-slate-200 hover:border-slate-300 text-slate-700'
                    }`}
                  >
                    <div className="font-semibold">{opt.label}</div>
                    <div className="text-xs text-slate-500 mt-0.5">{opt.sub}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Q5: Marital History */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-900 block">
                5. Prior Marital Status of Both Parties?
              </label>
              <select
                value={qMaritalStatus}
                onChange={(e) => {
                  setQMaritalStatus(e.target.value);
                  setEvalSubmitted(true);
                }}
                className="w-full p-3 rounded-xl border border-slate-200 bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 font-medium"
              >
                <option value="BOTH_UNMARRIED">Both parties are unmarried (Bachelor / Spinster)</option>
                <option value="DIVORCEE">Either party is legally divorced (Holds certified Family Court decree)</option>
                <option value="WIDOWED">Either party is widowed (Holds official death certificate of late spouse)</option>
                <option value="SEPARATED_WITHOUT_DECREE">Separated without formal legal divorce decree</option>
              </select>
              <p className="text-xs text-slate-500">
                Neither party must have a living spouse unless formally dissolved by court decree absolute.
              </p>
            </div>

            {/* Q6: Special Circumstances */}
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-900 block">
                6. Are any special circumstances involved?
              </label>
              <select
                value={qSpecialCircumstances}
                onChange={(e) => {
                  setQSpecialCircumstances(e.target.value);
                  setEvalSubmitted(true);
                }}
                className="w-full p-3 rounded-xl border border-slate-200 bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 font-medium"
              >
                <option value="NONE">None (Both Indian citizens residing locally)</option>
                <option value="FOREIGN_NATIONAL_NRI">One party is a Foreign Citizen / NRI (Requires Embassy NOC)</option>
                <option value="INTER_FAITH">Inter-Faith / Inter-Religious Marriage (Special Marriage Act route)</option>
                <option value="PROHIBITED_DEGREE_CUSTOM">Related within prohibited degrees where custom permits</option>
              </select>
              <p className="text-xs text-slate-500">
                Special circumstances require additional verifications such as Embassy NOCs or customary affidavits.
              </p>
            </div>
          </div>

          {/* Preliminary Guidance Output Box */}
          <div className="pt-4 border-t border-slate-200">
            {isUnderage ? (
              <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-900 space-y-2">
                <div className="flex items-center gap-2 font-bold text-red-800">
                  <FileWarning className="w-5 h-5 text-red-600" />
                  Statutory Age Restriction — Registration Barred
                </div>
                <p className="text-sm text-red-700 leading-relaxed">
                  Under Section 5(iii) of the Hindu Marriage Act and Section 4(c) of the Special Marriage Act, the groom must be minimum 21 years old and the bride minimum 18 years old on the marriage date. Registration cannot be processed by any Sub-Registrar Office if either party is below the legal age.
                </p>
                <div className="text-xs text-red-600 font-medium">
                  Advisory: Wait until both parties attain statutory legal age. Consult legal counsel if you have questions regarding personal rights.
                </div>
              </div>
            ) : isSeparatedWithoutDecree ? (
              <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 space-y-2">
                <div className="flex items-center gap-2 font-bold text-amber-800">
                  <AlertCircle className="w-5 h-5 text-amber-600" />
                  Legal Impediment: Prior Marriage Is Active
                </div>
                <p className="text-sm text-amber-700 leading-relaxed">
                  Living separately does not legally dissolve a marriage in India. Entering a second marriage without a certified <span className="font-semibold text-amber-950">Divorce Decree Absolute</span> issued by a competent Family Court constitutes bigamy under Section 494 IPC / BNS and renders the new marriage null and void.
                </p>
                <div className="text-xs text-amber-800 font-medium">
                  Required Action: Conclude formal divorce proceedings in Family Court and obtain the certified decree before applying for marriage registration.
                </div>
              </div>
            ) : isSmaRecommended ? (
              <div className="p-4 rounded-xl bg-indigo-50 border border-indigo-200 text-indigo-950 space-y-2">
                <div className="flex items-center gap-2 font-bold text-indigo-900">
                  <CheckCircle2 className="w-5 h-5 text-indigo-600" />
                  Recommended Pathway: Special Marriage Act, 1954 (Civil / Court Marriage)
                </div>
                <p className="text-sm text-indigo-800 leading-relaxed">
                  Based on your answers, your marriage should be solemnized and registered under the <strong className="text-indigo-950">Special Marriage Act, 1954</strong>. This route requires submitting a Notice of Intended Marriage to the Sub-Registrar where at least one party has resided for &gt; 30 days. After a mandatory 30-day public notice without valid objections, the marriage is solemnized before the Marriage Officer with 3 witnesses.
                </p>
                <div className="flex flex-wrap gap-4 text-xs text-indigo-700 pt-1">
                  <span><strong>Statutory Fee:</strong> ₹150 – ₹500</span>
                  <span><strong>Mandatory Notice:</strong> 30 Days</span>
                  <span><strong>Witnesses:</strong> 3 Adults</span>
                  <span><strong>Authority:</strong> SRO Marriage Officer</span>
                </div>
              </div>
            ) : (
              <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-950 space-y-2">
                <div className="flex items-center gap-2 font-bold text-emerald-900">
                  <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                  Recommended Pathway: Hindu Marriage Act, 1955 (Registration of Solemnized Marriage)
                </div>
                <p className="text-sm text-emerald-800 leading-relaxed">
                  Based on your answers, your marriage can be registered under the <strong className="text-emerald-950">Hindu Marriage Act, 1955</strong>. Since the religious ceremony has already taken place, <span className="underline font-semibold">no 30-day notice is required</span>. You can book an appointment online via the AP Registration & Stamps portal (IGRS AP) or MeeSeva and appear at the SRO with 2 witnesses, wedding photos, and wedding card.
                </p>
                <div className="flex flex-wrap gap-4 text-xs text-emerald-700 pt-1">
                  <span><strong>Statutory Fee:</strong> ₹100 – ₹200</span>
                  <span><strong>Notice Period:</strong> None (Direct SRO Slot)</span>
                  <span><strong>Witnesses:</strong> 2 Adults</span>
                  <span><strong>Timeline:</strong> 7–15 Days (Often Same-Day)</span>
                </div>
              </div>
            )}

            {/* Disclaimer */}
            <div className="text-[11px] text-slate-500 pt-2 italic">
              * Note: This questionnaire provides preliminary informational guidance based on Indian statutory law. It does not constitute a final legal determination. The final authority to accept and register a marriage rests with the competent Sub-Registrar of Marriages / Marriage Officer.
            </div>
          </div>
        </section>

        {/* 4. STEP-BY-STEP SRO WORKFLOW */}
        <section id="step-by-step-guide" className="space-y-6">
          <div className="border-b border-slate-200 pb-3">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <Building2 className="w-6 h-6 text-rose-600" />
              Step-by-Step Registration & SRO Verification Guide
            </h2>
            <p className="text-sm text-slate-600 mt-1">
              Follow this verified 7-stage workflow to navigate your marriage registration in Andhra Pradesh without delays or rejections.
            </p>
          </div>

          {/* Comparison Callout: HMA vs SMA */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-sm space-y-1">
              <div className="font-bold text-rose-900 flex items-center gap-1.5">
                <Heart className="w-4 h-4 text-rose-600" />
                Hindu Marriage Act (HMA 1955) Flow
              </div>
              <p className="text-xs text-rose-700 leading-relaxed">
                Ceremony already solemnized → Book SRO slot online via IGRS AP → Appear with spouse & 2 witnesses → Verify originals & biometrics → Receive certificate. <span className="font-semibold text-rose-900">Zero notice period.</span>
              </p>
            </div>

            <div className="p-4 rounded-xl bg-indigo-50 border border-indigo-200 text-sm space-y-1">
              <div className="font-bold text-indigo-900 flex items-center gap-1.5">
                <Scale className="w-4 h-4 text-indigo-600" />
                Special Marriage Act (SMA 1954) Flow
              </div>
              <p className="text-xs text-indigo-700 leading-relaxed">
                File Notice of Intended Marriage → Mandatory 30-day public notice on SRO notice board → No valid objections received → Solemnization before Marriage Officer with 3 witnesses. <span className="font-semibold text-indigo-900">30-day statutory notice is mandatory.</span>
              </p>
            </div>
          </div>

          {/* The 7 Steps */}
          <div className="space-y-4">
            {[
              {
                num: 1,
                title: 'Determine Legal Pathway & Identify Jurisdictional SRO',
                desc: 'Confirm whether applying under HMA (already solemnized) or SMA (court marriage). Identify the competent Sub-Registrar Office (SRO) in Andhra Pradesh. Under statutory rules, registration can only happen in the SRO circle where either the husband or wife has resided for over 30 days, or where the wedding ceremony occurred. Locate your SRO circle on registration.ap.gov.in.',
                time: 'Step 1',
                tip: 'Jurisdictional mismatch is the #1 reason Sub-Registrars reject applications. Double-check your local Mandal / Ward SRO boundaries.',
              },
              {
                num: 2,
                title: 'Online Form Entry & Appointment Slot Booking',
                desc: 'Visit the official AP Registration and Stamps Department portal (IGRS AP - registration.ap.gov.in) or visit your nearest MeeSeva citizen centre. Complete the Memorandum of Marriage with bride, groom, solemnization, and witness details. Choose a convenient physical appointment slot at the SRO.',
                time: '30 Minutes',
                tip: 'Ensure that the spelling of names matches your Aadhaar, 10th marksheet, and wedding card letter-for-letter.',
              },
              {
                num: 3,
                title: 'Document Upload & Statutory Fee Payment',
                desc: 'Upload clear scans of age proofs (10th marksheet/birth certificate), address proofs (> 30 days stay), joint passport photos, ceremony ritual photos, wedding invitation, and sworn notarized affidavits. Pay the official registration fee online (~₹100 to ₹200 for HMA, ₹150 for SMA notice). Preserve the payment acknowledgement receipt.',
                time: '15 Minutes',
                tip: 'Keep fee receipt and ARN appointment acknowledgement slip printed for SRO entry.',
              },
              {
                num: 4,
                title: 'Witness Organization & Physical Appearance Preparation',
                desc: 'Organize your mandatory witnesses: 2 adult witnesses for Hindu Marriage Act, or 3 adult witnesses for Special Marriage Act. Witnesses must have personally witnessed the marriage ceremony (HMA) or consent (SMA). All witnesses must carry original government photo ID (Aadhaar Card, PAN Card, or Passport) and 2 passport photos.',
                time: 'Before Visit',
                tip: 'Parents, relatives, or adult friends who attended the wedding can serve as credible witnesses.',
              },
              {
                num: 5,
                title: 'Mandatory 30-Day Public Notice Display (SMA Only)',
                desc: 'For Special Marriage Act registrations, the Marriage Officer posts the Notice of Intended Marriage on the SRO notice board for a mandatory 30-day period. This invites any lawful objections (such as existing active marriage or underage parties). If no valid objection is filed within 30 days, the marriage proceeds to solemnization. (Skip this step for HMA already solemnized marriages).',
                time: '30 Days Window',
                tip: 'Under SMA, continuous residence in the jurisdiction during the 30-day notice window is legally required.',
              },
              {
                num: 6,
                title: 'SRO Physical Verification & Biometric Capture',
                desc: 'Husband, Wife, and all mandatory witnesses appear in person before the Sub-Registrar on the scheduled date and time. The Sub-Registrar inspects all original documents and affidavits. Biometric verification is conducted: fingerprints of the couple and witnesses are recorded, and live webcam photographs are captured for the state register.',
                time: '1–2 Hours',
                tip: 'Physical attendance of both parties and witnesses is mandatory under law; no proxy or power of attorney is permitted.',
              },
              {
                num: 7,
                title: 'Marriage Register Entry & Certificate Download',
                desc: 'Upon successful verification, the Sub-Registrar signs and registers the marriage in the official State Marriage Register. The official digitally signed Marriage Certificate carrying a security QR code is generated. Citizens can collect the printed certificate from the SRO, download the digital PDF from the IGRS AP portal, or access it via DigiLocker.',
                time: 'Same Day / 7 Days',
                tip: 'The certificate carries legal evidentiary value under the Indian Evidence Act for passport spouse name endorsement, visa processing, and banking.',
              },
            ].map((s) => (
              <div key={s.num} className="bg-white p-5 rounded-xl border border-slate-200/80 shadow-sm space-y-3">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-rose-100 text-rose-700 font-bold flex items-center justify-center text-sm shrink-0">
                      {s.num}
                    </span>
                    <h3 className="text-base font-bold text-slate-900">{s.title}</h3>
                  </div>
                  <span className="text-xs px-2.5 py-1 rounded-md bg-slate-100 text-slate-600 font-semibold shrink-0">
                    {s.time}
                  </span>
                </div>
                <p className="text-sm text-slate-600 leading-relaxed pl-11">{s.desc}</p>
                <div className="ml-11 p-2.5 rounded-lg bg-amber-50/70 border border-amber-200/60 text-xs text-amber-900 flex items-start gap-2">
                  <Info className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                  <span><strong>Important:</strong> {s.tip}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Common Mistakes To Avoid */}
          <div className="bg-rose-50/70 border border-rose-200 p-5 rounded-2xl space-y-3">
            <h3 className="font-bold text-rose-950 text-sm flex items-center gap-2">
              <FileWarning className="w-4 h-4 text-rose-600" />
              Common Candidate Mistakes That Lead To SRO Rejection
            </h3>
            <ul className="text-xs text-rose-900 space-y-1.5 list-disc pl-5">
              <li><strong>Jurisdictional Mismatch:</strong> Applying at an SRO where neither party has resided for 30 days nor where the wedding took place.</li>
              <li><strong>Name Discrepancies:</strong> Spelling mismatch between Aadhaar Card, Class 10 Certificate, and Wedding Card (even one character error can delay registration).</li>
              <li><strong>Missing Witnesses:</strong> Bringing only 1 witness for HMA (needs 2) or only 2 witnesses for SMA (needs 3). Witnesses missing original ID cards.</li>
              <li><strong>Incomplete Divorce Decree:</strong> Submitting an interim stay or petition copy instead of a certified <span className="font-semibold">Divorce Decree Absolute</span>.</li>
              <li><strong>Missing Ritual Photos:</strong> Bringing only studio couple portraits without actual wedding ceremony ritual photographs (garland exchange/saptapadi).</li>
            </ul>
          </div>
        </section>

        {/* 5. DOCUMENT CHECKLIST */}
        <section id="document-checklist" className="space-y-6">
          <div className="border-b border-slate-200 pb-3 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
                <FileCheck2 className="w-6 h-6 text-rose-600" />
                Comprehensive Documents Checklist
              </h2>
              <p className="text-sm text-slate-600 mt-1">
                All original documents along with self-attested photocopies must be presented before the Sub-Registrar during physical appearance.
              </p>
            </div>

            {/* Progress Bar Badge */}
            <div className="bg-white p-3 rounded-xl border border-slate-200 shadow-sm min-w-[200px] space-y-1.5">
              <div className="flex justify-between text-xs font-bold">
                <span className="text-slate-600">Checklist Readiness</span>
                <span className="text-rose-600">{docProgressPercent}%</span>
              </div>
              <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-rose-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${docProgressPercent}%` }}
                />
              </div>
              <div className="text-[10px] text-slate-500 text-right">
                {completedCount} of {MARRIAGE_DOCUMENTS.length} items ready
              </div>
            </div>
          </div>

          {/* Filter Pills */}
          <div className="flex flex-wrap gap-2">
            {[
              { id: 'ALL', label: 'All Documents (11)' },
              { id: 'COUPLE', label: 'Mandatory Couple Suite (7)' },
              { id: 'WITNESSES', label: 'Witnesses Proofs (1)' },
              { id: 'SPECIAL', label: 'Special & Conditional Cases (3)' },
            ].map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setDocFilter(tab.id as any)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                  docFilter === tab.id
                    ? 'bg-rose-600 text-white shadow-sm'
                    : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Document Cards */}
          <div className="space-y-3">
            {filteredDocs.map((doc) => {
              const isDone = !!completedDocs[doc.id];
              const isMissing = !!missingDocs[doc.id];
              const isExpanded = !!expandedDocs[doc.id];

              return (
                <div
                  key={doc.id}
                  className={`bg-white rounded-xl border transition shadow-sm p-4 sm:p-5 ${
                    isDone
                      ? 'border-emerald-300 bg-emerald-50/20'
                      : isMissing
                      ? 'border-amber-300 bg-amber-50/20'
                      : 'border-slate-200'
                  }`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-3">
                      <button
                        type="button"
                        onClick={() => toggleDocComplete(doc.id, doc.name)}
                        className={`w-6 h-6 rounded-md border flex items-center justify-center shrink-0 mt-0.5 transition ${
                          isDone
                            ? 'bg-emerald-600 border-emerald-600 text-white'
                            : 'border-slate-300 hover:border-slate-400 bg-white'
                        }`}
                      >
                        {isDone && <Check className="w-4 h-4 stroke-[3]" />}
                      </button>

                      <div className="space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3
                            className={`text-sm sm:text-base font-bold ${
                              isDone ? 'line-through text-slate-500' : 'text-slate-900'
                            }`}
                          >
                            {doc.name}
                          </h3>
                          {doc.mandatory ? (
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-100 text-rose-700">
                              Commonly Required
                            </span>
                          ) : (
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800">
                              Conditional
                            </span>
                          )}
                          {isMissing && (
                            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500 text-white">
                              Flagged As Missing
                            </span>
                          )}
                        </div>
                        <p className="text-xs text-slate-600 leading-relaxed">{doc.description}</p>
                        <div className="text-[11px] text-slate-500">
                          <strong className="text-slate-700">Accepted Format:</strong> {doc.acceptedFormats}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <button
                        type="button"
                        onClick={() => toggleDocMissing(doc.id)}
                        className={`text-xs px-2.5 py-1 rounded-md border transition ${
                          isMissing
                            ? 'bg-amber-100 border-amber-300 text-amber-800 font-semibold'
                            : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        {isMissing ? 'Missing' : 'Flag Missing'}
                      </button>

                      <button
                        type="button"
                        onClick={() => toggleDocExpand(doc.id)}
                        className="text-slate-400 hover:text-slate-600 p-1 rounded-md transition"
                      >
                        {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                      </button>
                    </div>
                  </div>

                  {/* Expandable Details */}
                  {isExpanded && (
                    <div className="mt-4 pt-3 border-t border-slate-100 space-y-2 text-xs text-slate-600 pl-9">
                      <div>
                        <strong className="text-slate-800">How to Obtain:</strong> {doc.howToObtain}
                      </div>
                      <div className="text-rose-700">
                        <strong className="text-rose-900">Legal Standard:</strong> {doc.legalImportance}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </section>

        {/* 6. FEES AND PROCESSING TIME */}
        <section className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          <div className="border-b border-slate-200 pb-3">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <Banknote className="w-6 h-6 text-rose-600" />
              Statutory Fees & Processing Timeline (SLA)
            </h2>
            <p className="text-sm text-slate-600 mt-1">
              Statutory registration fees in Andhra Pradesh are strictly regulated by the State Registration and Stamps Department. Never pay unreceipted cash to third-party intermediaries.
            </p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-xs font-bold uppercase text-slate-600">
                  <th className="py-3 px-4">Registration Pathway / Scheme</th>
                  <th className="py-3 px-4">Applicable Act</th>
                  <th className="py-3 px-4">Statutory Fee (Official)</th>
                  <th className="py-3 px-4">User Charges / SRO</th>
                  <th className="py-3 px-4">Standard Processing SLA</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs sm:text-sm">
                <tr className="hover:bg-slate-50/50">
                  <td className="py-3 px-4 font-semibold text-slate-900">
                    Registration of Solemnized Marriage
                  </td>
                  <td className="py-3 px-4 text-slate-600">Hindu Marriage Act, 1955</td>
                  <td className="py-3 px-4 font-bold text-emerald-700">₹100</td>
                  <td className="py-3 px-4 text-slate-600">₹50 – ₹100 (MeeSeva / Biometric)</td>
                  <td className="py-3 px-4 text-slate-700">7 to 15 Days (Often Same-Day post SRO visit)</td>
                </tr>
                <tr className="hover:bg-slate-50/50">
                  <td className="py-3 px-4 font-semibold text-slate-900">
                    Notice of Intended Civil Marriage
                  </td>
                  <td className="py-3 px-4 text-slate-600">Special Marriage Act, 1954</td>
                  <td className="py-3 px-4 font-bold text-emerald-700">₹150</td>
                  <td className="py-3 px-4 text-slate-600">₹50 (Public Notice Board fee)</td>
                  <td className="py-3 px-4 text-slate-700">Mandatory 30-Day Notice Display</td>
                </tr>
                <tr className="hover:bg-slate-50/50">
                  <td className="py-3 px-4 font-semibold text-slate-900">
                    Solemnization & Certificate Issuance
                  </td>
                  <td className="py-3 px-4 text-slate-600">Special Marriage Act, 1954</td>
                  <td className="py-3 px-4 font-bold text-emerald-700">₹150 – ₹350</td>
                  <td className="py-3 px-4 text-slate-600">₹100 (Book entry & certificate)</td>
                  <td className="py-3 px-4 text-slate-700">Same day of solemnization (post 30-day notice)</td>
                </tr>
                <tr className="hover:bg-slate-50/50">
                  <td className="py-3 px-4 font-semibold text-slate-900">
                    Sikh Anand Karaj Solemnization
                  </td>
                  <td className="py-3 px-4 text-slate-600">Anand Marriage Act</td>
                  <td className="py-3 px-4 font-bold text-emerald-700">₹100</td>
                  <td className="py-3 px-4 text-slate-600">₹50 – ₹100</td>
                  <td className="py-3 px-4 text-slate-700">7 to 15 Days</td>
                </tr>
                <tr className="hover:bg-slate-50/50">
                  <td className="py-3 px-4 font-semibold text-slate-900">
                    Additional Certified Copy
                  </td>
                  <td className="py-3 px-4 text-slate-600">All Acts</td>
                  <td className="py-3 px-4 font-bold text-emerald-700">₹50 per copy</td>
                  <td className="py-3 px-4 text-slate-600">₹30</td>
                  <td className="py-3 px-4 text-slate-700">Instant on IGRS AP or 1–3 Days at SRO</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-600 space-y-1">
            <p>
              <strong className="text-slate-800">Fast-Track / Tatkal Note:</strong> In Andhra Pradesh, there is no separate ₹10,000 Tatkal scheme like in Delhi. As long as you have booked your appointment slot online via IGRS AP and your documents are in order, the Sub-Registrar generates and signs the marriage certificate on the same day or within a few working days of your physical biometric appearance.
            </p>
          </div>
        </section>

        {/* 7. OFFICIAL LINKS & LOCAL GUIDANCE */}
        <section className="space-y-4">
          <div className="border-b border-slate-200 pb-3">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <ExternalLink className="w-6 h-6 text-rose-600" />
              Verified Official Portals & Legal References
            </h2>
            <p className="text-sm text-slate-600 mt-1">
              Direct access to verified government registration portals, MeeSeva, and statutory acts. Links open securely in an external tab.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[
              {
                title: 'AP Registration & Stamps Dept (IGRS AP)',
                url: 'https://registration.ap.gov.in',
                desc: 'Official government portal for Sub-Registrar Office slot booking, marriage memorandum entry, and certificate download.',
                badge: 'Primary AP Portal',
              },
              {
                title: 'Andhra Pradesh MeeSeva Portal',
                url: 'https://ap.meeseva.gov.in',
                desc: 'Citizen services portal for offline SRO appointment scheduling, application submission, and revenue services.',
                badge: 'Citizen Centre Portal',
              },
              {
                title: 'DigiLocker Government Repository',
                url: 'https://www.digilocker.gov.in',
                desc: 'National digital document repository where digitally signed marriage certificates are pushed post SRO issuance.',
                badge: 'Digital Locker',
              },
              {
                title: 'Hindu Marriage Act, 1955 (India Code)',
                url: 'https://www.indiacode.nic.in/handle/123456789/1560',
                desc: 'Statutory text of the Hindu Marriage Act covering eligibility, ceremonies, prohibited degrees, and registration.',
                badge: 'Central Statute',
              },
              {
                title: 'Special Marriage Act, 1954 (India Code)',
                url: 'https://www.indiacode.nic.in/handle/123456789/1387',
                desc: 'Statutory text of the Special Marriage Act covering civil marriage notice, 30-day objection period, and solemnization.',
                badge: 'Central Statute',
              },
              {
                title: 'OnePath Service Centre Finder',
                url: '/service-centres',
                isInternal: true,
                desc: 'Discover verified physical citizen service centres and government offices across Andhra Pradesh.',
                badge: 'OnePath Directory',
              },
            ].map((link, idx) => (
              <div key={idx} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between space-y-3">
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-rose-50 text-rose-700 border border-rose-200">
                      {link.badge}
                    </span>
                  </div>
                  <h3 className="font-bold text-slate-900 text-sm">{link.title}</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">{link.desc}</p>
                </div>

                <div>
                  {link.isInternal ? (
                    <Link
                      to={link.url}
                      className="inline-flex items-center gap-1.5 text-xs font-semibold text-rose-600 hover:text-rose-700"
                    >
                      <span>Open Centre Finder</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  ) : (
                    <a
                      href={link.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1.5 text-xs font-semibold text-rose-600 hover:text-rose-700"
                    >
                      <span>Visit Official Portal</span>
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* 8. MY REGISTRATION ROADMAP */}
        <section id="roadmap-section" className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm space-y-6">
          <div className="border-b border-slate-200 pb-3 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
                <CheckCircle2 className="w-6 h-6 text-rose-600" />
                My Registration Roadmap
              </h2>
              <p className="text-sm text-slate-600 mt-1">
                Track your personal progress through the 10 sequential milestones from eligibility verification to certificate issuance.
              </p>
            </div>

            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 min-w-[200px] space-y-1.5">
              <div className="flex justify-between text-xs font-bold">
                <span className="text-slate-600">Roadmap Progress</span>
                <span className="text-rose-600">{taskProgressPercent}%</span>
              </div>
              <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-rose-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${taskProgressPercent}%` }}
                />
              </div>
              <div className="text-[10px] text-slate-500 text-right">
                {completedTasksCount} of {ROADMAP_TASKS.length} tasks complete
              </div>
            </div>
          </div>

          <div className="space-y-3">
            {ROADMAP_TASKS.map((task) => {
              const isDone = !!completedTasks[task.id];

              return (
                <div
                  key={task.id}
                  onClick={() => toggleTaskComplete(task.id)}
                  className={`cursor-pointer p-4 rounded-xl border transition flex items-start gap-3.5 ${
                    isDone
                      ? 'border-emerald-300 bg-emerald-50/20'
                      : 'border-slate-200 hover:border-slate-300 bg-white'
                  }`}
                >
                  <div
                    className={`w-6 h-6 rounded-md border flex items-center justify-center shrink-0 mt-0.5 transition ${
                      isDone
                        ? 'bg-emerald-600 border-emerald-600 text-white'
                        : 'border-slate-300 bg-white'
                    }`}
                  >
                    {isDone && <Check className="w-4 h-4 stroke-[3]" />}
                  </div>

                  <div className="flex-1 space-y-1">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <h4 className={`text-sm font-bold ${isDone ? 'line-through text-slate-500' : 'text-slate-900'}`}>
                        {task.stepNumber}. {task.title}
                      </h4>
                      <div className="flex items-center gap-2">
                        {task.actScope !== 'ALL' && (
                          <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-600">
                            {task.actScope} Only
                          </span>
                        )}
                        <span className="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-600 font-medium">
                          {task.timeline}
                        </span>
                      </div>
                    </div>
                    <p className="text-xs text-slate-600 leading-relaxed">{task.description}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      </div>
    </div>
  );
};

export default MarriageRegistrationPage;
