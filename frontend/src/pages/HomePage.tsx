import React from 'react';
import { Link } from 'react-router-dom';
import {
  GraduationCap,
  FileText,
  CheckCircle2,
  Bell,
  MapPin,
  ArrowRight,
  ShieldCheck,
  Search,
  Sparkles,
  Clock,
  HelpCircle,
  Heart,
} from 'lucide-react';

export const HomePage: React.FC = () => {
  return (
    <div className="space-y-20 pb-20">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28">
        {/* Background Gradients */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-brand-400/15 rounded-full blur-3xl pointer-events-none -z-10" />
        <div className="absolute top-1/4 right-10 w-72 h-72 bg-indigo-400/15 rounded-full blur-3xl pointer-events-none -z-10" />

        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs sm:text-sm font-semibold mb-6 shadow-sm">
            <Sparkles className="w-4 h-4 text-brand-600 animate-pulse" />
            <span>Smart Process Guidance Platform for Students</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-black text-slate-950 tracking-tight leading-[1.1] mb-6">
            Understand. Apply.{' '}
            <span className="bg-gradient-to-r from-brand-600 via-indigo-600 to-sky-500 bg-clip-text text-transparent">
              Stay Updated.
            </span>
          </h1>

          <p className="text-base sm:text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed mb-10 font-normal">
            OnePath simplifies complicated government, civic, educational, and institutional procedures into clear, step-by-step guidance tailored for young adults.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 max-w-md mx-auto">
            <Link
              to="/services"
              className="w-full sm:w-auto inline-flex items-center justify-center px-6 py-3.5 rounded-xl text-base font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-lg shadow-brand-500/25 transition transform hover:-translate-y-0.5"
            >
              Explore Services
              <ArrowRight className="w-4 h-4 ml-2" />
            </Link>

            <Link
              to="/colleges"
              className="w-full sm:w-auto inline-flex items-center justify-center px-6 py-3.5 rounded-xl text-base font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 shadow-sm transition hover:border-slate-400"
            >
              <GraduationCap className="w-5 h-5 mr-2 text-brand-600" />
              College Documentation
            </Link>
          </div>
        </div>
      </section>

      {/* Feature Highlights Grid */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-14">
          <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">
            Everything you need to navigate critical milestones
          </h2>
          <p className="text-slate-600 mt-2 text-sm sm:text-base">
            Never miss a document, eligibility requirement, or deadline again.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Card 1 */}
          <div className="bg-white p-7 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-300 transition group">
            <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center mb-5 group-hover:scale-110 transition">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">Eligibility Guidance</h3>
            <p className="text-sm text-slate-600 leading-relaxed">
              Verify your qualifications and age criteria before applying to save valuable time and application fees.
            </p>
          </div>

          {/* Card 2 */}
          <div className="bg-white p-7 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-300 transition group">
            <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-5 group-hover:scale-110 transition">
              <Clock className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">Step-by-Step Timelines</h3>
            <p className="text-sm text-slate-600 leading-relaxed">
              Clear sequential instructions broken into manageable milestones with estimated processing times.
            </p>
          </div>

          {/* Card 3 */}
          <div className="bg-white p-7 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-300 transition group">
            <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center mb-5 group-hover:scale-110 transition">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">Document Checklist</h3>
            <p className="text-sm text-slate-600 leading-relaxed">
              Interactive document tracker to mark ready documents and receive actionable guides for missing paperwork.
            </p>
          </div>

          {/* Card 4 */}
          <div className="bg-white p-7 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-300 transition group">
            <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center mb-5 group-hover:scale-110 transition">
              <Bell className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">Important Updates</h3>
            <p className="text-sm text-slate-600 leading-relaxed">
              Timely alerts regarding fee revisions, deadline extensions, and rule amendments from institutions.
            </p>
          </div>

          {/* Card 5 */}
          <div className="bg-white p-7 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-300 transition group">
            <div className="w-12 h-12 rounded-xl bg-sky-50 text-sky-600 flex items-center justify-center mb-5 group-hover:scale-110 transition">
              <GraduationCap className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">College Documentation</h3>
            <p className="text-sm text-slate-600 leading-relaxed">
              Dedicated institutional module connecting students with college-specific admissions, hostels, and certificate requirements.
            </p>
          </div>

          {/* Card 6 */}
          <div className="bg-white p-7 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-300 transition group">
            <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center mb-5 group-hover:scale-110 transition">
              <MapPin className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">Service Centers</h3>
            <p className="text-sm text-slate-600 leading-relaxed">
              Locate physical facilitation counters, civic centers, and verification desks in your locality.
            </p>
          </div>
        </div>
      </section>

      {/* JoSAA & Admissions Counselling Spotlight Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-3xl bg-gradient-to-r from-blue-950 via-slate-900 to-indigo-950 text-white p-8 sm:p-12 relative overflow-hidden shadow-xl border border-blue-900/40">
          <div className="relative z-10 max-w-2xl space-y-4">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-blue-500/20 text-blue-300 text-xs font-bold border border-blue-400/30">
              <Sparkles className="w-3.5 h-3.5" />
              <span>National & State Admissions Architecture</span>
            </div>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight">
              JoSAA & State College Admissions Counselling
            </h2>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              Navigate centralized admissions after JEE Main, JEE Advanced, and AP EAPCET. Check your dual eligibility across 5 compulsory subjects, explore verified cutoffs for 23 IITs, 31 NITs, IIITs, and AP state engineering colleges, and track your personalized roadmap.
            </p>
            <div className="pt-2 flex flex-wrap gap-4">
              <Link
                to="/services/josaa-state-admissions-counselling"
                className="inline-flex items-center px-6 py-3 rounded-xl bg-brand-600 text-white font-semibold hover:bg-brand-500 transition shadow-md shadow-brand-500/20"
              >
                Launch Counselling Guide
                <ArrowRight className="w-4 h-4 ml-2" />
              </Link>
              <Link
                to="/services?category=Education"
                className="inline-flex items-center px-6 py-3 rounded-xl bg-white/10 text-white font-semibold hover:bg-white/20 border border-white/20 transition"
              >
                View Education Services
              </Link>
            </div>
          </div>
          <div className="hidden lg:block absolute -right-10 -bottom-10 opacity-10 pointer-events-none">
            <GraduationCap className="w-96 h-96 text-white" />
          </div>
        </div>
      </section>

      {/* Marriage Registration Spotlight Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-3xl bg-gradient-to-r from-rose-950 via-slate-900 to-indigo-950 text-white p-8 sm:p-12 relative overflow-hidden shadow-xl border border-rose-900/40">
          <div className="relative z-10 max-w-2xl space-y-4">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-rose-500/20 text-rose-300 text-xs font-bold border border-rose-400/30">
              <Heart className="w-3.5 h-3.5 fill-rose-400 text-rose-400" />
              <span>Civic & Legal Services • Andhra Pradesh</span>
            </div>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight">
              Marriage Registration & Certification
            </h2>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              Official statutory guidance for couples registering under the Hindu Marriage Act, 1955 (already solemnized religious ceremony) or Special Marriage Act, 1954 (civil/court marriage). Check eligibility, prepare 11 verified documents, organize mandatory witnesses, and book SRO appointment slots.
            </p>
            <div className="pt-2 flex flex-wrap gap-4">
              <Link
                to="/services/marriage-registration"
                className="inline-flex items-center px-6 py-3 rounded-xl bg-rose-600 text-white font-semibold hover:bg-rose-500 transition shadow-md shadow-rose-500/20"
              >
                Launch Registration Guide
                <ArrowRight className="w-4 h-4 ml-2" />
              </Link>
              <Link
                to="/services"
                className="inline-flex items-center px-6 py-3 rounded-xl bg-white/10 text-white font-semibold hover:bg-white/20 border border-white/20 transition"
              >
                Browse All Services
              </Link>
            </div>
          </div>
          <div className="hidden lg:block absolute -right-10 -bottom-10 opacity-10 pointer-events-none">
            <Heart className="w-96 h-96 text-white fill-white" />
          </div>
        </div>
      </section>

      {/* College Documentation Dedicated Spotlight */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-3xl bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white p-8 sm:p-12 relative overflow-hidden shadow-xl">
          <div className="relative z-10 max-w-2xl">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-300 text-xs font-semibold mb-4 border border-indigo-400/30">
              <GraduationCap className="w-3.5 h-3.5" />
              <span>Campus & Institutional Hub</span>
            </div>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight mb-4">
              College Documentation & Verification Hub
            </h2>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed mb-8">
              Select your university or college to review department checklists, hostel onboarding, migration certificates, and scholarship verification instructions directly structured from institutional databases.
            </p>
            <Link
              to="/colleges"
              className="inline-flex items-center px-6 py-3 rounded-xl bg-brand-600 text-white font-semibold hover:bg-brand-500 transition shadow-md"
            >
              Browse College Dashboards
              <ArrowRight className="w-4 h-4 ml-2" />
            </Link>
          </div>
          <div className="hidden lg:block absolute -right-10 -bottom-10 opacity-10 pointer-events-none">
            <GraduationCap className="w-96 h-96 text-white" />
          </div>
        </div>
      </section>
    </div>
  );
};
