import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { mentorApi } from '../services/api';
import { MentorRequest, MentorRequestStatus, MentorStatusUpdate } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  Users,
  Search,
  Filter,
  CheckCircle2,
  Clock,
  Phone,
  MessageSquare,
  Mail,
  UserCheck,
  BadgeCheck,
  RefreshCw,
  ExternalLink,
  ChevronRight,
  X,
  FileText,
  Calendar,
  AlertCircle,
  HelpCircle,
  Send,
} from 'lucide-react';

const PROCESS_FILTER_OPTIONS = [
  'ALL',
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

const STATUS_OPTIONS: MentorRequestStatus[] = ['NEW', 'CONTACTED', 'ASSIGNED', 'COMPLETED'];

export const AdminDashboardPage: React.FC = () => {
  const { user } = useAuth();

  const [requests, setRequests] = useState<MentorRequest[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [processFilter, setProcessFilter] = useState<string>('ALL');

  // Modal / Detail view
  const [selectedRequest, setSelectedRequest] = useState<MentorRequest | null>(null);
  const [newStatus, setNewStatus] = useState<MentorRequestStatus>('NEW');
  const [adminNotes, setAdminNotes] = useState<string>('');
  const [isUpdatingStatus, setIsUpdatingStatus] = useState<boolean>(false);
  const [updateSuccessMsg, setUpdateSuccessMsg] = useState<string | null>(null);

  const fetchRequests = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await mentorApi.getAllRequests(search, statusFilter, processFilter);
      setRequests(data);
    } catch (err: any) {
      console.error('Failed to load mentor requests', err);
      setError(err.response?.data?.message || 'Failed to load mentor requests from server. Admin permissions required.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, [statusFilter, processFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchRequests();
  };

  const handleOpenDetail = (req: MentorRequest) => {
    setSelectedRequest(req);
    setNewStatus(req.status);
    setAdminNotes(req.adminNotes || '');
    setUpdateSuccessMsg(null);
  };

  const handleCloseDetail = () => {
    setSelectedRequest(null);
    setUpdateSuccessMsg(null);
  };

  const handleStatusUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRequest) return;

    setIsUpdatingStatus(true);
    setUpdateSuccessMsg(null);
    try {
      const payload: MentorStatusUpdate = {
        status: newStatus,
        adminNotes: adminNotes.trim() ? adminNotes.trim() : undefined,
      };

      const updated = await mentorApi.updateStatus(selectedRequest.id, payload);

      // Update state locally
      setSelectedRequest(updated);
      setRequests((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
      setUpdateSuccessMsg(`Status updated successfully to ${updated.status}`);
      setTimeout(() => setUpdateSuccessMsg(null), 3500);
    } catch (err: any) {
      console.error('Failed to update status', err);
      alert(err.response?.data?.message || 'Failed to update request status');
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  // Status badge helper
  const renderStatusBadge = (status: MentorRequestStatus) => {
    switch (status) {
      case 'NEW':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800 border border-amber-200">
            <Clock className="w-3 h-3 mr-1" /> NEW
          </span>
        );
      case 'CONTACTED':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800 border border-blue-200">
            <MessageSquare className="w-3 h-3 mr-1" /> CONTACTED
          </span>
        );
      case 'ASSIGNED':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-purple-100 text-purple-800 border border-purple-200">
            <UserCheck className="w-3 h-3 mr-1" /> ASSIGNED
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-200">
            <BadgeCheck className="w-3 h-3 mr-1" /> COMPLETED
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-800">
            {status}
          </span>
        );
    }
  };

  // Quick Stats
  const totalCount = requests.length;
  const newCount = requests.filter((r) => r.status === 'NEW').length;
  const contactedCount = requests.filter((r) => r.status === 'CONTACTED').length;
  const assignedCount = requests.filter((r) => r.status === 'ASSIGNED').length;
  const completedCount = requests.filter((r) => r.status === 'COMPLETED').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-slate-100 text-slate-700 text-xs font-bold mb-2">
            <Users className="w-3.5 h-3.5 text-brand-600" />
            <span>Administrator Workspace</span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
            Mentor Requests Dashboard
          </h1>
          <p className="text-slate-600 mt-1 text-sm sm:text-base">
            Review incoming student assistance inquiries, coordinate outreach, and update guidance progress.
          </p>
        </div>

        <button
          onClick={fetchRequests}
          disabled={isLoading}
          className="inline-flex items-center px-4 py-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-sm font-semibold transition shadow-sm self-start sm:self-center"
        >
          <RefreshCw className={`w-4 h-4 mr-2 ${isLoading ? 'animate-spin text-brand-600' : ''}`} />
          Refresh Requests
        </button>
      </div>

      {/* KPI Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 sm:gap-4">
        <div className="p-4 rounded-2xl bg-white border border-slate-200/80 shadow-sm">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Total Filtered</p>
          <p className="text-2xl font-black text-slate-900 mt-1">{totalCount}</p>
        </div>
        <div className="p-4 rounded-2xl bg-amber-50/60 border border-amber-200/70 shadow-sm">
          <p className="text-xs font-bold uppercase tracking-wider text-amber-700">New Inquiries</p>
          <p className="text-2xl font-black text-amber-900 mt-1">{newCount}</p>
        </div>
        <div className="p-4 rounded-2xl bg-blue-50/60 border border-blue-200/70 shadow-sm">
          <p className="text-xs font-bold uppercase tracking-wider text-blue-700">Contacted</p>
          <p className="text-2xl font-black text-blue-900 mt-1">{contactedCount}</p>
        </div>
        <div className="p-4 rounded-2xl bg-purple-50/60 border border-purple-200/70 shadow-sm">
          <p className="text-xs font-bold uppercase tracking-wider text-purple-700">Assigned</p>
          <p className="text-2xl font-black text-purple-900 mt-1">{assignedCount}</p>
        </div>
        <div className="p-4 rounded-2xl bg-emerald-50/60 border border-emerald-200/70 shadow-sm col-span-2 sm:col-span-1">
          <p className="text-xs font-bold uppercase tracking-wider text-emerald-700">Completed</p>
          <p className="text-2xl font-black text-emerald-900 mt-1">{completedCount}</p>
        </div>
      </div>

      {/* Search and Filters Bar */}
      <div className="bg-white p-4 sm:p-5 rounded-3xl border border-slate-200/80 shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by student name, email, request ID (MNT-...), phone, or process..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:border-brand-500 focus:ring-brand-100 transition"
            />
          </div>
          <button
            type="submit"
            className="px-5 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-sm font-semibold transition"
          >
            Search
          </button>
        </form>

        <div className="flex flex-wrap items-center gap-3 pt-3 border-t border-slate-100 text-xs">
          <div className="flex items-center space-x-2">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <span className="font-bold text-slate-700">Status:</span>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {['ALL', 'NEW', 'CONTACTED', 'ASSIGNED', 'COMPLETED'].map((st) => (
              <button
                key={st}
                type="button"
                onClick={() => setStatusFilter(st)}
                className={`px-3 py-1 rounded-lg font-semibold transition ${
                  statusFilter === st
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {st}
              </button>
            ))}
          </div>

          <div className="ml-auto flex items-center space-x-2">
            <span className="font-bold text-slate-700">Process:</span>
            <select
              value={processFilter}
              onChange={(e) => setProcessFilter(e.target.value)}
              className="px-3 py-1.5 rounded-lg border border-slate-300 bg-white text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-100"
            >
              {PROCESS_FILTER_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {error && <ErrorAlert message={error} />}

      {/* Requests Table / Cards */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="py-20 flex justify-center">
            <LoadingSpinner message="Loading mentor requests..." />
          </div>
        ) : requests.length === 0 ? (
          <div className="py-16 text-center space-y-3">
            <Users className="w-12 h-12 text-slate-300 mx-auto" />
            <p className="text-base font-bold text-slate-700">No Mentor Requests Found</p>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              There are currently no requests matching your search query or status filter.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50/80 border-b border-slate-200/80 text-[11px] uppercase tracking-wider text-slate-500 font-bold">
                <tr>
                  <th className="px-5 py-4">Request ID</th>
                  <th className="px-5 py-4">Student</th>
                  <th className="px-5 py-4">Department / Process</th>
                  <th className="px-5 py-4">Contact Method</th>
                  <th className="px-5 py-4">Submitted</th>
                  <th className="px-5 py-4">Status</th>
                  <th className="px-5 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {requests.map((req) => (
                  <tr
                    key={req.id}
                    className="hover:bg-slate-50/80 transition cursor-pointer"
                    onClick={() => handleOpenDetail(req)}
                  >
                    <td className="px-5 py-4 font-mono font-bold text-slate-900">
                      <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-800">
                        {req.requestId}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <p className="font-bold text-slate-900 text-sm">{req.fullName}</p>
                      <p className="text-slate-500 text-[11px]">{req.email}</p>
                    </td>
                    <td className="px-5 py-4 font-semibold text-slate-800 max-w-[200px] truncate">
                      {req.departmentOrProcess}
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center space-x-1.5 font-medium text-slate-700">
                        {req.preferredContactMethod.toLowerCase().includes('phone') ? (
                          <Phone className="w-3.5 h-3.5 text-blue-600" />
                        ) : req.preferredContactMethod.toLowerCase().includes('whatsapp') ? (
                          <MessageSquare className="w-3.5 h-3.5 text-emerald-600" />
                        ) : (
                          <Mail className="w-3.5 h-3.5 text-amber-600" />
                        )}
                        <span>{req.preferredContactMethod}</span>
                      </div>
                      {req.phoneNumber && (
                        <p className="text-slate-500 text-[11px] font-mono mt-0.5">{req.phoneNumber}</p>
                      )}
                    </td>
                    <td className="px-5 py-4 text-slate-500">
                      {new Date(req.createdAt).toLocaleDateString(undefined, {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                      })}
                    </td>
                    <td className="px-5 py-4">{renderStatusBadge(req.status)}</td>
                    <td className="px-5 py-4 text-right">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleOpenDetail(req);
                        }}
                        className="inline-flex items-center px-3 py-1.5 rounded-lg bg-brand-50 hover:bg-brand-100 text-brand-700 font-semibold text-xs transition"
                      >
                        Review
                        <ChevronRight className="w-3.5 h-3.5 ml-1" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Detailed Modal / Drawer */}
      {selectedRequest && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl space-y-6 relative max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="flex items-start justify-between border-b border-slate-100 pb-4">
              <div>
                <div className="flex items-center space-x-2">
                  <span className="font-mono text-sm font-black px-2.5 py-1 bg-brand-50 text-brand-700 rounded-lg">
                    {selectedRequest.requestId}
                  </span>
                  {renderStatusBadge(selectedRequest.status)}
                </div>
                <h2 className="text-xl font-bold text-slate-950 mt-2">
                  {selectedRequest.departmentOrProcess}
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Inquiry submitted on{' '}
                  {new Date(selectedRequest.createdAt).toLocaleString(undefined, {
                    dateStyle: 'medium',
                    timeStyle: 'short',
                  })}
                </p>
              </div>

              <button
                type="button"
                onClick={handleCloseDetail}
                className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Student Contact Info */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 bg-slate-50 p-4 rounded-2xl border border-slate-200/80 text-xs">
              <div>
                <span className="font-semibold text-slate-500 uppercase tracking-wider text-[10px]">
                  Applicant Name
                </span>
                <p className="text-sm font-bold text-slate-900 mt-0.5">{selectedRequest.fullName}</p>
                <p className="text-slate-600 mt-0.5 flex items-center">
                  <Mail className="w-3 h-3 mr-1 text-slate-400" />
                  <a href={`mailto:${selectedRequest.email}`} className="text-brand-600 hover:underline">
                    {selectedRequest.email}
                  </a>
                </p>
              </div>

              <div>
                <span className="font-semibold text-slate-500 uppercase tracking-wider text-[10px]">
                  Preferred Outreach
                </span>
                <p className="text-sm font-bold text-slate-900 mt-0.5 flex items-center">
                  {selectedRequest.preferredContactMethod}
                </p>
                {selectedRequest.phoneNumber ? (
                  <p className="text-slate-600 mt-0.5 flex items-center font-mono">
                    <Phone className="w-3 h-3 mr-1 text-emerald-600" />
                    <a href={`tel:${selectedRequest.phoneNumber}`} className="text-slate-800 hover:underline">
                      {selectedRequest.phoneNumber}
                    </a>
                  </p>
                ) : (
                  <p className="text-slate-400 mt-0.5 italic">No phone number provided (Email outreach)</p>
                )}
              </div>
            </div>

            {/* Problem & Desired Outcome Details */}
            <div className="space-y-4 text-xs">
              <div className="space-y-1">
                <h4 className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                  Problem Encountered
                </h4>
                <div className="p-3.5 rounded-xl bg-slate-50/70 border border-slate-200 text-slate-700 whitespace-pre-wrap leading-relaxed">
                  {selectedRequest.problemDescription}
                </div>
              </div>

              <div className="space-y-1">
                <h4 className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                  Desired Outcome
                </h4>
                <div className="p-3.5 rounded-xl bg-slate-50/70 border border-slate-200 text-slate-700 whitespace-pre-wrap leading-relaxed">
                  {selectedRequest.desiredOutcome}
                </div>
              </div>

              {selectedRequest.additionalInfo && (
                <div className="space-y-1">
                  <h4 className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                    Additional Details
                  </h4>
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-600 whitespace-pre-wrap">
                    {selectedRequest.additionalInfo}
                  </div>
                </div>
              )}
            </div>

            {/* Admin Status & Notes Update Form */}
            <form onSubmit={handleStatusUpdate} className="space-y-4 pt-4 border-t border-slate-200">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 flex items-center">
                <BadgeCheck className="w-4 h-4 mr-1 text-brand-600" />
                Update Request Status & Coordinator Notes
              </h4>

              {updateSuccessMsg && (
                <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center animate-fade-in">
                  <CheckCircle2 className="w-4 h-4 mr-2 text-emerald-600 shrink-0" />
                  {updateSuccessMsg}
                </div>
              )}

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {STATUS_OPTIONS.map((st) => (
                  <button
                    key={st}
                    type="button"
                    onClick={() => setNewStatus(st)}
                    className={`py-2 px-3 rounded-xl text-xs font-bold transition border ${
                      newStatus === st
                        ? 'bg-slate-900 text-white border-slate-900 shadow-sm'
                        : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100'
                    }`}
                  >
                    {st}
                  </button>
                ))}
              </div>

              <div>
                <label className="block text-[11px] font-bold uppercase tracking-wider text-slate-600 mb-1">
                  Internal Coordinator Notes
                </label>
                <textarea
                  rows={3}
                  value={adminNotes}
                  onChange={(e) => setAdminNotes(e.target.value)}
                  placeholder="e.g. Contacted applicant on 28th Oct via WhatsApp; confirmed passport tatkaal documents; assigned mentor..."
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:border-brand-500 focus:ring-brand-100"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-2">
                <button
                  type="button"
                  onClick={handleCloseDetail}
                  className="px-4 py-2 rounded-xl border border-slate-300 text-slate-700 text-xs font-semibold hover:bg-slate-50 transition"
                >
                  Close
                </button>
                <button
                  type="submit"
                  disabled={isUpdatingStatus}
                  className="px-5 py-2 rounded-xl bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold shadow-sm transition disabled:opacity-50"
                >
                  {isUpdatingStatus ? 'Saving Changes...' : 'Save Status & Notes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default AdminDashboardPage;
