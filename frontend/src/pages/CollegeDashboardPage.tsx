import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { collegeService } from '../services/api';
import { College } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import { useAuth } from '../context/AuthContext';
import {
  GraduationCap,
  Search,
  MapPin,
  ExternalLink,
  ArrowRight,
  PlusCircle,
  Building,
} from 'lucide-react';

export const CollegeDashboardPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const searchQuery = searchParams.get('q') || '';
  const { user } = useAuth();

  const [colleges, setColleges] = useState<College[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>(searchQuery);

  const fetchColleges = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await collegeService.getAll(searchQuery);
      setColleges(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load colleges from backend database.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchColleges();
  }, [searchQuery]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newParams = new URLSearchParams(searchParams);
    if (searchTerm.trim()) {
      newParams.set('q', searchTerm.trim());
    } else {
      newParams.delete('q');
    }
    setSearchParams(newParams);
  };

  const isCollegeAdminOrSuper = user?.role === 'COLLEGE_ADMIN' || user?.role === 'SUPER_ADMIN';

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
            College Documentation Hub
          </h1>
          <p className="text-slate-600 mt-1 text-sm sm:text-base">
            Select your university or college to access tailored documentation guidelines and requirements.
          </p>
        </div>

        {isCollegeAdminOrSuper && (
          <Link
            to="/colleges/manage"
            className="inline-flex items-center px-4 py-2.5 rounded-xl bg-brand-600 text-white text-sm font-semibold hover:bg-brand-700 transition shadow-sm"
          >
            <PlusCircle className="w-4 h-4 mr-2" /> Add Institutional Process
          </Link>
        )}
      </div>

      {/* Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm">
        <form onSubmit={handleSearchSubmit} className="relative">
          <Search className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search universities by institution name, short acronym, or city..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-11 pr-28 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition"
          />
          <button
            type="submit"
            className="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-1.5 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg transition"
          >
            Search
          </button>
        </form>
      </div>

      {/* Content States */}
      {isLoading ? (
        <LoadingSpinner message="Checking college registry..." />
      ) : error ? (
        <ErrorAlert message={error} onRetry={fetchColleges} />
      ) : colleges.length === 0 ? (
        <EmptyState
          title="No institutions have been added yet."
          description="The college documentation architecture is live and ready. When institutions or processes are registered in the database, they will appear here automatically."
          icon={GraduationCap}
          actionLabel={searchQuery ? 'Clear Search' : isCollegeAdminOrSuper ? 'Add First Institution' : undefined}
          onAction={() => {
            if (searchQuery) {
              setSearchTerm('');
              setSearchParams({});
            } else if (isCollegeAdminOrSuper) {
              window.location.href = '/colleges/manage';
            }
          }}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {colleges.map((college) => (
            <div
              key={college.id}
              className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm hover:shadow-md hover:border-brand-300 transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="w-12 h-12 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center font-bold text-base border border-brand-100">
                    {college.shortName || college.name.substring(0, 2).toUpperCase()}
                  </div>
                  {college.location && (
                    <span className="flex items-center text-xs text-slate-500">
                      <MapPin className="w-3.5 h-3.5 mr-1 text-slate-400" />
                      {college.location}
                    </span>
                  )}
                </div>

                <h3 className="text-lg font-bold text-slate-900 mb-1 leading-snug">
                  {college.name}
                </h3>
                {college.shortName && (
                  <p className="text-xs font-semibold text-brand-600 mb-3">{college.shortName}</p>
                )}
                <p className="text-sm text-slate-600 line-clamp-3 mb-4 leading-relaxed">
                  {college.description || 'Access verified admission, migration, and certificate processes for this college.'}
                </p>
              </div>

              <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
                <span className="text-xs font-medium text-slate-500">
                  {college.processCount || 0} Available Processes
                </span>
                <Link
                  to={`/colleges/${college.id}`}
                  className="inline-flex items-center text-xs font-semibold text-brand-600 hover:text-brand-800"
                >
                  View Processes <ArrowRight className="w-3.5 h-3.5 ml-1" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
