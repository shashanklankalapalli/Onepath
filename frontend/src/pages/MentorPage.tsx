import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { mentorApi } from '../services/api';
import { MentorRequest, MentorRequestCreate } from '../types';
import { ErrorAlert } from '../components/ErrorAlert';
import { LoadingSpinner } from '../components/LoadingSpinner';
import {
  Sparkles,
  CheckCircle2,
  AlertCircle,
  Phone,
  MessageSquare,
  Mail,
  HelpCircle,
  Clock,
  ShieldAlert,
  ArrowRight,
  Send,
  UserCheck,
  FileText,
  BadgeCheck,
  RefreshCw,
  Copy,
  Check,
} from 'lucide-react';

const PROCESS_OPTIONS = [
  'JoSAA / College Admissions',
  'Passport',
  'Driving Licence',
  'Government Certificates',
  'Marriage Registration',
  'Aadhaar',
  'PAN Card',
  'Property / Land',
  'Voter ID',
  'College Documentation',
  'Scholarships',
  'Other',
];

export const MentorPage: React.FC = () => {
  const { user, isAuthenticated } = useAuth();

  // Form State
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [selectedProcess, setSelectedProcess] = useState('');
  const [customProcess, setCustomProcess] = useState('');
  const [problemDescription, setProblemDescription] = useState('');
  const [desiredOutcome, setDesiredOutcome] = useState('');
  const [contactMethod, setContactMethod] = useState<'Phone call' | 'WhatsApp' | 'Email'>('WhatsApp');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [additionalInfo, setAdditionalInfo] = useState('');

  // UI State
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [submittedRequest, setSubmittedRequest] = useState<MentorRequest | null>(null);
  const [copiedId, setCopiedId] = useState(false);

  // Authenticated User's Request History
  const [myRequests, setMyRequests] = useState<MentorRequest[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [activeTab, setActiveTab] = useState<'form' | 'history'>('form');

  // Pre-fill user details if logged in
  useEffect(() => {
    if (user) {
      if (!fullName) setFullName(user.name);
      if (!email) setEmail(user.email);
    }
  }, [user]);

  // Load history if user is logged in
  const loadMyRequests = async () => {
    if (!isAuthenticated) return;
    setLoadingHistory(true);
    try {
      const data = await mentorApi.getMyRequests();
      setMyRequests(data);
    } catch (err) {
      console.error('Failed to load user mentor requests', err);
    } finally {
      setLoadingHistory(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      loadMyRequests();
    }
  }, [isAuthenticated]);

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!fullName.trim()) {
      errors.fullName = 'Full name is required';
    }

    if (!email.trim()) {
      errors.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = 'Please enter a valid email address';
    }

    if (!selectedProcess) {
      errors.process = 'Please select a department or process';
    } else if (selectedProcess === 'Other' && !customProcess.trim()) {
      errors.customProcess = 'Please specify your process or department';
    }

    if (!problemDescription.trim()) {
      errors.problemDescription = 'Please describe the problem or obstacle you are facing';
    } else if (problemDescription.trim().length < 15) {
      errors.problemDescription = 'Please provide a bit more detail (minimum 15 characters)';
    }

    if (!desiredOutcome.trim()) {
      errors.desiredOutcome = 'Please describe what outcome you would like help achieving';
    }

    if (contactMethod === 'Phone call' || contactMethod === 'WhatsApp') {
      const cleanedPhone = phoneNumber.replace(/[\s-]/g, '');
      if (!phoneNumber.trim()) {
        errors.phoneNumber = `Phone number is required for ${contactMethod}`;
      } else if (cleanedPhone.length < 10) {
        errors.phoneNumber = 'Please enter a valid phone number (at least 10 digits)';
      }
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    const finalProcess = selectedProcess === 'Other'
      ? `Other: ${customProcess.trim()}`
      : selectedProcess;

    const payload: MentorRequestCreate = {
      fullName: fullName.trim(),
      email: email.trim(),
      departmentOrProcess: finalProcess,
      problemDescription: problemDescription.trim(),
      desiredOutcome: desiredOutcome.trim(),
      preferredContactMethod: contactMethod,
      phoneNumber: (contactMethod !== 'Email' && phoneNumber.trim()) ? phoneNumber.trim() : undefined,
      additionalInfo: additionalInfo.trim() ? additionalInfo.trim() : undefined,
    };

    try {
      const response = await mentorApi.submitRequest(payload);
      setSubmittedRequest(response);
      // Refresh user history
      if (isAuthenticated) {
        loadMyRequests();
      }
    } catch (err: any) {
      console.error('Failed to submit mentor request', err);
      const serverMessage = err.response?.data?.message || err.message || 'Failed to submit request. Please verify your connection.';
      setError(serverMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setSubmittedRequest(null);
    setProblemDescription('');
    setDesiredOutcome('');
    setAdditionalInfo('');
    setSelectedProcess('');
    setCustomProcess('');
    setValidationErrors({});
    setError(null);
  };

  const handleCopyId = (id: string) => {
    navigator.clipboard.writeText(id);
    setCopiedId(true);
    setTimeout(() => setCopiedId(false), 2000);
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'NEW':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">
            <Clock className="w-3 h-3 mr-1" /> Request Received
          </span>
        );
      case 'CONTACTED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-200">
            <MessageSquare className="w-3 h-3 mr-1" /> Contacted
          </span>
        );
      case 'ASSIGNED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-100 text-purple-800 border border-purple-200">
            <UserCheck className="w-3 h-3 mr-1" /> Mentor Assigned
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
            <BadgeCheck className="w-3 h-3 mr-1" /> Completed
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-800 border border-slate-200">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Hero Header */}
      <div className="text-center space-y-4 max-w-3xl mx-auto">
        <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold tracking-wide uppercase shadow-sm">
          <Sparkles className="w-3.5 h-3.5 text-brand-600 animate-pulse" />
          <span>Personalized 1-on-1 Guidance</span>
        </div>

        <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black text-slate-950 tracking-tight leading-tight">
          Confused with a process? <br />
          <span className="bg-gradient-to-r from-brand-600 via-indigo-600 to-brand-700 bg-clip-text text-transparent">
            Hire your mentor now!
          </span>
        </h1>

        <p className="text-slate-600 text-base sm:text-lg leading-relaxed">
          Request personalized, dedicated guidance for complicated government, civic, and college-related procedures.
          Our experienced coordinators review your exact situation, help clarify documentation requirements, and assist you step-by-step.
        </p>

        {/* Tab switch for authenticated users with history */}
        {isAuthenticated && myRequests.length > 0 && (
          <div className="flex justify-center pt-2">
            <div className="inline-flex p-1 bg-slate-100 rounded-xl border border-slate-200 text-xs font-semibold">
              <button
                type="button"
                onClick={() => setActiveTab('form')}
                className={`px-4 py-2 rounded-lg transition ${
                  activeTab === 'form'
                    ? 'bg-white text-brand-700 shadow-sm'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Request Assistance
              </button>
              <button
                type="button"
                onClick={() => setActiveTab('history')}
                className={`px-4 py-2 rounded-lg transition flex items-center space-x-1.5 ${
                  activeTab === 'history'
                    ? 'bg-white text-brand-700 shadow-sm'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <span>My Requests</span>
                <span className="px-1.5 py-0.2 rounded-full bg-brand-100 text-brand-800 text-[10px] font-bold">
                  {myRequests.length}
                </span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Transparency Banner */}
      <div className="bg-gradient-to-r from-amber-500/10 via-amber-400/5 to-amber-500/10 border border-amber-300/80 rounded-2xl p-5 shadow-sm text-amber-950">
        <div className="flex items-start space-x-3.5">
          <div className="w-9 h-9 rounded-xl bg-amber-500/20 text-amber-700 flex items-center justify-center shrink-0 mt-0.5">
            <ShieldAlert className="w-5 h-5 text-amber-700" />
          </div>
          <div className="space-y-1 text-sm">
            <p className="font-bold text-amber-900 leading-snug">
              Mentor assistance is a paid OnePath service. Pricing will be confirmed based on your requirement.
            </p>
            <p className="text-amber-800/90 text-xs sm:text-sm leading-relaxed">
              Submitting this request assesses your case. There is no automated upfront charge. Our representative will contact you via your preferred communication method with custom timeline, scope, and service confirmation.
            </p>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      {activeTab === 'history' && isAuthenticated ? (
        /* User Request History View */
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm space-y-6">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <div>
              <h2 className="text-xl font-bold text-slate-900">My Mentor Requests</h2>
              <p className="text-xs text-slate-500">Track the status and progress of your guidance requests</p>
            </div>
            <button
              onClick={loadMyRequests}
              disabled={loadingHistory}
              className="inline-flex items-center px-3 py-1.5 text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition"
            >
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loadingHistory ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>

          {loadingHistory ? (
            <div className="py-12 flex justify-center">
              <LoadingSpinner message="Loading your requests..." />
            </div>
          ) : myRequests.length === 0 ? (
            <div className="py-12 text-center text-slate-500 text-sm">
              You haven't submitted any mentor requests yet.
            </div>
          ) : (
            <div className="space-y-4">
              {myRequests.map((req) => (
                <div
                  key={req.id}
                  className="p-5 rounded-2xl border border-slate-200/90 hover:border-brand-300 bg-slate-50/50 hover:bg-white transition space-y-3"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                    <div className="flex items-center space-x-2.5">
                      <span className="font-mono text-xs font-bold px-2.5 py-1 bg-slate-200 text-slate-800 rounded-lg">
                        {req.requestId}
                      </span>
                      <h3 className="font-bold text-slate-900 text-sm">{req.departmentOrProcess}</h3>
                    </div>
                    <div>{getStatusBadge(req.status)}</div>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-600">
                    <div>
                      <span className="font-semibold text-slate-700">Problem: </span>
                      <span className="line-clamp-2">{req.problemDescription}</span>
                    </div>
                    <div>
                      <span className="font-semibold text-slate-700">Desired Outcome: </span>
                      <span className="line-clamp-2">{req.desiredOutcome}</span>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-200/60 text-[11px] text-slate-500">
                    <div>
                      Preferred Contact: <strong className="text-slate-700">{req.preferredContactMethod}</strong>
                      {req.phoneNumber && ` (${req.phoneNumber})`}
                    </div>
                    <div>
                      Submitted on: {new Date(req.createdAt).toLocaleDateString(undefined, {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : submittedRequest ? (
        /* Submission Success View */
        <div className="bg-white rounded-3xl border border-emerald-200 p-8 sm:p-12 shadow-md space-y-8 text-center max-w-2xl mx-auto animate-fade-in">
          <div className="w-16 h-16 rounded-2xl bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto shadow-sm">
            <CheckCircle2 className="w-10 h-10 text-emerald-600" />
          </div>

          <div className="space-y-2">
            <h2 className="text-2xl sm:text-3xl font-black text-slate-950">
              Request Received Successfully!
            </h2>
            <p className="text-slate-600 text-sm sm:text-base">
              Thank you, <strong>{submittedRequest.fullName}</strong>. Your mentor assistance inquiry has been registered in the OnePath system.
            </p>
          </div>

          {/* Request ID Display Card */}
          <div className="bg-slate-50 border-2 border-dashed border-slate-300 rounded-2xl p-6 space-y-2">
            <p className="text-xs uppercase font-bold text-slate-400 tracking-wider">Your Unique Request ID</p>
            <div className="flex items-center justify-center space-x-3">
              <span className="text-2xl sm:text-3xl font-mono font-black text-brand-700 tracking-wider">
                {submittedRequest.requestId}
              </span>
              <button
                type="button"
                onClick={() => handleCopyId(submittedRequest.requestId)}
                title="Copy Request ID"
                className="p-2 rounded-xl bg-white border border-slate-200 hover:bg-slate-100 text-slate-600 transition shadow-sm"
              >
                {copiedId ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
            <p className="text-xs text-slate-500">
              Please save this ID for any follow-up inquiries.
            </p>
          </div>

          {/* Details Summary */}
          <div className="bg-slate-50/70 rounded-2xl p-5 border border-slate-200/70 text-left space-y-2.5 text-xs text-slate-600">
            <div className="flex justify-between py-1 border-b border-slate-200/50">
              <span className="font-semibold text-slate-700">Department / Process:</span>
              <span className="text-slate-900 font-medium">{submittedRequest.departmentOrProcess}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-200/50">
              <span className="font-semibold text-slate-700">Preferred Contact:</span>
              <span className="text-slate-900 font-medium">
                {submittedRequest.preferredContactMethod} {submittedRequest.phoneNumber ? `(${submittedRequest.phoneNumber})` : ''}
              </span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-200/50">
              <span className="font-semibold text-slate-700">Email Address:</span>
              <span className="text-slate-900 font-medium">{submittedRequest.email}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="font-semibold text-slate-700">Initial Status:</span>
              <span>{getStatusBadge(submittedRequest.status)}</span>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-blue-50/70 border border-blue-200 text-xs text-blue-900 text-left flex items-start space-x-2.5">
            <HelpCircle className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
            <div>
              <strong>What happens next?</strong> Our coordinator will examine the procedure requirements for{' '}
              <em>{submittedRequest.departmentOrProcess}</em> and contact you via{' '}
              <strong>{submittedRequest.preferredContactMethod}</strong> to confirm pricing, verify required documentation, and assign a dedicated mentor.
            </div>
          </div>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
            <button
              onClick={resetForm}
              className="w-full sm:w-auto px-6 py-2.5 rounded-xl font-semibold text-sm text-slate-700 bg-slate-100 hover:bg-slate-200 transition"
            >
              Submit Another Request
            </button>
            {isAuthenticated && (
              <button
                onClick={() => setActiveTab('history')}
                className="w-full sm:w-auto px-6 py-2.5 rounded-xl font-semibold text-sm text-white bg-brand-600 hover:bg-brand-700 transition shadow-sm"
              >
                View All My Requests
              </button>
            )}
            <Link
              to="/"
              className="w-full sm:w-auto px-6 py-2.5 rounded-xl font-semibold text-sm text-slate-700 border border-slate-300 hover:bg-slate-50 transition"
            >
              Back to Home
            </Link>
          </div>
        </div>
      ) : (
        /* Submission Form */
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-10 shadow-sm space-y-8">
          <div>
            <h2 className="text-xl sm:text-2xl font-bold text-slate-950">
              Personalized Guidance Request Form
            </h2>
            <p className="text-slate-500 text-xs sm:text-sm mt-1">
              Please fill out all required details so our specialist team can review your specific procedural hurdle.
            </p>
          </div>

          {error && <ErrorAlert message={error} />}

          <form onSubmit={handleSubmit} className="space-y-6" noValidate>
            {/* Name and Email */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Full Name <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  value={fullName}
                  onChange={(e) => {
                    setFullName(e.target.value);
                    if (validationErrors.fullName) {
                      setValidationErrors({ ...validationErrors, fullName: '' });
                    }
                  }}
                  placeholder="e.g. Shashank Lankalapalli"
                  className={`w-full px-4 py-2.5 rounded-xl border text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition ${
                    validationErrors.fullName
                      ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                      : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                  }`}
                />
                {validationErrors.fullName && (
                  <p className="mt-1 text-xs text-rose-500 flex items-center">
                    <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.fullName}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Email Address <span className="text-rose-500">*</span>
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (validationErrors.email) {
                      setValidationErrors({ ...validationErrors, email: '' });
                    }
                  }}
                  placeholder="e.g. student@onepath.org"
                  className={`w-full px-4 py-2.5 rounded-xl border text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition ${
                    validationErrors.email
                      ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                      : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                  }`}
                />
                {validationErrors.email && (
                  <p className="mt-1 text-xs text-rose-500 flex items-center">
                    <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.email}
                  </p>
                )}
              </div>
            </div>

            {/* Department / Process Selection */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Department or Process <span className="text-rose-500">*</span>
              </label>
              <select
                value={selectedProcess}
                onChange={(e) => {
                  setSelectedProcess(e.target.value);
                  if (validationErrors.process) {
                    setValidationErrors({ ...validationErrors, process: '' });
                  }
                }}
                className={`w-full px-4 py-2.5 rounded-xl border text-sm text-slate-900 focus:outline-none focus:ring-2 transition bg-white ${
                  validationErrors.process
                    ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                    : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                }`}
              >
                <option value="">Select a department or process...</option>
                {PROCESS_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>
                    {opt}
                  </option>
                ))}
              </select>
              {validationErrors.process && (
                <p className="mt-1 text-xs text-rose-500 flex items-center">
                  <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.process}
                </p>
              )}

              {/* Conditional "Other" Process Input */}
              {selectedProcess === 'Other' && (
                <div className="mt-3 animate-fade-in">
                  <label className="block text-xs font-medium text-slate-600 mb-1">
                    Please specify the custom process or department <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={customProcess}
                    onChange={(e) => {
                      setCustomProcess(e.target.value);
                      if (validationErrors.customProcess) {
                        setValidationErrors({ ...validationErrors, customProcess: '' });
                      }
                    }}
                    placeholder="e.g. Visa Attestation, Ration Card Bifurcation, Specific University Lateral Entry"
                    className={`w-full px-4 py-2.5 rounded-xl border text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition ${
                      validationErrors.customProcess
                        ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                        : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                    }`}
                  />
                  {validationErrors.customProcess && (
                    <p className="mt-1 text-xs text-rose-500 flex items-center">
                      <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.customProcess}
                    </p>
                  )}
                </div>
              )}
            </div>

            {/* Problem Description */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Problem Description <span className="text-rose-500">*</span>
              </label>
              <textarea
                rows={3}
                value={problemDescription}
                onChange={(e) => {
                  setProblemDescription(e.target.value);
                  if (validationErrors.problemDescription) {
                    setValidationErrors({ ...validationErrors, problemDescription: '' });
                  }
                }}
                placeholder="Describe where you are stuck, conflicting instructions you encountered, or specific document issues..."
                className={`w-full px-4 py-2.5 rounded-xl border text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition ${
                  validationErrors.problemDescription
                    ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                    : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                }`}
              />
              {validationErrors.problemDescription && (
                <p className="mt-1 text-xs text-rose-500 flex items-center">
                  <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.problemDescription}
                </p>
              )}
            </div>

            {/* Desired Outcome */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Desired Outcome <span className="text-rose-500">*</span>
              </label>
              <textarea
                rows={2}
                value={desiredOutcome}
                onChange={(e) => {
                  setDesiredOutcome(e.target.value);
                  if (validationErrors.desiredOutcome) {
                    setValidationErrors({ ...validationErrors, desiredOutcome: '' });
                  }
                }}
                placeholder="What exactly do you want to accomplish? (e.g. Obtain tatkaal passport before my internship, complete physical reporting without missing documents...)"
                className={`w-full px-4 py-2.5 rounded-xl border text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition ${
                  validationErrors.desiredOutcome
                    ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                    : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                }`}
              />
              {validationErrors.desiredOutcome && (
                <p className="mt-1 text-xs text-rose-500 flex items-center">
                  <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.desiredOutcome}
                </p>
              )}
            </div>

            {/* Preferred Contact Method */}
            <div className="space-y-2">
              <label className="block text-xs font-semibold text-slate-700">
                Preferred Contact Method <span className="text-rose-500">*</span>
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {[
                  { id: 'WhatsApp', label: 'WhatsApp', icon: MessageSquare, desc: 'Chat & voice notes' },
                  { id: 'Phone call', label: 'Phone call', icon: Phone, desc: 'Direct voice conversation' },
                  { id: 'Email', label: 'Email', icon: Mail, desc: 'Written guidance via email' },
                ].map((item) => {
                  const Icon = item.icon;
                  const isSelected = contactMethod === item.id;
                  return (
                    <label
                      key={item.id}
                      className={`relative flex items-center space-x-3 p-3.5 rounded-2xl border cursor-pointer transition ${
                        isSelected
                          ? 'border-brand-600 bg-brand-50/50 ring-2 ring-brand-100'
                          : 'border-slate-200 hover:border-slate-300 bg-white'
                      }`}
                    >
                      <input
                        type="radio"
                        name="contactMethod"
                        value={item.id}
                        checked={isSelected}
                        onChange={() => {
                          setContactMethod(item.id as any);
                          if (item.id === 'Email' && validationErrors.phoneNumber) {
                            const { phoneNumber, ...rest } = validationErrors;
                            setValidationErrors(rest);
                          }
                        }}
                        className="sr-only"
                      />
                      <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                        isSelected ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-600'
                      }`}>
                        <Icon className="w-4 h-4" />
                      </div>
                      <div className="text-left">
                        <div className={`text-xs font-bold ${isSelected ? 'text-brand-900' : 'text-slate-900'}`}>
                          {item.label}
                        </div>
                        <div className="text-[10px] text-slate-500">{item.desc}</div>
                      </div>
                    </label>
                  );
                })}
              </div>
            </div>

            {/* Phone Number (conditional or required based on contact method) */}
            {(contactMethod === 'Phone call' || contactMethod === 'WhatsApp') && (
              <div className="animate-fade-in">
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Phone Number ({contactMethod === 'WhatsApp' ? 'WhatsApp Number' : 'Calling Number'}){' '}
                  <span className="text-rose-500">*</span>
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <Phone className="w-4 h-4" />
                  </div>
                  <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => {
                      setPhoneNumber(e.target.value);
                      if (validationErrors.phoneNumber) {
                        setValidationErrors({ ...validationErrors, phoneNumber: '' });
                      }
                    }}
                    placeholder="e.g. 9876543210"
                    className={`w-full pl-10 pr-4 py-2.5 rounded-xl border text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition ${
                      validationErrors.phoneNumber
                        ? 'border-rose-400 focus:ring-rose-200 bg-rose-50/20'
                        : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
                    }`}
                  />
                </div>
                {validationErrors.phoneNumber && (
                  <p className="mt-1 text-xs text-rose-500 flex items-center">
                    <AlertCircle className="w-3 h-3 mr-1" /> {validationErrors.phoneNumber}
                  </p>
                )}
              </div>
            )}

            {/* Optional Additional Info */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Additional Information <span className="text-slate-400 font-normal">(Optional)</span>
              </label>
              <textarea
                rows={2}
                value={additionalInfo}
                onChange={(e) => setAdditionalInfo(e.target.value)}
                placeholder="Any relevant deadlines, physical city location, or existing document numbers..."
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:border-brand-500 focus:ring-brand-100 transition"
              />
            </div>

            {/* Submit Action */}
            <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4">
              <p className="text-xs text-slate-500">
                By submitting, you agree to receive follow-up contact regarding this service request.
              </p>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full sm:w-auto inline-flex items-center justify-center px-8 py-3 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-brand-600 to-indigo-600 hover:from-brand-700 hover:to-indigo-700 disabled:opacity-50 transition shadow-md shadow-brand-500/25 cursor-pointer"
              >
                {isSubmitting ? (
                  <>
                    <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                    Submitting Request...
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4 mr-2" />
                    Submit Mentor Request
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
export default MentorPage;
