import React, { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { serviceCentersApi } from '../services/api';
import { ServiceCenter } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  MapPin,
  Search,
  Phone,
  Clock,
  Navigation,
  Building2,
  ExternalLink,
  ShieldCheck,
  CheckCircle2,
  AlertTriangle,
  X,
  Filter,
  Car,
  FileText,
  BadgeCheck,
  Layers,
} from 'lucide-react';

// Helper functions for category and details enrichment
const getCategory = (c: ServiceCenter): string => {
  if (c.category) return c.category;
  const lowerType = (c.serviceType || '').toLowerCase();
  const lowerName = (c.name || '').toLowerCase();
  if (
    lowerType.includes('passport') ||
    lowerName.includes('passport') ||
    lowerName.includes('psk') ||
    lowerName.includes('popsk') ||
    lowerName.includes('rpo')
  ) {
    return 'PASSPORT';
  }
  if (
    lowerType.includes('transport') ||
    lowerType.includes('rto') ||
    lowerType.includes('rta') ||
    lowerType.includes('licence') ||
    lowerName.includes('rto') ||
    lowerName.includes('transport') ||
    lowerName.includes('test track')
  ) {
    return 'DRIVING_LICENCE';
  }
  return 'CERTIFICATES';
};

const getIsGovernment = (c: ServiceCenter): boolean => {
  if (c.isGovernment !== undefined && c.isGovernment !== null) {
    return c.isGovernment;
  }
  const text = `${c.name} ${c.serviceType || ''}`.toLowerCase();
  if (text.includes('franchise') || text.includes('assisted') || text.includes('private')) {
    return false;
  }
  return true;
};

const getIsRpo = (c: ServiceCenter): boolean => {
  if (c.isRpo !== undefined && c.isRpo !== null) {
    return c.isRpo;
  }
  const text = `${c.name} ${c.serviceType || ''}`.toLowerCase();
  return text.includes('regional passport office') || text.includes('(rpo)');
};

const getSupportedServices = (c: ServiceCenter): string => {
  if (c.supportedServices) return c.supportedServices;
  const name = c.name.toLowerCase();
  const type = (c.serviceType || '').toLowerCase();

  if (name.includes('rpo') || type.includes('rpo')) {
    return 'Passport Jurisdiction Administration, Complex Inquiries, Grievances, Surrender & Revocation Inquiries, Appeals (Note: Routine new passport applications and biometrics are accepted at PSK and POPSK)';
  }
  if (name.includes('popsk') || type.includes('popsk')) {
    return 'Normal Fresh Passport, Passport Renewal, Biometrics & Verification (Note: Tatkaal and complex categories require PSK)';
  }
  if (name.includes('psk') || type.includes('psk') || type.includes('passport')) {
    return 'Fresh Passport Application, Passport Re-issue & Renewal, Tatkaal Passport, Minor Passport, Police Clearance Certificate (PCC), Biometric Capture & Document Verification';
  }
  if (name.includes('test track') || type.includes('test')) {
    return "Learner's Licence (LL) Computer-Based Test, Permanent DL Practical Driving Test (Two-Wheeler & Four-Wheeler Tracks), Vehicle Fitness Inspection";
  }
  if (type.includes('rto') || name.includes('rto') || type.includes('transport')) {
    return 'Permanent Driving Licence (DL) Issuance, Driving Licence Renewal, Duplicate DL, International Driving Permit (IDP), Vehicle Registration (RC), RC Ownership Transfer, Hypothecation Endorsement';
  }
  if (name.includes('franchise') || type.includes('franchise') || name.includes('assisted')) {
    return 'Assisted Online Application for EWS, Income and Caste Certificates, Challan Payment, Aadhaar Updation Assistance (Note: Authorized private franchise operator providing assisted citizen services)';
  }
  if (name.includes('tahsildar') || type.includes('tahsildar') || name.includes('mro')) {
    return 'Verification and Statutory Approval of EWS Certificate, Annual Family Income Certificate, Integrated Caste Certificate (SC/ST/BC), Residence Certificate, Revenue Field Inquiries';
  }
  return 'EWS Certificate Application Submission, Income Certificate Request, Caste Certificate Application, Biometric Authentication, Document Scanning & Fee Payment';
};

