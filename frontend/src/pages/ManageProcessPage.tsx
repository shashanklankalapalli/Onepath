import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { collegeService } from '../services/api';
import { CollegeProcessDetail, CollegeProcessStep, CollegeRequiredDocument, CollegeProcessUpdate } from '../types';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  ArrowLeft,
  Plus,
  Trash2,
  CheckCircle2,
  FileCheck2,
  Layers,
  Clock,
  Banknote,
  Globe,
  Bell,
  Info,
} from 'lucide-react';

export const ManageProcessPage: React.FC = () => {
  const { collegeId, processId } = useParams<{ collegeId: string; processId?: string }>();
  const isEditing = !!processId;
  const navigate = useNavigate();
  const { user } = useAuth();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('Admissions');
  const [eligibility, setEligibility] = useState('');
  const [timeline, setTimeline] = useState('');
  const [fees, setFees] = useState('');
  const [officialPortalUrl, setOfficialPortalUrl] = useState('');
  const [status, setStatus] = useState('ACTIVE');
  const [importantNotes, setImportantNotes] = useState('');

  // Structured child elements
  const [steps, setSteps] = useState<CollegeProcessStep[]>([]);
  const [documents, setDocuments] = useState<CollegeRequiredDocument[]>([]);
  const [updates, setUpdates] = useState<CollegeProcessUpdate[]>([]);

  const [isLoading, setIsLoading] = useState(isEditing);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // New step scratch state
  const [stepTitle, setStepTitle] = useState('');
  const [stepDesc, setStepDesc] = useState('');
  const [stepTime, setStepTime] = useState('');
  const [stepNotes, setStepNotes] = useState('');

  // New document scratch state
  const [docName, setDocName] = useState('');
  const [docDesc, setDocDesc] = useState('');
  const [docMandatory, setDocMandatory] = useState(true);
  const [docFormats, setDocFormats] = useState('PDF, JPG (Max 2MB)');
  const [docHowToObtain, setDocHowToObtain] = useState('');

  // New update scratch state
  const [updateTitle, setUpdateTitle] = useState('');
  const [updateDesc, setUpdateDesc] = useState('');
  const [updateType, setUpdateType] = useState('FEE_CHANGE');
  const [updateDate, setUpdateDate] = useState('');

  useEffect(() => {
    if (isEditing && collegeId && processId) {
      const loadProcess = async () => {
        setIsLoading(true);
        try {
          const [p, upList] = await Promise.all([
            collegeService.getProcessDetail(collegeId, processId),
            collegeService.getProcessUpdates(collegeId, processId).catch(() => [] as CollegeProcessUpdate[])
          ]);
          setTitle(p.title);
          setDescription(p.description || '');
          setCategory(p.category || 'Admissions');
          setEligibility(p.eligibilityCriteria || p.eligibility || '');
          setTimeline(p.timeline || '');
          setFees(p.fees || '');
          setOfficialPortalUrl(p.officialPortalUrl || '');
          setStatus(p.status || 'ACTIVE');
          setImportantNotes(p.importantNotes || '');
          setSteps(p.steps || []);
          setDocuments(p.requiredDocuments || []);
          const rawUpdates = (upList && upList.length > 0) ? upList : (p.updates || []);
          setUpdates(rawUpdates.map((u) => ({
            ...u,
            title: u.title || u.updateTitle || '',
            updateTitle: u.updateTitle || u.title || '',
            description: u.description || u.announcementDetails,
            announcementDetails: u.announcementDetails || u.description,
          })));
        } catch (err: any) {
          setError(err.response?.data?.message || 'Failed to load process details for editing.');
        } finally {
          setIsLoading(false);
        }
      };
      loadProcess();
    }
  }, [collegeId, processId, isEditing]);

  const handleAddStep = () => {
    if (!stepTitle.trim()) return;
    const newStep: CollegeProcessStep = {
      id: 0,
      processId: Number(processId) || 0,
      stepNumber: steps.length + 1,
      title: stepTitle.trim(),
      description: stepDesc.trim() || undefined,
      estimatedTime: stepTime.trim() || undefined,
      notes: stepNotes.trim() || undefined,
    };
    setSteps([...steps, newStep]);
    setStepTitle('');
    setStepDesc('');
    setStepTime('');
    setStepNotes('');
  };

  const handleRemoveStep = (index: number) => {
    const updated = steps.filter((_, i) => i !== index).map((s, idx) => ({ ...s, stepNumber: idx + 1 }));
    setSteps(updated);
  };

  const handleAddDocument = () => {
    if (!docName.trim()) return;
    const newDoc: CollegeRequiredDocument = {
      id: 0,
      processId: Number(processId) || 0,
      documentName: docName.trim(),
      description: docDesc.trim() || undefined,
      mandatory: docMandatory,
      acceptedFormats: docFormats.trim() || undefined,
      howToObtain: docHowToObtain.trim() || undefined,
    };
    setDocuments([...documents, newDoc]);
    setDocName('');
    setDocDesc('');
    setDocMandatory(true);
    setDocHowToObtain('');
  };

  const handleRemoveDocument = (index: number) => {
    setDocuments(documents.filter((_, i) => i !== index));
  };

  const handleAddUpdate = () => {
    if (!updateTitle.trim()) return;
    const newUp: CollegeProcessUpdate = {
      id: 0,
      processId: Number(processId) || 0,
      title: updateTitle.trim(),
      updateTitle: updateTitle.trim(),
      description: updateDesc.trim() || undefined,
      announcementDetails: updateDesc.trim() || undefined,
      updateType: updateType || 'GENERAL',
      effectiveDate: updateDate || undefined,
    };
    setUpdates([...updates, newUp]);
    setUpdateTitle('');
    setUpdateDesc('');
    setUpdateDate('');
  };

  const handleRemoveUpdate = (index: number) => {
    setUpdates(updates.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!collegeId) return;

    if (!title.trim()) {
      setError('Process title is required');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    let currentSteps = [...steps];
    if (stepTitle.trim()) {
      currentSteps.push({
        id: 0,
        processId: Number(processId) || 0,
        stepNumber: currentSteps.length + 1,
        title: stepTitle.trim(),
        description: stepDesc.trim() || undefined,
        estimatedTime: stepTime.trim() || undefined,
        notes: stepNotes.trim() || undefined,
      });
    }

    let currentDocs = [...documents];
    if (docName.trim()) {
      currentDocs.push({
        id: 0,
        processId: Number(processId) || 0,
        documentName: docName.trim(),
        description: docDesc.trim() || undefined,
        mandatory: docMandatory,
        acceptedFormats: docFormats.trim() || undefined,
        howToObtain: docHowToObtain.trim() || undefined,
      });
    }

    let currentUpdates = updates.map((u) => ({
      ...u,
      title: u.title || u.updateTitle || '',
      updateTitle: u.updateTitle || u.title || '',
      description: u.description || u.announcementDetails,
      announcementDetails: u.announcementDetails || u.description,
    }));
    if (updateTitle.trim()) {
      currentUpdates.push({
        id: 0,
        processId: Number(processId) || 0,
        title: updateTitle.trim(),
        updateTitle: updateTitle.trim(),
        description: updateDesc.trim() || undefined,
        announcementDetails: updateDesc.trim() || undefined,
        updateType: updateType || 'GENERAL',
        effectiveDate: updateDate || undefined,
      });
    }

    const payload: any = {
      title: title.trim(),
      description: description.trim() || undefined,
      category: category.trim(),
      eligibility: eligibility.trim() || undefined,
      eligibilityCriteria: eligibility.trim() || undefined,
      timeline: timeline.trim() || undefined,
      fees: fees.trim() || undefined,
      officialPortalUrl: officialPortalUrl.trim() || undefined,
      status: status,
      importantNotes: importantNotes.trim() || undefined,
      steps: currentSteps,
      requiredDocuments: currentDocs,
      updates: currentUpdates,
    };

    try {
      if (isEditing && processId) {
        await collegeService.updateProcess(collegeId, processId, payload);
      } else {
        await collegeService.createProcess(collegeId, payload);
      }
      navigate(`/colleges/${collegeId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save process. Admin privileges required.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Loading process editor..." />;
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Back button */}
      <Link
        to={`/colleges/${collegeId}`}
        className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Institution Profile
      </Link>

      <div>
        <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
          {isEditing ? 'Edit Institutional Process' : 'Add Institutional Process'}
        </h1>
        <p className="text-slate-600 mt-1 text-sm">
          Configure procedural steps, required certificates, fees, and guidelines for this institution.
        </p>
      </div>

      {error && <ErrorAlert message={error} />}

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Section 1: Basic Information */}
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm space-y-5">
          <h2 className="text-lg font-bold text-slate-900 flex items-center">
            <Layers className="w-5 h-5 mr-2 text-brand-600" /> Basic Process Information
          </h2>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Process Name / Title *
            </label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. B.Tech First Year Admissions Onboarding"
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
              >
                <option value="Admissions">Admissions</option>
                <option value="Hostel">Hostel & Accommodation</option>
                <option value="Scholarships">Scholarships & Financial Aid</option>
                <option value="Certificates">Certificates & NOC</option>
                <option value="Examinations">Examinations & Transcripts</option>
                <option value="Placement">Placement & Internships</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
              >
                <option value="ACTIVE">ACTIVE (Open)</option>
                <option value="UPCOMING">UPCOMING</option>
                <option value="CLOSED">CLOSED</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Process Description
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Overview of the procedure, department details, and requirements..."
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Timeline / Window
              </label>
              <input
                type="text"
                value={timeline}
                onChange={(e) => setTimeline(e.target.value)}
                placeholder="e.g. June 15 - July 30"
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Applicable Fees
              </label>
              <input
                type="text"
                value={fees}
                onChange={(e) => setFees(e.target.value)}
                placeholder="e.g. ₹500 or Free"
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Official Department Portal
              </label>
              <input
                type="text"
                value={officialPortalUrl}
                onChange={(e) => setOfficialPortalUrl(e.target.value)}
                placeholder="https://..."
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Eligibility Criteria
            </label>
            <textarea
              rows={2}
              value={eligibility}
              onChange={(e) => setEligibility(e.target.value)}
              placeholder="Candidate eligibility prerequisites, entrance ranks, or qualifying criteria..."
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Important Notes / Department Circulars
            </label>
            <textarea
              rows={2}
              value={importantNotes}
              onChange={(e) => setImportantNotes(e.target.value)}
              placeholder="Special instructions, penalty warnings, or physical reporting notes..."
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>
        </div>

        {/* Section 2: Step-by-Step Procedure */}
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900 flex items-center">
              <Clock className="w-5 h-5 mr-2 text-brand-600" /> Step-by-Step Procedure ({steps.length})
            </h2>
          </div>

          {/* Current Steps List */}
          {steps.length > 0 && (
            <div className="space-y-3">
              {steps.map((s, idx) => (
                <div key={idx} className="p-4 bg-slate-50 rounded-2xl border border-slate-200/70 flex items-start justify-between gap-3">
                  <div className="flex items-start space-x-3">
                    <div className="w-7 h-7 rounded-full bg-brand-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">
                      {s.stepNumber}
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-slate-900">{s.title}</h4>
                      {s.description && <p className="text-xs text-slate-600 mt-0.5">{s.description}</p>}
                      {s.estimatedTime && <p className="text-[11px] text-slate-400 mt-1 font-medium">Time: {s.estimatedTime}</p>}
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleRemoveStep(idx)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Add Step Card */}
          <div className="p-4 bg-slate-50/70 rounded-2xl border border-dashed border-slate-300 space-y-3">
            <p className="text-xs font-bold text-slate-700 uppercase tracking-wider">Add Step #{steps.length + 1}</p>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <input
                type="text"
                placeholder="Step Title (e.g. Document Verification Desk)"
                value={stepTitle}
                onChange={(e) => setStepTitle(e.target.value)}
                className="sm:col-span-2 px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
              <input
                type="text"
                placeholder="Time (e.g. 15 mins)"
                value={stepTime}
                onChange={(e) => setStepTime(e.target.value)}
                className="px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <textarea
              rows={2}
              placeholder="Step instructions..."
              value={stepDesc}
              onChange={(e) => setStepDesc(e.target.value)}
              className="w-full px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            <button
              type="button"
              onClick={handleAddStep}
              className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold flex items-center"
            >
              <Plus className="w-3.5 h-3.5 mr-1" /> Add Step to Process
            </button>
          </div>
        </div>

        {/* Section 3: Required Documents */}
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm space-y-5">
          <h2 className="text-lg font-bold text-slate-900 flex items-center">
            <FileCheck2 className="w-5 h-5 mr-2 text-brand-600" /> Required Documents & Missing Guidance ({documents.length})
          </h2>

          {/* Current Documents List */}
          {documents.length > 0 && (
            <div className="space-y-3">
              {documents.map((d, idx) => (
                <div key={idx} className="p-4 bg-slate-50 rounded-2xl border border-slate-200/70 flex items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center space-x-2">
                      <h4 className="text-sm font-bold text-slate-900">{d.documentName}</h4>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${d.mandatory ? 'bg-rose-100 text-rose-800' : 'bg-slate-200 text-slate-700'}`}>
                        {d.mandatory ? 'Mandatory' : 'Optional'}
                      </span>
                    </div>
                    {d.description && <p className="text-xs text-slate-600 mt-1">{d.description}</p>}
                    {d.howToObtain && (
                      <p className="text-xs text-brand-700 mt-1">
                        <strong>Missing Guide:</strong> {d.howToObtain}
                      </p>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => handleRemoveDocument(idx)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Add Document Card */}
          <div className="p-4 bg-slate-50/70 rounded-2xl border border-dashed border-slate-300 space-y-3">
            <p className="text-xs font-bold text-slate-700 uppercase tracking-wider">Add Document Requirement</p>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <input
                type="text"
                placeholder="Document Name (e.g. 12th Marks Sheet)"
                value={docName}
                onChange={(e) => setDocName(e.target.value)}
                className="sm:col-span-2 px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
              <select
                value={docMandatory ? 'true' : 'false'}
                onChange={(e) => setDocMandatory(e.target.value === 'true')}
                className="px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="true">Mandatory</option>
                <option value="false">Optional</option>
              </select>
            </div>
            <textarea
              rows={2}
              placeholder="How to obtain if missing (e.g. Download Digilocker copy or request from school board)..."
              value={docHowToObtain}
              onChange={(e) => setDocHowToObtain(e.target.value)}
              className="w-full px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            <button
              type="button"
              onClick={handleAddDocument}
              className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold flex items-center"
            >
              <Plus className="w-3.5 h-3.5 mr-1" /> Add Document
            </button>
          </div>
        </div>

        {/* Section 4: Process Updates & Circulars */}
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm space-y-5">
          <h2 className="text-lg font-bold text-slate-900 flex items-center">
            <Bell className="w-5 h-5 mr-2 text-brand-600" /> Process Circulars & Updates ({updates.length})
          </h2>

          {/* Current Updates List */}
          {updates.length > 0 && (
            <div className="space-y-3">
              {updates.map((u, idx) => (
                <div key={idx} className="p-4 bg-slate-50 rounded-2xl border border-slate-200/70 flex items-start justify-between gap-3">
                  <div>
                    <h4 className="text-sm font-bold text-slate-900">{u.updateTitle || u.title}</h4>
                    {(u.announcementDetails || u.description) && <p className="text-xs text-slate-600 mt-1">{u.announcementDetails || u.description}</p>}
                    {u.effectiveDate && <p className="text-[11px] text-slate-400 mt-1">Effective: {u.effectiveDate}</p>}
                  </div>
                  <button
                    type="button"
                    onClick={() => handleRemoveUpdate(idx)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Add Update Card */}
          <div className="p-4 bg-slate-50/70 rounded-2xl border border-dashed border-slate-300 space-y-3">
            <p className="text-xs font-bold text-slate-700 uppercase tracking-wider">Post Official Process Update</p>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <input
                type="text"
                placeholder="Update Title (e.g. Reporting date extended)"
                value={updateTitle}
                onChange={(e) => setUpdateTitle(e.target.value)}
                className="sm:col-span-2 px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
              <input
                type="date"
                value={updateDate}
                onChange={(e) => setUpdateDate(e.target.value)}
                className="px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <textarea
              rows={2}
              placeholder="Update announcement details..."
              value={updateDesc}
              onChange={(e) => setUpdateDesc(e.target.value)}
              className="w-full px-3.5 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
            <button
              type="button"
              onClick={handleAddUpdate}
              className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-xl text-xs font-semibold flex items-center"
            >
              <Plus className="w-3.5 h-3.5 mr-1" /> Add Notice / Circular
            </button>
          </div>
        </div>

        {/* Form Submit Actions */}
        <div className="flex items-center justify-end gap-3 pt-4">
          <Link
            to={`/colleges/${collegeId}`}
            className="px-6 py-3 rounded-xl text-sm font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 transition"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-8 py-3 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 transition shadow-md disabled:opacity-50"
          >
            {isSubmitting ? 'Saving Process...' : isEditing ? 'Save Changes' : 'Publish Process'}
          </button>
        </div>
      </form>
    </div>
  );
};
