import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { collegeService, checklistApi } from '../services/api';
import { CollegeProcessDetail, CollegeProcessUpdate, ChecklistItem } from '../types';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  ArrowLeft,
  GraduationCap,
  Clock,
  Banknote,
  FileCheck2,
  Bell,
  ShieldCheck,
  AlertCircle,
  HelpCircle,
  Info,
  Calendar,
  ExternalLink,
} from 'lucide-react';

export const CollegeProcessDetailPage: React.FC = () => {
  const { collegeId, processId } = useParams<{ collegeId: string; processId: string }>();
  const { isAuthenticated } = useAuth();

  const [processDetail, setProcessDetail] = useState<CollegeProcessDetail | null>(null);
  const [updates, setUpdates] = useState<CollegeProcessUpdate[]>([]);
  const [checklist, setChecklist] = useState<Record<string, ChecklistItem>>({});
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'steps' | 'documents' | 'updates'>('steps');
  const [expandedDocInfo, setExpandedDocInfo] = useState<Record<string, boolean>>({});

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

  const fetchProcess = async () => {
    if (!collegeId || !processId) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await collegeService.getProcessDetail(collegeId, processId);
      setProcessDetail(data);

      try {
        const processUpdates = await collegeService.getProcessUpdates(collegeId, processId);
        setUpdates(processUpdates);
      } catch (err) {
        setUpdates(data.updates || []);
      }

      if (isAuthenticated) {
        try {
          const userItems = await checklistApi.getByTarget('COLLEGE_PROCESS', data.id);
          const map: Record<string, ChecklistItem> = {};
          userItems.forEach((item) => {
            map[item.documentName] = item;
          });
          setChecklist(map);
        } catch (ignored) {}
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load college process documentation.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProcess();
  }, [collegeId, processId, isAuthenticated]);

  const toggleDocumentCheck = async (docName: string) => {
    if (!isAuthenticated || !processDetail) return;
    const current = checklist[docName];
    const newCompleted = current ? !current.completed : true;

    try {
      const saved = await checklistApi.saveItem({
        id: current?.id,
        targetType: 'COLLEGE_PROCESS',
        targetId: processDetail.id,
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
    if (!isAuthenticated || !processDetail) return;
    const current = checklist[docName];
    const newMissing = current ? !current.isMissing : true;

    try {
      const saved = await checklistApi.saveItem({
        id: current?.id,
        targetType: 'COLLEGE_PROCESS',
        targetId: processDetail.id,
        documentName: docName,
        completed: current?.completed || false,
        isMissing: newMissing,
        userNotes: current?.userNotes || '',
      });
      setChecklist((prev) => ({ ...prev, [docName]: saved }));
    } catch (err) {
      console.error('Failed to update missing status', err);
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Retrieving institutional process workflow..." />;
  }

  if (error || !processDetail) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <ErrorAlert message={error || 'Process not found'} onRetry={fetchProcess} />
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Navigation Breadcrumb */}
      <Link
        to={`/colleges/${collegeId}`}
        className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to {processDetail.collegeName || 'College'}
      </Link>

      {/* Header Banner */}
      <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm">
        <div className="flex flex-wrap items-center gap-2 mb-3">
          <span className="text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-3 py-1 rounded-full border border-brand-100">
            {processDetail.category || 'Institutional Procedure'}
          </span>
          <span
            className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${
              processDetail.status === 'ACTIVE'
                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                : 'bg-slate-100 text-slate-700'
            }`}
          >
            {processDetail.status}
          </span>
        </div>

        <h1 className="text-2xl sm:text-4xl font-black text-slate-950 tracking-tight mb-3">
          {processDetail.title}
        </h1>
        <p className="text-sm font-semibold text-brand-600 mb-4">
          {processDetail.collegeName}
        </p>

        {processDetail.description && (
          <p className="text-slate-600 text-base leading-relaxed max-w-3xl mb-6">
            {processDetail.description}
          </p>
        )}

        {/* Key Metrics */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-6 border-t border-slate-100">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-brand-600 flex items-center justify-center flex-shrink-0">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Timeline / Window</p>
              <p className="text-sm font-semibold text-slate-800">
                {processDetail.timeline || 'Open round the year'}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
              <Banknote className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Institutional Fee</p>
              <p className="text-sm font-semibold text-slate-800">
                {processDetail.fees || 'Free'}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center flex-shrink-0">
              <FileCheck2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Required Documents</p>
              <p className="text-sm font-semibold text-slate-800">
                {processDetail.requiredDocuments?.length || 0} Total Prerequisites
              </p>
            </div>
          </div>
        </div>

        {processDetail.officialPortalUrl && (
          <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
            <span className="text-xs text-slate-500">Institutional Portal / Link:</span>
            <a
              href={processDetail.officialPortalUrl.startsWith('http') ? processDetail.officialPortalUrl : `https://${processDetail.officialPortalUrl}`}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center text-xs font-semibold text-brand-600 hover:text-brand-800 underline"
            >
              Open Official Portal &rarr;
            </a>
          </div>
        )}
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200">
        <button
          onClick={() => setActiveTab('steps')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'steps'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Procedural Steps ({processDetail.steps?.length || 0})
        </button>
        <button
          onClick={() => setActiveTab('documents')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'documents'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Document Checklist ({processDetail.requiredDocuments?.length || 0})
        </button>
        <button
          onClick={() => setActiveTab('overview')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'overview'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Eligibility & Notes
        </button>
        <button
          onClick={() => setActiveTab('updates')}
          className={`pb-3 px-4 text-sm font-semibold border-b-2 transition ${
            activeTab === 'updates'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Updates & Circulars ({updates.length})
        </button>
      </div>

      {/* Tab: Procedural Steps */}
      {activeTab === 'steps' && (
        <div className="space-y-4">
          {!processDetail.steps || processDetail.steps.length === 0 ? (
            <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-500 text-sm">
              No individual procedural steps have been added to this college process yet.
            </div>
          ) : (
            <div className="space-y-4">
              {processDetail.steps.map((step) => (
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

      {/* Tab: Documents */}
      {activeTab === 'documents' && (
        <div className="space-y-4">
          {!processDetail.requiredDocuments || processDetail.requiredDocuments.length === 0 ? (
            <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-500 text-sm">
              No document checklist requirements have been configured for this process yet.
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

              {processDetail.requiredDocuments.map((doc) => {
                const item = checklist[doc.documentName];
                const isCompleted = item?.completed || false;
                const isMissing = item?.isMissing || false;
                const isExpanded = !!expandedDocInfo[doc.documentName];

                return (
                  <div
                    key={doc.id}
                    className={`bg-white rounded-2xl border p-5 transition ${
                      isCompleted
                        ? 'border-emerald-200 bg-emerald-50/20'
                        : isMissing
                        ? 'border-rose-200 bg-rose-50/20'
                        : 'border-slate-200/80'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start space-x-3">
                        {isAuthenticated && (
                          <input
                            type="checkbox"
                            checked={isCompleted}
                            onChange={() => toggleDocumentCheck(doc.documentName)}
                            className="w-5 h-5 mt-0.5 text-brand-600 rounded border-slate-300 focus:ring-brand-500 cursor-pointer"
                          />
                        )}
                        <div>
                          <div className="flex items-center space-x-2">
                            <h4
                              className={`text-base font-bold ${
                                isCompleted ? 'line-through text-slate-400' : 'text-slate-900'
                              }`}
                            >
                              {doc.documentName}
                            </h4>
                            {doc.mandatory ? (
                              <span className="text-[10px] font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
                                Mandatory
                              </span>
                            ) : (
                              <span className="text-[10px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded">
                                Optional
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-slate-600 mt-1">{doc.description}</p>
                          {doc.acceptedFormats && (
                            <p className="text-[11px] text-slate-400 mt-1 font-mono">
                              Accepted Format: {doc.acceptedFormats}
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
                            <span>{isExpanded ? 'Hide Instructions' : 'How to Obtain'}</span>
                          </button>
                        )}
                        {isAuthenticated && (
                          <button
                            onClick={() => toggleMissingStatus(doc.documentName)}
                            className={`text-xs px-2.5 py-1.5 rounded-xl font-medium transition ${
                              isMissing
                                ? 'bg-rose-100 text-rose-800'
                                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                            }`}
                          >
                            {isMissing ? 'Flagged Missing' : 'Mark Missing'}
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

      {/* Tab: Overview & Eligibility */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm space-y-4">
            <h3 className="text-lg font-bold text-slate-900 flex items-center">
              <ShieldCheck className="w-5 h-5 mr-2 text-brand-600" /> Eligibility Criteria
            </h3>
            <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
              {processDetail.eligibilityCriteria || processDetail.eligibility || 'Standard student eligibility applicable per department regulations.'}
            </p>
          </div>

          {processDetail.importantNotes && (
            <div className="bg-amber-50/50 p-6 sm:p-8 rounded-2xl border border-amber-200/80 shadow-sm space-y-4">
              <h3 className="text-lg font-bold text-amber-900 flex items-center">
                <AlertCircle className="w-5 h-5 mr-2 text-amber-600" /> Important Notes / Department Circulars
              </h3>
              <p className="text-sm text-amber-900/80 leading-relaxed whitespace-pre-line">
                {processDetail.importantNotes}
              </p>
            </div>
          )}
        </div>
      )}

      {/* Tab: Updates & Circulars */}
      {activeTab === 'updates' && (
        <div className="space-y-4">
          {!updates || updates.length === 0 ? (
            <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-slate-500 text-sm">
              No official circulars or amendments have been posted for this process.
            </div>
          ) : (
            <div className="space-y-4">
              {updates.map((update, idx) => (
                <div
                  key={update.id || idx}
                  className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm space-y-2"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded border border-brand-100">
                      {update.updateType || 'Official Notice'}
                    </span>
                    {update.effectiveDate && (
                      <span className="text-xs text-slate-400 flex items-center">
                        <Calendar className="w-3.5 h-3.5 mr-1" />
                        Effective: {update.effectiveDate}
                      </span>
                    )}
                  </div>
                  <h4 className="text-base font-bold text-slate-900">{update.updateTitle || update.title}</h4>
                  {(update.announcementDetails || update.description) && (
                    <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
                      {update.announcementDetails || update.description}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
