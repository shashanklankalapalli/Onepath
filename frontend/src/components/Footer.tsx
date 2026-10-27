import React from 'react';
import { Link } from 'react-router-dom';
import { Shield, Sparkles, Heart } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-900 text-slate-400 border-t border-slate-800 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div className="md:col-span-1">
            <div className="flex items-center space-x-2.5 mb-3">
              <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center text-white font-black text-base shadow">
                1P
              </div>
              <span className="text-lg font-bold text-white tracking-tight">OnePath</span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed mb-4">
              "Understand. Apply. Stay Updated." Simplifying complicated civic, educational, and institutional procedures for youth and students.
            </p>
            <div className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full bg-slate-800 text-slate-300 text-xs border border-slate-700">
              <Shield className="w-3.5 h-3.5 text-emerald-400 mr-1" />
              <span>Verified Institutional Platform</span>
            </div>
          </div>

          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-200 mb-3">
              Processes
            </h4>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/services?category=Government" className="hover:text-white transition">
                  Civic & Government
                </Link>
              </li>
              <li>
                <Link to="/services?category=Education" className="hover:text-white transition">
                  Educational Procedures
                </Link>
              </li>
              <li>
                <Link to="/services?category=Documents" className="hover:text-white transition">
                  Certificates & Passports
                </Link>
              </li>
              <li>
                <Link to="/colleges" className="hover:text-white transition">
                  College Dashboards
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-200 mb-3">
              Student Tools
            </h4>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/mentor" className="text-brand-300 hover:text-white font-semibold transition">
                  Hire a Mentor
                </Link>
              </li>
              <li>
                <Link to="/my-documents" className="hover:text-white transition">
                  Document Checklist
                </Link>
              </li>
              <li>
                <Link to="/my-documents?filter=missing" className="hover:text-white transition">
                  Missing Document Guide
                </Link>
              </li>
              <li>
                <Link to="/updates" className="hover:text-white transition">
                  Fee & Deadline Alerts
                </Link>
              </li>
              <li>
                <Link to="/service-centers" className="hover:text-white transition">
                  Center Locator
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-200 mb-3">
              Institutional Admins
            </h4>
            <p className="text-xs text-slate-400 leading-relaxed mb-3">
              College admission desks and department officers can manage process steps, requirements, and circular updates directly.
            </p>
            <Link
              to="/login"
              className="inline-flex items-center text-xs font-semibold text-brand-400 hover:text-brand-300"
            >
              <Sparkles className="w-3.5 h-3.5 mr-1" /> Admin Portal Login &rarr;
            </Link>
          </div>
        </div>

        <div className="pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500">
          <p>© {new Date().getFullYear()} OnePath Platform. All rights reserved.</p>
          <p className="mt-2 sm:mt-0 flex items-center">
            Built with modern resilience for students everywhere.
          </p>
        </div>
      </div>
    </footer>
  );
};
