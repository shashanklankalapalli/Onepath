import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { civicServicesApi, checklistApi } from '../services/api';
import {
  CivicService,
  ChecklistItem,
  QuestionnaireData,
  PathwayEvaluationResult,
} from '../types';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { JosaaCounsellingPage } from './JosaaCounsellingPage';
import { MarriageRegistrationPage } from './MarriageRegistrationPage';
import {
  ArrowLeft,
  Clock,
  Banknote,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  FileCheck2,
  ExternalLink,
  HelpCircle,
  Info,
  Sparkles,
  RotateCcw,
  GraduationCap,
  ChevronRight,
  Stamp,
  MapPin,
  Check,
} from 'lucide-react';

export const ServiceDetailPage: React.FC = () => {
  const { identifier } = useParams<{ identifier: string }>();
  const { isAuthenticated } = useAuth();

  const [service, setService] = useState<CivicService | null>(null);
  const [questionnaire, setQuestionnaire] = useState<QuestionnaireData | null>(null);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [evaluationResult, setEvaluationResult] = useState<PathwayEvaluationResult | null>(null);
  const [showQuestionnaire, setShowQuestionnaire] = useState<boolean>(true);
  const [isEvaluating, setIsEvaluating] = useState<boolean>(false);

  // Local checklist for personalized pathway (isolated from personal documents)
  const [pathwayCheckedDocs, setPathwayCheckedDocs] = useState<Record<string, boolean>>({});
  const [expandedDocInfo, setExpandedDocInfo] = useState<Record<string, boolean>>({});

  // Fallback checklist for services without questionnaire
  const [checklist, setChecklist] = useState<Record<string, ChecklistItem>>({});
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'documents' | 'process' | 'eligibility'>('documents');

  const fetchServiceData = async () => {
    if (!identifier) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await civicServicesApi.getByIdOrSlug(identifier);
      setService(data);

      if (data.hasQuestionnaire) {
        try {
          const qData = await civicServicesApi.getQuestionnaire(data.slug || identifier);
          setQuestionnaire(qData);

          // Set default initial answers
          const initialAnswers: Record<string, string> = {
            applicationType: 'FRESH',
            applicantCategory: 'COLLEGE_STUDENT_HOSTEL',
            scheme: 'NORMAL',
            bookletSize: 'PAGES_36',
            renewalReason: 'EXPIRY_PAGES_NO_CHANGE',
            educationLevel: 'NON_ECR_QUALIFIED',
          };
          if (qData.questions && qData.questions.length > 0) {
            qData.questions.forEach((q) => {
              if (q.options && q.options.length > 0 && !initialAnswers[q.id]) {
                initialAnswers[q.id] = q.options[0].value;
              }
            });
          }
          setAnswers(initialAnswers);
          setShowQuestionnaire(true);
        } catch (qErr) {
          console.error('Failed to load questionnaire', qErr);
        }
      } else {
        if (isAuthenticated) {
          try {
            const userItems = await checklistApi.getByTarget('SERVICE', data.id);
            const map: Record<string, ChecklistItem> = {};
            userItems.forEach((item) => {
              map[item.documentName] = item;
            });
            setChecklist(map);
          } catch (ignored) {}
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load service details.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchServiceData();
  }, [identifier, isAuthenticated]);

  const handleOptionSelect = (questionId: string, optionValue: string) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: optionValue,
    }));
  };

  const handleEvaluate = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!identifier) return;

    setIsEvaluating(true);
    setError(null);
    try {
      const result = await civicServicesApi.evaluatePathway(identifier, { answers });
      setEvaluationResult(result);
      setShowQuestionnaire(false);
      setActiveTab('documents');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to evaluate pathway. Please check your answers.');
    } finally {
      setIsEvaluating(false);
    }
  };

  const togglePathwayDocCheck = (docName: string) => {
    setPathwayCheckedDocs((prev) => ({
      ...prev,
      [docName]: !prev[docName],
    }));
  };

  const toggleDocHowToObtain = (docName: string) => {
    setExpandedDocInfo((prev) => ({
      ...prev,
      [docName]: !prev[docName],
    }));
  };

  const renderFormattedHowToObtain = (text: string, docName: string) => {
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    const lowerName = docName.toLowerCase();
    const isAntiRagging = lowerName.includes('anti-ragging');
    const isNotaryAffidavit = lowerName.includes('hostel affidavit') || lowerName.includes('day scholar affidavit');

    return (
      <div className="space-y-2.5">
        {isAntiRagging && (
          <div className="p-3.5 bg-blue-50 border border-blue-200 rounded-xl text-xs text-blue-950 flex items-start gap-2.5 shadow-xs">
            <ShieldCheck className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-blue-900">UGC Statutory Exemption: NO STAMP PAPER / NO NOTARY REQUIRED</p>
              <p className="mt-0.5 text-blue-800 leading-relaxed">
                Per UGC Regulations and Supreme Court of India directives, Anti-Ragging undertakings do NOT require non-judicial stamp paper or notary public attestation. Standard signed printouts on plain A4 white paper from antiragging.in are legally accepted.
              </p>
            </div>
          </div>
        )}

        {isNotaryAffidavit && (
          <div className="p-3.5 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-950 flex items-start gap-2.5 shadow-xs">
            <AlertCircle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-bold text-amber-900">Statutory Legal Execution: Non-Judicial Stamp Paper + Notary Seal Required</p>
              <p className="mt-0.5 text-amber-800 leading-relaxed">
                Must be procured on ₹20, ₹50, or ₹100 non-judicial stamp paper (as per state stamp laws) and signed by both Student and Parent in the presence of an Advocate or Notary Public with official red seal, stamp, and notary register entry serial number. Plain paper printouts are legally invalid.
              </p>
            </div>
          </div>
        )}

        <div className="text-xs text-slate-700 leading-relaxed space-y-1">
          {text.split('\n').map((line, idx) => {
            const trimmed = line.trim();
            const isHeader = /^[A-Z\s&/()-]{3,}:$/.test(trimmed) || (trimmed.endsWith(':') && trimmed.toUpperCase() === trimmed);
            const isSubHeader = /^[A-Z\s&/()-]{3,}:/.test(trimmed) && !isHeader;

            const parts = line.split(urlRegex);

            return (
              <div
                key={idx}
                className={
                  isHeader
                    ? 'font-bold text-slate-900 text-[12px] pt-2 pb-0.5 border-b border-slate-200/80 uppercase tracking-wide'
                    : isSubHeader
                    ? 'pt-1'
                    : 'min-h-[1.1rem]'
                }
              >
                {parts.map((part, pIdx) => {
                  if (part.match(urlRegex)) {
                    return (
                      <a
                        key={pIdx}
                        href={part}
                        target="_blank"
                        rel="noreferrer"
                        className="text-brand-600 hover:text-brand-800 underline font-semibold inline-flex items-center gap-0.5 mx-0.5"
                      >
                        {part}
                        <ExternalLink className="w-3 h-3 inline" />
                      </a>
                    );
                  }
                  if (isSubHeader && pIdx === 0 && part.includes(':')) {
                    const colonIndex = part.indexOf(':');
                    const label = part.substring(0, colonIndex);
                    const rest = part.substring(colonIndex + 1);
                    return (
                      <span key={pIdx}>
                        <span className="font-semibold text-slate-900">{label}:</span>
                        {rest}
                      </span>
                    );
                  }
                  return <span key={pIdx}>{part}</span>;
                })}
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  // Standard fallback toggle for non-questionnaire services
  const toggleDocumentCheck = async (docName: string) => {
    if (!isAuthenticated || !service) return;
    const current = checklist[docName];
    const newCompleted = current ? !current.completed : true;

    try {
      const saved = await checklistApi.saveItem({
        id: current?.id,
        targetType: 'SERVICE',
        targetId: service.id,
        documentName: docName,
        completed: newCompleted,
        isMissing: current?.isMissing || false,
        userNotes: current?.userNotes || '',
      });
      setChecklist((prev) => ({ ...prev, [docName]: saved }));
    } catch (err) {
      console.error('Failed to update checklist item', err);
    }
  };

  const toggleMissingStatus = async (docName: string) => {
    if (!isAuthenticated || !service) return;
    const current = checklist[docName];
    const newMissing = current ? !current.isMissing : true;

    try {
      const saved = await checklistApi.saveItem({
        id: current?.id,
        targetType: 'SERVICE',
        targetId: service.id,
        documentName: docName,
        completed: current?.completed || false,
        isMissing: newMissing,
        userNotes: current?.userNotes || '',
      });
      setChecklist((prev) => ({ ...prev, [docName]: saved }));
    } catch (err) {
      console.error('Failed to update missing document status', err);
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Loading verified service details..." />;
  }

  if (error && !service) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <ErrorAlert message={error || 'Service not found'} onRetry={fetchServiceData} />
      </div>
    );
  }

  if (!service) return null;

  if (
    identifier === 'josaa-state-admissions-counselling' ||
    identifier === 'josaa-counselling' ||
    identifier === 'josaa' ||
    identifier === '4' ||
    service.slug === 'josaa-state-admissions-counselling' ||
    (service.name && service.name.toLowerCase().includes('josaa'))
  ) {
    return <JosaaCounsellingPage />;
  }

  if (
    identifier === 'marriage-registration' ||
    identifier === 'marriage' ||
    identifier === 'marriage-certificate' ||
    identifier === '5' ||
    service.slug === 'marriage-registration' ||
    (service.name && service.name.toLowerCase().includes('marriage'))
  ) {
    return <MarriageRegistrationPage />;
  }

  // ==========================================
  // VIEW 1: QUESTIONNAIRE (IF HAS QUESTIONNAIRE AND ACTIVE)
  // ==========================================
  if (service.hasQuestionnaire && showQuestionnaire && questionnaire) {
    const visibleQuestions = questionnaire.questions.filter((q) => {
      if (!q.dependsOn) return true;
      const [depField, depVal] = q.dependsOn.split('=');
      return answers[depField] === depVal;
    });

    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
        {/* Navigation Breadcrumb */}
        <Link
          to="/services"
          className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900 transition"
        >
          <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Services Directory
        </Link>

        {/* Questionnaire Hero Banner */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900 via-brand-950 to-blue-900 text-white p-6 sm:p-10 shadow-xl">
          <div className="relative z-10 max-w-2xl space-y-3">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-500/20 text-brand-300 border border-brand-400/30 text-xs font-bold uppercase tracking-wider">
              <Sparkles className="w-3.5 h-3.5" /> Intelligent Pathway Navigator
            </div>
            <h1 className="text-2xl sm:text-4xl font-black tracking-tight text-white">
              {service.name}
            </h1>
            <p className="text-sm sm:text-base text-slate-300 leading-relaxed">
              Every applicant has different requirements depending on residency, age, vehicle category, and existing documentation.
              Answer {visibleQuestions.length} simple questions to receive your exact official pathway, tailored document checklist, and accurate government fee.
            </p>
          </div>
          <div className="absolute right-0 bottom-0 opacity-10 pointer-events-none translate-x-12 translate-y-12">
            <Stamp className="w-72 h-72 text-white" />
          </div>
        </div>

        {error && <ErrorAlert message={error} />}

        {/* Questionnaire Questions */}
        <form onSubmit={handleEvaluate} className="space-y-8">
          {visibleQuestions.map((question, index) => {
            const currentSelected = answers[question.id];

            return (
              <div
                key={question.id}
                className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm transition hover:border-brand-200"
              >
                <div className="flex items-start gap-3 mb-4">
                  <span className="w-7 h-7 rounded-xl bg-brand-50 text-brand-700 font-extrabold text-sm flex items-center justify-center flex-shrink-0 mt-0.5 border border-brand-100">
                    {index + 1}
                  </span>
                  <div>
                    <h3 className="text-base sm:text-lg font-bold text-slate-900 leading-snug">
                      {question.title}
                    </h3>
                    {question.subtitle && (
                      <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                        {question.subtitle}
                      </p>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 mt-4">
                  {question.options.map((option) => {
                    const isSelected = currentSelected === option.value;

                    return (
                      <button
                        type="button"
                        key={option.value}
                        onClick={() => handleOptionSelect(question.id, option.value)}
                        className={`text-left p-4 rounded-2xl border-2 transition relative flex flex-col justify-between ${
                          isSelected
                            ? 'border-brand-600 bg-brand-50/40 ring-2 ring-brand-600/10'
                            : 'border-slate-200/90 hover:border-slate-300 bg-slate-50/50 hover:bg-slate-50'
                        }`}
                      >
                        <div>
                          <div className="flex items-center justify-between gap-2 mb-1.5">
                            <span
                              className={`text-sm font-bold ${
                                isSelected ? 'text-brand-950' : 'text-slate-800'
                              }`}
                            >
                              {option.label}
                            </span>
                            <div
                              className={`w-4 h-4 rounded-full border flex items-center justify-center flex-shrink-0 transition ${
                                isSelected
                                  ? 'border-brand-600 bg-brand-600 text-white'
                                  : 'border-slate-300 bg-white'
                              }`}
                            >
                              {isSelected && <Check className="w-3 h-3 stroke-[3]" />}
                            </div>
                          </div>

                          {option.description && (
                            <p className="text-xs text-slate-600 leading-relaxed">
                              {option.description}
                            </p>
                          )}
                        </div>

                        {option.badge && (
                          <div className="mt-3">
                            <span className="inline-block text-[10px] font-bold text-brand-700 bg-brand-100/70 px-2 py-0.5 rounded-md">
                              {option.badge}
                            </span>
                          </div>
                        )}
                      </button>
                    );
                  })}
                </div>
              </div>
            );
          })}

          {/* Evaluation Submit Bar */}
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
            <div>
              <p className="text-sm font-bold text-slate-900">
                Ready to view your tailored checklist & procedure?
              </p>
              <p className="text-xs text-slate-500">
                Your answers will map directly to official government statutory regulations.
              </p>
            </div>
            <button
              type="submit"
              disabled={isEvaluating}
              className="w-full sm:w-auto px-6 py-3.5 bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white font-bold text-sm rounded-xl shadow-md shadow-brand-600/20 transition flex items-center justify-center gap-2 flex-shrink-0 disabled:opacity-60"
            >
              {isEvaluating ? (
                <span>Determining Pathway...</span>
              ) : (
                <>
                  <span>Determine My {service.name.includes('Passport') ? 'Passport' : (service.name.includes('Driving') ? 'Licence & Vehicle' : (service.name.includes('Property') ? 'Property' : (service.name.includes('PAN') ? 'PAN' : 'Service')))} Pathway</span>
                  <ChevronRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    );
  }

  // ==========================================
  // VIEW 2: PERSONALIZED EVALUATED RESULT (IF EVALUATED)
  // ==========================================
  if (service.hasQuestionnaire && evaluationResult && !showQuestionnaire) {
    const totalDocs = evaluationResult.documents.length;
    const completedDocs = evaluationResult.documents.filter(
      (d) => pathwayCheckedDocs[d.documentName]
    ).length;

    return (
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
        {/* Navigation & Retake Bar */}
        <div className="flex flex-wrap items-center justify-between gap-4">
          <Link
            to="/services"
            className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900 transition"
          >
            <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Services Directory
          </Link>

          <button
            type="button"
            onClick={() => setShowQuestionnaire(true)}
            className="inline-flex items-center text-xs font-bold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 px-3.5 py-1.5 rounded-lg transition"
          >
            <RotateCcw className="w-3.5 h-3.5 mr-1.5" /> Change Answers / Retake Questionnaire
          </button>
        </div>

        {/* Personalized Banner */}
        <div className="p-4 sm:p-5 rounded-2xl bg-emerald-50 border border-emerald-200 flex items-start gap-3 shadow-sm">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
          <div className="text-sm text-emerald-950">
            <span className="font-extrabold block mb-0.5">
              Based on your answers, this is the process that applies to you:
            </span>
            <p className="text-emerald-800 text-xs sm:text-sm leading-relaxed">
              {evaluationResult.matchReason}
            </p>
          </div>
        </div>

        {/* Pathway Header Card */}
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm space-y-6">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-3 py-1 rounded-full border border-brand-100">
                {service.name}
              </span>
              {evaluationResult.badgeText && (
                <span className="text-xs font-bold text-blue-700 bg-blue-50 px-3 py-1 rounded-full border border-blue-100">
                  {evaluationResult.badgeText}
                </span>
              )}
            </div>
            <span className="text-xs font-semibold text-slate-500">
              Official {service.name.includes('Passport') ? 'MEA 2026' : (service.name.includes('Driving') ? 'MoRTH Parivahan' : (service.name.includes('Property') ? 'AP IGRS & Revenue' : 'Revenue Department'))} Guidelines
            </span>
          </div>

          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-950 tracking-tight">
              {evaluationResult.pathwayTitle}
            </h1>
            {evaluationResult.validity && (
              <p className="text-sm font-medium text-slate-600 mt-1">
                <span className="text-slate-400 font-normal">Validity:</span>{' '}
                {evaluationResult.validity}
              </p>
            )}
          </div>

          {/* Key Metrics Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4 border-t border-slate-100">
            <div className="flex items-start space-x-3 p-3.5 rounded-2xl bg-slate-50 border border-slate-100">
              <div className="w-10 h-10 rounded-xl bg-blue-50 text-brand-600 flex items-center justify-center flex-shrink-0">
                <Clock className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-400 font-medium">Processing Timeline</p>
                <p className="text-sm font-bold text-slate-900 mt-0.5">
                  {evaluationResult.estimatedTimeline}
                </p>
              </div>
            </div>

            <div className="flex items-start space-x-3 p-3.5 rounded-2xl bg-slate-50 border border-slate-100">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
                <Banknote className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-400 font-medium">Government Fee</p>
                <p className="text-sm font-bold text-emerald-700 mt-0.5">
                  {evaluationResult.calculatedFee}
                </p>
                <p className="text-[11px] text-slate-500 mt-0.5 leading-snug">
                  {evaluationResult.feeBreakdown}
                </p>
              </div>
            </div>

            <div className="flex items-start space-x-3 p-3.5 rounded-2xl bg-slate-50 border border-slate-100">
              <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-400 font-medium">
                  {evaluationResult.policeVerificationMode?.toLowerCase().includes('test') || evaluationResult.policeVerificationMode?.toLowerCase().includes('inspection')
                    ? 'Testing & Verification'
                    : (service.name.includes('Passport') ? 'Police Verification' : (service.name.includes('Property') ? 'Verification & Authority' : 'Field & Desk Verification'))}
                </p>
                <p className="text-xs font-bold text-slate-900 mt-0.5 leading-tight">
                  {evaluationResult.policeVerificationMode}
                </p>
              </div>
            </div>
          </div>

          {/* Emigration and Official Portal Bar */}
          <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
            {evaluationResult.emigrationStatus && (
              <p className="text-slate-600 font-medium">
                <span className="font-bold text-slate-900">
                  {service.slug?.includes('property') ? 'Statutory Notice: ' : 'Emigration Status: '}
                </span>
                {evaluationResult.emigrationStatus}
              </p>
            )}

            <div className="flex items-center gap-2.5 flex-wrap">
              {service.slug?.includes('property') && (
                <a
                  href="https://www.google.com/maps/search/?api=1&query=Sub+Registrar+Office"
                  target="_blank"
                  rel="noreferrer"
                  id="find-sro-office-btn"
                  className="inline-flex items-center text-xs font-bold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 px-3.5 py-2 rounded-xl transition shadow-sm flex-shrink-0"
                >
                  <MapPin className="w-3.5 h-3.5 mr-1.5 text-brand-600" /> Find Nearby Sub-Registrar Office <ExternalLink className="w-3 h-3 ml-1 text-brand-400" />
                </a>
              )}
              <a
                href={evaluationResult.officialPortalUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center text-xs font-bold text-white bg-slate-900 hover:bg-black px-4 py-2 rounded-xl transition shadow-sm flex-shrink-0"
              >
                Open Official {service.name.includes('Passport') ? 'Passport Seva' : (service.name.includes('Driving') ? 'Parivahan' : (service.name.includes('Property') ? 'AP IGRS' : (service.name.includes('PAN') ? 'Income Tax' : 'e-District / Revenue')))} Portal <ExternalLink className="w-3.5 h-3.5 ml-1.5" />
              </a>
            </div>
          </div>
        </div>

        {/* Tab Controls */}
        <div className="flex border-b border-slate-200">
          <button
            onClick={() => setActiveTab('documents')}
            className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
              activeTab === 'documents'
                ? 'border-brand-600 text-brand-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Your Document Checklist ({totalDocs})
          </button>
          <button
            onClick={() => setActiveTab('process')}
            className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
              activeTab === 'process'
                ? 'border-brand-600 text-brand-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Step-by-Step Procedure ({evaluationResult.steps.length})
          </button>
          <button
            onClick={() => setActiveTab('eligibility')}
            className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
              activeTab === 'eligibility'
                ? 'border-brand-600 text-brand-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Eligibility & Critical Tips
          </button>
        </div>

        {/* TAB 1: PERSONALIZED CHECKLIST */}
        {activeTab === 'documents' && (
          <div className="space-y-4">
            {/* Checklist Progress Header */}
            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div>
                <p className="text-sm font-bold text-slate-900">
                  Prepared Documents: {completedDocs} of {totalDocs}
                </p>
                <p className="text-xs text-slate-500 mt-0.5">
                  Check off documents as you gather them. This checklist is saved locally for this government application and is kept separate from your university documents.
                </p>
              </div>
              <div className="w-full sm:w-48 bg-slate-200 rounded-full h-2.5 overflow-hidden flex-shrink-0">
                <div
                  className="bg-brand-600 h-2.5 rounded-full transition-all duration-300"
                  style={{ width: `${totalDocs > 0 ? (completedDocs / totalDocs) * 100 : 0}%` }}
                />
              </div>
            </div>

            {/* Document List */}
            <div className="space-y-3">
              {evaluationResult.documents.map((doc) => {
                const isChecked = !!pathwayCheckedDocs[doc.documentName];
                const isExpanded = !!expandedDocInfo[doc.documentName];

                return (
                  <div
                    key={doc.id || doc.documentName}
                    className={`bg-white rounded-2xl border transition p-4 sm:p-5 shadow-sm ${
                      isChecked ? 'border-emerald-300 bg-emerald-50/20' : 'border-slate-200/90'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3.5">
                        <button
                          type="button"
                          onClick={() => togglePathwayDocCheck(doc.documentName)}
                          className={`w-6 h-6 rounded-lg border flex items-center justify-center flex-shrink-0 mt-0.5 transition ${
                            isChecked
                              ? 'bg-brand-600 border-brand-600 text-white shadow-sm'
                              : 'border-slate-300 hover:border-slate-400 bg-white'
                          }`}
                        >
                          {isChecked && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                        </button>
                        <div className="space-y-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span
                              className={`text-sm sm:text-base font-bold ${
                                isChecked ? 'line-through text-slate-400' : 'text-slate-900'
                              }`}
                            >
                              {doc.documentName}
                            </span>
                            {doc.mandatory ? (
                              <span className="text-[10px] font-bold text-rose-700 bg-rose-50 border border-rose-200 px-2 py-0.5 rounded-md">
                                Mandatory
                              </span>
                            ) : (
                              <span className="text-[10px] font-bold text-slate-600 bg-slate-100 px-2 py-0.5 rounded-md">
                                Optional / Conditional
                              </span>
                            )}
                          </div>
                          {doc.description && (
                            <p className="text-xs text-slate-500 italic">
                              {doc.description}
                            </p>
                          )}
                          {doc.acceptedFormats && (
                            <p className="text-xs text-slate-400">
                              Format: {doc.acceptedFormats}
                            </p>
                          )}
                        </div>
                      </div>
                      {doc.howToObtain && (
                        <button
                          type="button"
                          onClick={() => toggleDocHowToObtain(doc.documentName)}
                          className="text-xs text-brand-600 hover:text-brand-800 font-semibold flex items-center gap-1 flex-shrink-0 mt-0.5"
                        >
                          <HelpCircle className="w-3.5 h-3.5" />
                          <span>{isExpanded ? 'Hide Info' : 'How to obtain'}</span>
                        </button>
                      )}
                    </div>
                    {isExpanded && doc.howToObtain && (
                      <div className="mt-4 p-4 sm:p-5 bg-slate-50/90 rounded-2xl border border-slate-200/90 text-xs shadow-inner">
                        <div className="flex items-center justify-between mb-3 pb-2.5 border-b border-slate-200">
                          <p className="font-bold text-slate-900 flex items-center gap-2 text-sm">
                            <HelpCircle className="w-4 h-4 text-brand-600" />
                            Procurement & Verification Guide: <span className="text-brand-700">{doc.documentName}</span>
                          </p>
                          <button
                            type="button"
                            onClick={() => toggleDocHowToObtain(doc.documentName)}
                            className="text-xs text-slate-400 hover:text-slate-700 font-semibold px-2 py-0.5 rounded-md hover:bg-slate-200/50"
                          >
                            Close &times;
                          </button>
                        </div>
                        {renderFormattedHowToObtain(doc.howToObtain, doc.documentName)}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 2: STEP-BY-STEP PROCEDURE */}
        {activeTab === 'process' && (
          <div className="space-y-4">
            <div className="p-4 bg-blue-50/60 rounded-2xl border border-blue-200/70 text-xs text-blue-900 flex items-start gap-2">
              <Info className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
              <span>
                These steps reflect the exact workflow for your pathway. Follow them sequentially from portal registration to Speed Post delivery.
              </span>
            </div>

            <div className="space-y-4">
              {evaluationResult.steps.map((step) => (
                <div
                  key={step.id}
                  className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm flex items-start space-x-4"
                >
                  <div className="w-8 h-8 rounded-full bg-brand-600 text-white font-bold text-sm flex items-center justify-center flex-shrink-0 mt-0.5 shadow-sm">
                    {step.stepNumber}
                  </div>
                  <div className="flex-1 space-y-2">
                    <div className="flex items-center justify-between flex-wrap gap-2">
                      <h3 className="text-base font-bold text-slate-900">{step.title}</h3>
                      {step.estimatedTime && (
                        <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-2.5 py-1 rounded-md flex items-center">
                          <Clock className="w-3 h-3 mr-1 text-slate-400" /> {step.estimatedTime}
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
                      {step.description}
                    </p>
                    {step.notes && (
                      <div className="p-3 bg-amber-50 rounded-xl border border-amber-200/60 text-xs text-amber-900 flex items-start space-x-2">
                        <Info className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
                        <span className="font-medium">{step.notes}</span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* TAB 3: ELIGIBILITY & CRITICAL TIPS */}
        {activeTab === 'eligibility' && (
          <div className="space-y-6">
            {/* Police Verification Mode Explainer */}
            <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
              <h3 className="text-lg font-bold text-slate-900 flex items-center">
                <ShieldCheck className="w-5 h-5 mr-2 text-brand-600" />
                Police Verification Mode: {evaluationResult.policeVerificationMode}
              </h3>
              <p className="text-sm text-slate-600 leading-relaxed">
                {evaluationResult.policeVerificationDetails}
              </p>
            </div>

            {/* Critical OnePath Tips for Students and Citizens */}
            {evaluationResult.criticalTips && evaluationResult.criticalTips.length > 0 && (
              <div className="bg-amber-50/50 p-6 sm:p-8 rounded-2xl border border-amber-200/80 shadow-sm space-y-4">
                <h3 className="text-lg font-bold text-amber-950 flex items-center">
                  <AlertCircle className="w-5 h-5 mr-2 text-amber-600" />
                  Critical Guidelines & Best Practices
                </h3>
                <div className="space-y-3">
                  {evaluationResult.criticalTips.map((tip, idx) => (
                    <div
                      key={idx}
                      className="p-3.5 bg-white rounded-xl border border-amber-200/60 text-xs sm:text-sm text-amber-950 flex items-start space-x-2.5"
                    >
                      <span className="w-5 h-5 rounded-full bg-amber-100 text-amber-800 font-bold text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
                        {idx + 1}
                      </span>
                      <p className="leading-relaxed">{tip}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* General Service Eligibility */}
            <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
              <h3 className="text-lg font-bold text-slate-900 flex items-center">
                <GraduationCap className="w-5 h-5 mr-2 text-brand-600" />
                General Passport Eligibility & Provisions
              </h3>
              <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
                {service.eligibility}
              </p>
            </div>
          </div>
        )}
      </div>
    );
  }

  // ==========================================
  // VIEW 3: STANDARD SERVICE DETAIL VIEW (FOR NON-QUESTIONNAIRE SERVICES)
  // ==========================================
  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Back button */}
      <Link
        to="/services"
        className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Services
      </Link>

      {/* Header card */}
      <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm">
        <div className="flex flex-wrap items-center gap-2 mb-3">
          <span className="text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-3 py-1 rounded-full border border-brand-100">
            {service.category}
          </span>
        </div>

        <h1 className="text-2xl sm:text-4xl font-black text-slate-950 tracking-tight mb-4">
          {service.name}
        </h1>

        <p className="text-slate-600 text-base leading-relaxed max-w-3xl mb-6">
          {service.description}
        </p>

        {/* Key Metrics */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-6 border-t border-slate-100">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-brand-600 flex items-center justify-center flex-shrink-0">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Estimated Timeline</p>
              <p className="text-sm font-semibold text-slate-800">{service.timeline || 'Not specified'}</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
              <Banknote className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Applicable Fees</p>
              <p className="text-sm font-semibold text-slate-800">{service.fees || 'Free of cost'}</p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
              <FileCheck2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Documents</p>
              <p className="text-sm font-semibold text-slate-800">
                {service.requiredDocuments?.length || 0} Total Requirements
              </p>
            </div>
          </div>
        </div>

        {service.officialPortalUrl && (
          <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between flex-wrap gap-2">
            <span className="text-xs text-slate-500">Official Department Resource:</span>
            <div className="flex items-center gap-2 flex-wrap">
              {service.slug?.includes('property') && (
                <a
                  href="https://www.google.com/maps/search/?api=1&query=Sub+Registrar+Office"
                  target="_blank"
                  rel="noreferrer"
                  id="find-sro-office-btn-view3"
                  className="inline-flex items-center text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 px-3 py-1.5 rounded-lg transition"
                >
                  <MapPin className="w-3.5 h-3.5 mr-1 text-brand-600" /> Find Nearby Sub-Registrar Office <ExternalLink className="w-3 h-3 ml-1 text-brand-400" />
                </a>
              )}
              <a
                href={service.officialPortalUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center text-xs font-semibold text-brand-600 hover:text-brand-800 underline"
              >
                Open Department Portal <ExternalLink className="w-3.5 h-3.5 ml-1" />
              </a>
            </div>
          </div>
        )}
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200">
        <button
          onClick={() => setActiveTab('process')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'process'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Step-by-Step Process ({service.steps?.length || 0})
        </button>
        <button
          onClick={() => setActiveTab('documents')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'documents'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Document Checklist ({service.requiredDocuments?.length || 0})
        </button>
        <button
          onClick={() => setActiveTab('eligibility')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'eligibility'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Eligibility & Notes
        </button>
      </div>

      {/* Tab Contents */}
      {activeTab === 'process' && (
        <div className="space-y-4">
          {!service.steps || service.steps.length === 0 ? (
            <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-500 text-sm">
              No detailed procedural steps have been published for this service yet.
            </div>
          ) : (
            <div className="space-y-4">
              {service.steps.map((step) => (
                <div
                  key={step.id}
                  className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm flex items-start space-x-4"
                >
                  <div className="w-8 h-8 rounded-full bg-brand-600 text-white font-bold text-sm flex items-center justify-center flex-shrink-0 mt-0.5">
                    {step.stepNumber}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center justify-between flex-wrap gap-2 mb-1">
                      <h3 className="text-base font-bold text-slate-900">{step.title}</h3>
                      {step.estimatedTime && (
                        <span className="text-xs font-medium text-slate-400 flex items-center">
                          <Clock className="w-3 h-3 mr-1" /> {step.estimatedTime}
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-slate-600 leading-relaxed">{step.description}</p>
                    {step.notes && (
                      <div className="mt-3 p-3 bg-amber-50 rounded-xl border border-amber-200/60 text-xs text-amber-900 flex items-start space-x-2">
                        <Info className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
                        <span>{step.notes}</span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'documents' && (
        <div className="space-y-4">
          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <p className="text-sm font-bold text-slate-900">
                Official Document Checklist ({service.requiredDocuments?.length || 0})
              </p>
              <p className="text-xs text-slate-500 mt-0.5">
                Verify and prepare all required original documents and certificates before proceeding.
              </p>
            </div>
          </div>

          {!service.requiredDocuments || service.requiredDocuments.length === 0 ? (
            <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-500 text-sm">
              No specific document prerequisites defined for this service.
            </div>
          ) : (
            <div className="space-y-3">
              {!isAuthenticated && (
                <div className="p-4 bg-brand-50 border border-brand-200 rounded-xl text-xs text-brand-800 flex items-center justify-between">
                  <span>Sign in to save this checklist and track missing documents.</span>
                  <Link to="/login" className="font-bold underline ml-2">
                    Sign In &rarr;
                  </Link>
                </div>
              )}

              {service.requiredDocuments.map((doc) => {
                const item = checklist[doc.documentName];
                const isCompleted = item?.completed || false;
                const isMissing = item?.isMissing || false;
                const isExpanded = !!expandedDocInfo[doc.documentName];

                return (
                  <div
                    key={doc.id || doc.documentName}
                    className={`bg-white rounded-2xl border transition p-4 sm:p-5 shadow-sm ${
                      isCompleted ? 'border-emerald-300 bg-emerald-50/20' : 'border-slate-200/90'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3.5">
                        {isAuthenticated && (
                          <button
                            type="button"
                            onClick={() => toggleDocumentCheck(doc.documentName)}
                            className={`w-6 h-6 rounded-lg border flex items-center justify-center flex-shrink-0 mt-0.5 transition ${
                              isCompleted
                                ? 'bg-brand-600 border-brand-600 text-white shadow-sm'
                                : 'border-slate-300 hover:border-slate-400 bg-white'
                            }`}
                          >
                            {isCompleted && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                          </button>
                        )}
                        <div className="space-y-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span
                              className={`text-sm sm:text-base font-bold ${
                                isCompleted ? 'line-through text-slate-400' : 'text-slate-900'
                              }`}
                            >
                              {doc.documentName}
                            </span>
                            {doc.mandatory ? (
                              <span className="text-[10px] font-bold text-rose-700 bg-rose-50 border border-rose-200 px-2 py-0.5 rounded-md">
                                Mandatory
                              </span>
                            ) : (
                              <span className="text-[10px] font-bold text-slate-600 bg-slate-100 px-2 py-0.5 rounded-md">
                                Optional / Conditional
                              </span>
                            )}
                            {isMissing && (
                              <span className="text-[10px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-md">
                                Marked Missing
                              </span>
                            )}
                          </div>
                          {doc.description && (
                            <p className="text-xs text-slate-500 italic">
                              {doc.description}
                            </p>
                          )}
                          {doc.acceptedFormats && (
                            <p className="text-xs text-slate-400">
                              Format: {doc.acceptedFormats}
                            </p>
                          )}
                        </div>
                      </div>

                      <div className="flex items-center gap-2 flex-shrink-0">
                        {doc.howToObtain && (
                          <button
                            type="button"
                            onClick={() => toggleDocHowToObtain(doc.documentName)}
                            className="text-xs text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 px-3 py-1.5 rounded-xl font-semibold flex items-center gap-1.5 transition shadow-xs"
                          >
                            <HelpCircle className="w-3.5 h-3.5 text-brand-600" />
                            <span>{isExpanded ? 'Hide Info' : 'How to obtain'}</span>
                          </button>
                        )}
                        {isAuthenticated && (
                          <button
                            type="button"
                            onClick={() => toggleMissingStatus(doc.documentName)}
                            className={`text-xs px-2.5 py-1.5 rounded-xl border transition ${
                              isMissing
                                ? 'bg-amber-100 border-amber-300 text-amber-800 font-semibold'
                                : 'border-slate-200 text-slate-500 hover:bg-slate-50'
                            }`}
                          >
                            {isMissing ? 'Missing' : 'Mark Missing'}
                          </button>
                        )}
                      </div>
                    </div>

                    {/* Expandable Document Procurement Guidance */}
                    {isExpanded && doc.howToObtain && (
                      <div className="mt-4 p-4 sm:p-5 bg-slate-50/90 rounded-2xl border border-slate-200/90 text-xs shadow-inner">
                        <div className="flex items-center justify-between mb-3 pb-2.5 border-b border-slate-200">
                          <p className="font-bold text-slate-900 flex items-center gap-2 text-sm">
                            <HelpCircle className="w-4 h-4 text-brand-600" />
                            Procurement & Verification Guide: <span className="text-brand-700">{doc.documentName}</span>
                          </p>
                          <button
                            type="button"
                            onClick={() => toggleDocHowToObtain(doc.documentName)}
                            className="text-xs text-slate-400 hover:text-slate-700 font-semibold px-2 py-0.5 rounded-md hover:bg-slate-200/50"
                          >
                            Close &times;
                          </button>
                        </div>
                        {renderFormattedHowToObtain(doc.howToObtain, doc.documentName)}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {activeTab === 'eligibility' && (
        <div className="space-y-6">
          <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm space-y-4">
            <h3 className="text-lg font-bold text-slate-900 flex items-center">
              <ShieldCheck className="w-5 h-5 mr-2 text-brand-600" /> Eligibility Criteria
            </h3>
            <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
              {service.eligibility || 'Standard citizens / students meeting basic age prerequisites.'}
            </p>
          </div>

          {service.importantNotes && (
            <div className="bg-amber-50/50 p-6 sm:p-8 rounded-2xl border border-amber-200/80 shadow-sm space-y-4">
              <h3 className="text-lg font-bold text-amber-900 flex items-center">
                <AlertCircle className="w-5 h-5 mr-2 text-amber-600" /> Important Guidelines & Alerts
              </h3>
              <p className="text-sm text-amber-900/80 leading-relaxed whitespace-pre-line">
                {service.importantNotes}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
