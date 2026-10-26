import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { collegeService } from '../services/api';
import { College, CollegeProcessSummary } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import { useAuth } from '../context/AuthContext';
import {
  ArrowLeft,
  GraduationCap,
  MapPin,
  Globe,
  ArrowRight,
  Clock,
  Banknote,
  Layers,
  PlusCircle,
  Edit3,
  Trash2,
  ExternalLink,
} from 'lucide-react';

export const CollegeDetailPage: React.FC = () => {
  const { collegeId } = useParams<{ collegeId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [college, setCollege] = useState<College | null>(null);
  const [processes, setProcesses] = useState<CollegeProcessSummary[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const isSuperAdmin = user?.role === 'SUPER_ADMIN';
  const isCollegeAdmin = user?.role === 'COLLEGE_ADMIN' && (!user.assignedCollegeId || user.assignedCollegeId.toString() === collegeId);
  const canManage = isSuperAdmin || isCollegeAdmin;

  const fetchCollegeDetails = async () => {
    if (!collegeId) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await collegeService.getById(collegeId);
      setCollege(data);
      const procList = await collegeService.getProcesses(
        collegeId,
        selectedCategory === 'All' ? undefined : selectedCategory
      );
      setProcesses(procList);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load college processes.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCollegeDetails();
  }, [collegeId, selectedCategory]);

  const handleDeleteProcess = async (processId: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!collegeId) return;
    if (!window.confirm('Are you sure you want to delete this process and all its steps/documents?')) {
      return;
    }

    setDeletingId(processId);
    try {
      await collegeService.deleteProcess(collegeId, processId);
      setProcesses(processes.filter((p) => p.id !== processId));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete process.');
    } finally {
      setDeletingId(null);
    }
  };

  const handleDeleteCollege = async () => {
    if (!collegeId) return;
    if (!window.confirm(`Are you sure you want to delete ${college?.name}? This will remove all associated processes.`)) {
      return;
    }

    try {
      await collegeService.deleteCollege(collegeId);
      navigate('/colleges');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete college.');
    }
  };

  if (isLoading) {
    return <LoadingSpinner message="Fetching college documentation and processes..." />;
  }

  if (error || !college) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <ErrorAlert message={error || 'College not found'} onRetry={fetchCollegeDetails} />
      </div>
    );
  }

  const categories = ['All', 'Admissions', 'Hostel', 'Scholarships', 'Certificates', 'Examinations', 'Placement'];

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Back button */}
      <Link
        to="/colleges"
        className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Institutions
      </Link>

      {/* College Info Card */}
      <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div className="flex items-start space-x-4">
            <div className="w-16 h-16 rounded-2xl bg-brand-50 text-brand-700 flex items-center justify-center font-black text-xl border border-brand-200 flex-shrink-0">
              {college.shortName || college.name.substring(0, 2).toUpperCase()}
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-2xl sm:text-3xl font-black text-slate-950 tracking-tight">
                  {college.name}
                </h1>
                {college.shortName && (
                  <span className="text-xs font-bold px-2.5 py-1 bg-brand-50 text-brand-700 border border-brand-200 rounded-lg">
                    {college.shortName}
                  </span>
                )}
              </div>
              <div className="flex flex-wrap items-center gap-4 mt-2 text-xs text-slate-500">
                {college.location && (
                  <span className="flex items-center">
                    <MapPin className="w-3.5 h-3.5 mr-1 text-slate-400" />
                    {college.location}
                  </span>
                )}
                {college.website && (
                  <a
                    href={college.website.startsWith('http') ? college.website : `https://${college.website}`}
                    target="_blank"
                    rel="noreferrer"
                    className="flex items-center text-brand-600 hover:underline"
                  >
                    <Globe className="w-3.5 h-3.5 mr-1" />
                    Official Website
                  </a>
                )}
              </div>
            </div>
          </div>

          {/* Admin Management Action Buttons */}
          {canManage && (
            <div className="flex items-center gap-2 self-start">
              <Link
                to={`/colleges/${college.id}/processes/new`}
                className="inline-flex items-center px-4 py-2 rounded-xl bg-brand-600 text-white text-xs font-semibold hover:bg-brand-700 transition shadow-sm"
              >
                <PlusCircle className="w-4 h-4 mr-1.5" /> Add Process
              </Link>
              {isSuperAdmin && (
                <button
                  onClick={handleDeleteCollege}
                  className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition"
                  title="Delete College"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              )}
            </div>
          )}
        </div>

        {college.description && (
          <p className="text-sm text-slate-600 leading-relaxed mt-6 pt-6 border-t border-slate-100">
            {college.description}
          </p>
        )}
      </div>

      {/* Category Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2 border-b border-slate-200">
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            className={`px-4 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition ${
              selectedCategory === cat
                ? 'bg-slate-900 text-white shadow-sm'
                : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Processes List or Empty State */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-slate-900">
            Available Institutional Processes ({processes.length})
          </h2>
          {canManage && (
            <Link
              to={`/colleges/${college.id}/processes/new`}
              className="text-xs font-semibold text-brand-600 hover:text-brand-800 flex items-center"
            >
              <PlusCircle className="w-3.5 h-3.5 mr-1" /> New Process
            </Link>
          )}
        </div>

        {processes.length === 0 ? (
          <EmptyState
            title="No institutional processes have been added yet."
            description="The administrative structure for this institution is active. Process workflows, checklist requirements, and guidelines will appear here once published."
            icon={Layers}
            actionLabel={canManage ? 'Add First Process' : undefined}
            onAction={canManage ? () => navigate(`/colleges/${college.id}/processes/new`) : undefined}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {processes.map((proc) => (
              <div
                key={proc.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm hover:shadow-md transition flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-1 rounded-md border border-brand-100">
                      {proc.category || 'General'}
                    </span>
                    <div className="flex items-center gap-2">
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                          proc.status === 'ACTIVE'
                            ? 'bg-emerald-100 text-emerald-800'
                            : 'bg-slate-100 text-slate-600'
                        }`}
                      >
                        {proc.status}
                      </span>
                      {canManage && (
                        <div className="flex items-center space-x-1 pl-2 border-l border-slate-200">
                          <Link
                            to={`/colleges/${college.id}/processes/${proc.id}/edit`}
                            className="p-1 text-slate-400 hover:text-brand-600 rounded"
                            title="Edit Process"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </Link>
                          <button
                            onClick={(e) => handleDeleteProcess(proc.id, e)}
                            disabled={deletingId === proc.id}
                            className="p-1 text-slate-400 hover:text-rose-600 rounded"
                            title="Delete Process"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                  <h3 className="text-lg font-bold text-slate-900 mb-2 leading-snug">
                    {proc.title}
                  </h3>

                  <div className="grid grid-cols-2 gap-2 text-xs text-slate-500 my-4 py-3 bg-slate-50 rounded-xl px-3 border border-slate-100">
                    <div className="flex items-center">
                      <Clock className="w-3.5 h-3.5 mr-1.5 text-slate-400" />
                      <span>{proc.timeline || 'Flexible timeline'}</span>
                    </div>
                    <div className="flex items-center">
                      <Banknote className="w-3.5 h-3.5 mr-1.5 text-slate-400" />
                      <span>{proc.fees || 'Free'}</span>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                  <div className="flex items-center space-x-3 text-xs text-slate-500 font-medium">
                    <span>{proc.stepCount} Steps</span>
                    <span>•</span>
                    <span>{proc.documentCount} Documents</span>
                  </div>
                  <Link
                    to={`/colleges/${college.id}/processes/${proc.id}`}
                    className="inline-flex items-center text-xs font-semibold text-brand-600 hover:text-brand-800"
                  >
                    View Process Guide <ArrowRight className="w-3.5 h-3.5 ml-1" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
