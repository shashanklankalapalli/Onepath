import React from 'react';
import { useAuth } from '../context/AuthContext';
import { User as UserIcon, Mail, Shield, Award, Calendar, LogOut } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, logout } = useAuth();

  if (!user) return null;

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      <div>
        <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
          User Profile
        </h1>
        <p className="text-slate-600 mt-1 text-sm sm:text-base">
          Manage your account credentials and institutional role settings.
        </p>
      </div>

      <div className="bg-white rounded-3xl border border-slate-200/80 p-8 shadow-sm space-y-6">
        <div className="flex items-center space-x-4">
          <div className="w-16 h-16 rounded-2xl bg-brand-600 text-white flex items-center justify-center font-black text-2xl shadow-md shadow-brand-500/20">
            {user.name.charAt(0).toUpperCase()}
          </div>
          <div>
            <h2 className="text-xl font-bold text-slate-900">{user.name}</h2>
            <p className="text-sm text-slate-500">{user.email}</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-6 border-t border-slate-100">
          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/60">
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider mb-1 flex items-center">
              <Shield className="w-3.5 h-3.5 mr-1 text-brand-600" /> Account Role
            </p>
            <p className="text-sm font-bold text-slate-900">
              {user.role === 'USER' ? 'STUDENT' : user.role === 'SUPER_ADMIN' ? 'ADMIN' : user.role}
            </p>
          </div>

          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/60">
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider mb-1 flex items-center">
              <Award className="w-3.5 h-3.5 mr-1 text-emerald-600" /> System ID
            </p>
            <p className="text-sm font-bold text-slate-900">UID #{user.id}</p>
          </div>
        </div>

        {user.assignedCollegeId && (
          <div className="p-4 bg-brand-50 rounded-2xl border border-brand-200 text-xs text-brand-900">
            <strong>Institutional Administrator:</strong> You are authorized to manage processes for College ID #{user.assignedCollegeId}.
          </div>
        )}

        <div className="pt-6 border-t border-slate-100 flex justify-end">
          <button
            onClick={logout}
            className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-semibold text-rose-600 bg-rose-50 hover:bg-rose-100 transition"
          >
            <LogOut className="w-4 h-4 mr-1.5" /> Sign Out
          </button>
        </div>
      </div>
    </div>
  );
};
