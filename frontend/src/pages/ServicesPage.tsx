import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { civicServicesApi } from '../services/api';
import { CivicService } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  Search,
  Filter,
  Layers,
  ArrowRight,
  Clock,
  Banknote,
  FileCheck2,
  ExternalLink,
} from 'lucide-react';

const CATEGORIES = [
  'All',
  'Government & Civic Services',
  'Government Documents',
  'Education',
  'Documents & Certificates',
  'Other Institutional Services',
];

export const ServicesPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const activeCategory = searchParams.get('category') || 'All';
  const searchQuery = searchParams.get('q') || '';

  const [services, setServices] = useState<CivicService[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>(searchQuery);

  const fetchServices = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const categoryParam = activeCategory === 'All' ? undefined : activeCategory;
      const data = await civicServicesApi.getAll(categoryParam, searchQuery);
      setServices(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load services. Please check backend connection.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchServices();
  }, [activeCategory, searchQuery]);

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

  const handleCategoryChange = (category: string) => {
    const newParams = new URLSearchParams(searchParams);
    if (category === 'All') {
      newParams.delete('category');
    } else {
      newParams.set('category', category);
    }
    setSearchParams(newParams);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
          Services Directory
        </h1>
        <p className="text-slate-600 mt-1 text-sm sm:text-base">
          Discover verified step-by-step procedures for government credentials, certifications, and civic paperwork.
        </p>
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="relative">
          <Search className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search processes by name, keyword, or document required..."
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

        {/* Category Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-1 scrollbar-none">
          <Filter className="w-4 h-4 text-slate-400 flex-shrink-0 mr-1" />
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => handleCategoryChange(cat)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition ${
                activeCategory === cat || (cat === 'All' && !searchParams.get('category'))
                  ? 'bg-brand-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200 hover:text-slate-900'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Content States */}
      {isLoading ? (
        <LoadingSpinner message="Fetching verified services directory..." />
      ) : error ? (
        <ErrorAlert message={error} onRetry={fetchServices} />
      ) : services.length === 0 ? (
        <EmptyState
          title="No services found"
          description={
            searchQuery || activeCategory !== 'All'
              ? 'No services match your active search filter. Try clearing the filter or searching for another term.'
              : 'No service documentation has been published yet. The database architecture is ready for future ingestion.'
          }
          icon={Layers}
          actionLabel={searchQuery || activeCategory !== 'All' ? 'Reset Filters' : undefined}
          onAction={() => setSearchParams({})}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((service) => (
            <div
              key={service.id}
              className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-3">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-1 rounded-md border border-brand-100">
                    {service.category}
                  </span>
                  {service.timeline && (
                    <span className="flex items-center text-xs text-slate-500 font-medium">
                      <Clock className="w-3.5 h-3.5 mr-1 text-slate-400" />
                      {service.timeline}
                    </span>
                  )}
                </div>

                <h3 className="text-lg font-bold text-slate-900 mb-2 leading-snug">
                  {service.name}
                </h3>
                <p className="text-sm text-slate-600 line-clamp-3 mb-4 leading-relaxed">
                  {service.description || 'Step-by-step guidance and prerequisite document details.'}
                </p>

                {service.hasQuestionnaire && (
                  <div className="mb-4 inline-flex items-center text-xs font-semibold text-emerald-800 bg-emerald-50 px-2.5 py-1 rounded-md border border-emerald-200">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 mr-1.5 animate-pulse" />
                    Intelligent Questionnaire & Pathway Matching
                  </div>
                )}
              </div>

              <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
                <div className="flex items-center text-xs text-slate-500">
                  <FileCheck2 className="w-4 h-4 mr-1 text-emerald-600" />
                  <span>{service.requiredDocuments?.length || 0} Docs Required</span>
                </div>
                <Link
                  to={`/services/${service.slug || service.id}`}
                  className="inline-flex items-center text-xs font-semibold text-brand-600 hover:text-brand-800"
                >
                  {service.hasQuestionnaire ? 'Start Smart Navigator' : 'View Guide'}{' '}
                  <ArrowRight className="w-3.5 h-3.5 ml-1" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