const getOfficialWebsite = (c: ServiceCenter): string => {
  if (c.officialWebsite) return c.officialWebsite;
  const cat = getCategory(c);
  if (cat === 'PASSPORT') return 'https://www.passportindia.gov.in';
  if (cat === 'DRIVING_LICENCE') return 'https://www.aptransport.org';
  return 'https://ap.meeseva.gov.in';
};

export const ServiceCentersPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  // URL query params
  const searchQuery = searchParams.get('q') || '';
  const selectedCategory = searchParams.get('category') || 'ALL';
  const selectedCity = searchParams.get('city') || 'ALL';
  const selectedType = searchParams.get('type') || 'ALL';

  const [centers, setCenters] = useState<ServiceCenter[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState<string>(searchQuery);

  const fetchCenters = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await serviceCentersApi.getAll(
        searchQuery || undefined,
        selectedCity !== 'ALL' ? selectedCity : undefined,
        selectedCategory !== 'ALL' ? selectedCategory : undefined,
        selectedType !== 'ALL' ? selectedType : undefined
      );
      setCenters(data);
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          'Unable to load service centres directory. Please check your connection.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCenters();
  }, [searchQuery, selectedCategory, selectedCity, selectedType]);

  const updateParam = (key: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (!value || value === 'ALL') {
      next.delete(key);
    } else {
      next.set(key, value);
    }
    setSearchParams(next);
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateParam('q', searchInput.trim());
  };

  const handleClearAll = () => {
    setSearchInput('');
    setSearchParams(new URLSearchParams());
  };

  const isFiltered = Boolean(
    searchQuery ||
      (selectedCategory && selectedCategory !== 'ALL') ||
      (selectedCity && selectedCity !== 'ALL') ||
      (selectedType && selectedType !== 'ALL')
  );

  // Filtered centres list ensuring both backend and frontend filters align perfectly
  const displayedCenters = useMemo(() => {
    return centers.filter((c) => {
      // 1. Category filter
      if (selectedCategory && selectedCategory !== 'ALL') {
        const cat = getCategory(c);
        if (cat !== selectedCategory) return false;
      }
      // 2. City filter
      if (selectedCity && selectedCity !== 'ALL') {
        if ((c.city || '').toLowerCase() !== selectedCity.toLowerCase()) {
          return false;
        }
      }
      // 3. Search query filter
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        const services = getSupportedServices(c).toLowerCase();
        const match =
          (c.name || '').toLowerCase().includes(q) ||
          (c.city || '').toLowerCase().includes(q) ||
          (c.address || '').toLowerCase().includes(q) ||
          (c.locality || '').toLowerCase().includes(q) ||
          (c.serviceType || '').toLowerCase().includes(q) ||
          services.includes(q);
        if (!match) return false;
      }
      return true;
    });
  }, [centers, selectedCategory, selectedCity, searchQuery]);

  // Helper functions for Google Maps URLs
  const getGoogleMapsSearchUrl = (center: ServiceCenter): string => {
    const parts = [
      center.name,
      center.address || center.locality,
      center.city,
      center.pincode,
      'Andhra Pradesh',
    ]
      .filter(Boolean)
      .join(', ');

    const params = new URLSearchParams({
      api: '1',
      query: parts,
    });
    return `https://www.google.com/maps/search/?${params.toString()}`;
  };

  const getGoogleMapsDirectionsUrl = (center: ServiceCenter): string => {
    const parts = [
      center.name,
      center.address || center.locality,
      center.city,
      center.pincode,
      'Andhra Pradesh',
    ]
      .filter(Boolean)
      .join(', ');

    const params = new URLSearchParams({
      api: '1',
      destination: parts,
    });
    return `https://www.google.com/maps/dir/?${params.toString()}`;
  };

  // Distinct cities list with Vijayawada highlighted as primary focus
  const availableCities = useMemo(() => {
    return [
      { id: 'ALL', label: 'All Cities' },
      { id: 'Vijayawada', label: 'Vijayawada (NTR) ★ Focus' },
      { id: 'Guntur', label: 'Guntur' },
      { id: 'Tirupati', label: 'Tirupati' },
      { id: 'Visakhapatnam', label: 'Visakhapatnam' },
      { id: 'Kadapa', label: 'Kadapa (YSR)' },
      { id: 'Kurnool', label: 'Kurnool' },
      { id: 'Nellore', label: 'Nellore (SPSR)' },
    ];
  }, []);

  const categoryTabs = [
    { id: 'ALL', label: 'All Services', icon: Layers },
    { id: 'PASSPORT', label: 'Passport Services', icon: FileText },
    { id: 'DRIVING_LICENCE', label: 'Driving Licence & RC', icon: Car },
    { id: 'CERTIFICATES', label: 'EWS, Income & Caste', icon: Building2 },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-8">
      {/* 1. Page Header */}
      <div className="relative rounded-3xl bg-gradient-to-br from-slate-900 via-brand-950 to-indigo-950 p-6 sm:p-10 text-white shadow-xl overflow-hidden">
        <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 rounded-full bg-brand-500/10 blur-3xl pointer-events-none" />
        <div className="absolute bottom-0 left-1/3 -mb-20 w-80 h-80 rounded-full bg-indigo-500/10 blur-3xl pointer-events-none" />

        <div className="relative z-10 max-w-3xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-brand-500/20 border border-brand-400/30 text-brand-200 text-xs font-semibold tracking-wide">
            <BadgeCheck className="w-4 h-4 text-brand-300" />
            <span>Official Andhra Pradesh Citizen Service Directory</span>
          </div>

          <h1 className="text-2xl sm:text-4xl font-black tracking-tight text-white">
            Find Service Centres
          </h1>

          <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
            Find nearby offices and service centres for passports, driving licences and government certificates.
            Directly cross-verified for Vijayawada, Guntur, Tirupati, Visakhapatnam, and key regional hubs.
          </p>

          <div className="pt-2 flex flex-wrap gap-2 text-xs text-slate-300">
            <span className="inline-flex items-center px-2.5 py-1 rounded-lg bg-white/10 backdrop-blur-sm border border-white/10">
              🏛️ Official Government Offices
            </span>
            <span className="inline-flex items-center px-2.5 py-1 rounded-lg bg-white/10 backdrop-blur-sm border border-white/10">
              🏢 Authorized MeeSeva Centres
            </span>
            <span className="inline-flex items-center px-2.5 py-1 rounded-lg bg-white/10 backdrop-blur-sm border border-white/10">
              🗺️ 1-Click Google Maps & Directions
            </span>
          </div>
        </div>
      </div>

      {/* 2. Search & Category Filters Bar */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-4 sm:p-6 shadow-sm space-y-5">
        {/* Category Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-none">
          {categoryTabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = selectedCategory === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => updateParam('category', tab.id)}
                className={`flex items-center space-x-2 px-4 py-2.5 rounded-xl text-xs sm:text-sm font-semibold whitespace-nowrap transition-all ${
                  isActive
                    ? 'bg-brand-600 text-white shadow-md shadow-brand-500/20'
                    : 'bg-slate-100 text-slate-700 hover:bg-slate-200/80 hover:text-slate-900'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Search & City Filter Row */}
        <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 pt-2">
          {/* Search Input */}
          <form onSubmit={handleSearchSubmit} className="sm:col-span-8 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by centre name, locality (e.g. Labbipet, Auto Nagar), or service..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full pl-10 pr-24 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition"
            />
            {searchInput && (
              <button
                type="button"
                onClick={() => {
                  setSearchInput('');
                  updateParam('q', '');
                }}
                className="absolute right-16 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-slate-600"
                title="Clear input"
              >
                <X className="w-4 h-4" />
              </button>
            )}
            <button
              type="submit"
              className="absolute right-2 top-1/2 -translate-y-1/2 px-3.5 py-1.5 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg transition"
            >
              Search
            </button>
          </form>

          {/* City Dropdown */}
          <div className="sm:col-span-4 relative">
            <select
              value={selectedCity}
              onChange={(e) => updateParam('city', e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition"
            >
              {availableCities.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Filter Badges & Summary */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-2 text-xs border-t border-slate-100">
          <div className="flex items-center space-x-2 text-slate-500">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <span>
              Showing <strong className="text-slate-800">{displayedCenters.length}</strong> verified centres
              {selectedCity !== 'ALL' && ` in ${selectedCity}`}
              {selectedCategory !== 'ALL' && ` for ${selectedCategory.replace('_', ' ')}`}
            </span>
          </div>

          {isFiltered && (
            <button
              onClick={handleClearAll}
              className="inline-flex items-center space-x-1 text-brand-600 hover:text-brand-800 font-semibold"
            >
              <X className="w-3.5 h-3.5" />
              <span>Clear all filters</span>
            </button>
          )}
        </div>
      </div>

      {/* 3. Main Directory Grid */}
      {isLoading ? (
        <LoadingSpinner message="Retrieving verified service centres..." />
      ) : error ? (
        <ErrorAlert message={error} onRetry={fetchCenters} />
      ) : displayedCenters.length === 0 ? (
        <EmptyState
          title="No service centres found"
          description="No government offices or facilitation centres match your current search and filter combination. Try selecting 'All Cities' or resetting your search term."
          icon={MapPin}
          actionLabel="Reset All Filters"
          onAction={handleClearAll}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {displayedCenters.map((center) => {
            const mapsSearchUrl = getGoogleMapsSearchUrl(center);
            const mapsDirectionsUrl = getGoogleMapsDirectionsUrl(center);
            const isGov = getIsGovernment(center);
            const isRpoCenter = getIsRpo(center);
            const services = getSupportedServices(center);
            const website = getOfficialWebsite(center);

            return (
              <div
                key={center.id}
                className="bg-white rounded-2xl border border-slate-200/90 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col justify-between overflow-hidden"
              >
                <div className="p-5 sm:p-6 space-y-4">
                  {/* Top Badges */}
                  <div className="flex flex-wrap items-center gap-2">
                    {/* Government vs Assisted-Service Badge */}
                    {isGov ? (
                      <span className="inline-flex items-center space-x-1 text-[11px] font-bold text-emerald-800 bg-emerald-50 border border-emerald-200 px-2.5 py-0.5 rounded-full">
                        <Building2 className="w-3 h-3 text-emerald-600" />
                        <span>Government Office</span>
                      </span>
                    ) : (
                      <span className="inline-flex items-center space-x-1 text-[11px] font-bold text-amber-800 bg-amber-50 border border-amber-200 px-2.5 py-0.5 rounded-full">
                        <Building2 className="w-3 h-3 text-amber-600" />
                        <span>Authorized Franchise (Assisted)</span>
                      </span>
                    )}

                    {/* Facility Type Badge */}
                    <span className="text-[11px] font-bold uppercase tracking-wider text-brand-700 bg-brand-50 border border-brand-100 px-2 py-0.5 rounded-full">
                      {center.serviceType || 'Facilitation Center'}
                    </span>
                  </div>

                  {/* Centre Name */}
                  <div>
                    <h3 className="text-base sm:text-lg font-bold text-slate-900 leading-snug">
                      {center.name}
                    </h3>
                    <p className="text-xs text-brand-600 font-semibold mt-0.5">
                      {center.locality ? `${center.locality}, ` : ''}
                      {center.city}
                      {center.district ? ` (${center.district})` : ''}
                    </p>
                  </div>

                  {/* RPO Special Clarification Banner */}
                  {isRpoCenter && (
                    <div className="p-2.5 rounded-xl bg-blue-50/80 border border-blue-200 text-blue-900 text-xs flex items-start space-x-2">
                      <AlertTriangle className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
                      <p className="leading-relaxed">
                        <strong>Administrative Authority:</strong> Routine passport applications and biometrics are accepted at
                        PSKs & POPSKs. Visit RPO only for specific jurisdiction inquiries, complex grievances, or formal appeals.
                      </p>
                    </div>
                  )}

                  {/* Address */}
                  <div className="flex items-start space-x-2 text-xs text-slate-600 leading-relaxed">
                    <MapPin className="w-3.5 h-3.5 text-slate-400 flex-shrink-0 mt-0.5" />
                    <span>{center.address}</span>
                  </div>

                  {/* Verified Services Handled */}
                  {services && (
                    <div className="space-y-1.5 pt-1">
                      <p className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
                        Verified Services Handled:
                      </p>
                      <p className="text-xs text-slate-700 bg-slate-50 p-2.5 rounded-xl border border-slate-100 leading-relaxed">
                        {services}
                      </p>
                    </div>
                  )}

                  {/* Operational Details */}
                  <div className="space-y-2 pt-2 border-t border-slate-100 text-xs text-slate-600">
                    {center.operatingHours && (
                      <div className="flex items-center space-x-2">
                        <Clock className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                        <span>{center.operatingHours}</span>
                      </div>
                    )}

                    {center.contactNumber && (
                      <div className="flex items-center space-x-2">
                        <Phone className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                        <a
                          href={`tel:${center.contactNumber.split('/')[0].trim()}`}
                          className="text-brand-600 hover:underline font-medium"
                        >
                          {center.contactNumber}
                        </a>
                      </div>
                    )}

                    {website && (
                      <div className="flex items-center space-x-2">
                        <ExternalLink className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                        <a
                          href={website}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-brand-600 hover:underline font-medium truncate"
                        >
                          {website.replace(/^https?:\/\//, '')}
                        </a>
                      </div>
                    )}
                  </div>
                </div>

                {/* Card Footer: Verification Badge & Dual Google Maps Buttons */}
                <div className="p-4 bg-slate-50 border-t border-slate-100 space-y-3">
                  <div className="flex items-center justify-between text-[11px] text-slate-500">
                    <span className="inline-flex items-center space-x-1 font-medium text-emerald-700">
                      <ShieldCheck className="w-3 h-3 text-emerald-600" />
                      <span>{center.verificationStatus || 'Verified Official'}</span>
                    </span>
                    <span>{center.lastVerified || 'October 2026'}</span>
                  </div>

                  {/* Dual Google Maps Actions */}
                  <div className="grid grid-cols-2 gap-2 pt-1">
                    <a
                      href={mapsSearchUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center justify-center space-x-1 px-3 py-2 text-xs font-semibold rounded-xl bg-white border border-slate-300 text-slate-700 hover:bg-slate-100 hover:border-slate-400 transition text-center shadow-sm"
                      title="Open Google Maps search for this centre"
                    >
                      <MapPin className="w-3.5 h-3.5 text-slate-600" />
                      <span>View on Maps</span>
                    </a>

                    <a
                      href={mapsDirectionsUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center justify-center space-x-1 px-3 py-2 text-xs font-semibold rounded-xl bg-brand-600 hover:bg-brand-700 text-white transition text-center shadow-sm shadow-brand-500/20"
                      title="Open Google Maps directions to this centre"
                    >
                      <Navigation className="w-3.5 h-3.5 text-white" />
                      <span>Get Directions</span>
                    </a>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default ServiceCentersPage;
