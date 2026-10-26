import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { updatesApi } from '../services/api';
import { PlatformUpdate } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  Bell,
  Calendar,
  AlertTriangle,
  Info,
  Filter,
  Layers,
  ExternalLink,
} from 'lucide-react';

const UPDATE_CATEGORIES = [
  'All',
  'Fee Changes',
  'Deadline Updates',
  'Policy & Rules',
  'Process Simplification',
  'College Notices',
];

export const UpdatesPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const selectedCategory = searchParams.get('category') || 'All';

  const [updates, setUpdates] = useState<PlatformUpdate[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUpdates = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await updatesApi.getAll(
        selectedCategory === 'All' ? undefined : selectedCategory
      );
      setUpdates(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load updates.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUpdates();
  }, [selectedCategory]);

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
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
          Important Updates & Alerts
        </h1>
        <p className="text-slate-600 mt-1 text-sm sm:text-base">
          Live notifications regarding fee modifications, deadline extensions, and process revisions.
        </p>
      </div>

      {/* Category Pills */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center space-x-2 overflow-x-auto">
        <Filter className="w-4 h-4 text-slate-400 flex-shrink-0 mr-1" />
        {UPDATE_CATEGORIES.map((cat) => (
          <button
            key={cat}
            onClick={() => handleCategoryChange(cat)}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition ${
              selectedCategory === cat
                ? 'bg-slate-900 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Updates List or Empty State */}
      {isLoading ? (
        <LoadingSpinner message="Checking for institutional updates..." />
      ) : error ? (
        <ErrorAlert message={error} onRetry={fetchUpdates} />
      ) : updates.length === 0 ? (
        <EmptyState
          title="No updates or circulars posted"
          description="There are currently no active alerts for this category. All updates and timeline extensions will appear here in real time."
          icon={Bell}
        />
      ) : (
        <div className="space-y-4">
          {updates.map((update) => (
            <div
              key={update.id}
              className={`bg-white rounded-2xl border p-6 shadow-sm space-y-3 transition ${
                update.urgent ? 'border-amber-300 bg-amber-50/10' : 'border-slate-200/80'
              }`}
            >
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center space-x-2">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-brand-700 bg-brand-50 px-2.5 py-0.5 rounded border border-brand-100">
                    {update.category}
                  </span>
                  {update.urgent && (
                    <span className="flex items-center text-[11px] font-bold text-amber-700 bg-amber-100 px-2.5 py-0.5 rounded border border-amber-300">
                      <AlertTriangle className="w-3 h-3 mr-1" /> Urgent Alert
                    </span>
                  )}
                </div>

                {update.effectiveDate && (
                  <span className="flex items-center text-xs text-slate-500 font-medium">
                    <Calendar className="w-3.5 h-3.5 mr-1 text-slate-400" />
                    Effective Date: {update.effectiveDate}
                  </span>
                )}
              </div>

              <h3 className="text-lg font-bold text-slate-900 leading-snug">
                {update.title}
              </h3>

              <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
                {update.description}
              </p>

              <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2 text-xs">
                {update.relatedServiceOrProcess ? (
                  <div className="flex items-center text-slate-500">
                    <Layers className="w-3.5 h-3.5 mr-1 text-slate-400" />
                    <span>Related to: <strong className="text-slate-700">{update.relatedServiceOrProcess}</strong></span>
                  </div>
                ) : <div />}
                {update.officialSourceUrl && (
                  <a
                    href={update.officialSourceUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center font-semibold text-brand-600 hover:text-brand-800 underline ml-auto"
                  >
                    Official Source & Circular <ExternalLink className="w-3.5 h-3.5 ml-1" />
                  </a>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
