import React, { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Compass,
  GraduationCap,
  FileCheck2,
  Bell,
  MapPin,
  User as UserIcon,
  LogOut,
  Menu,
  X,
  Layers,
  Sparkles,
  Shield,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navLinks = [
    { name: 'Home', path: '/', icon: Compass },
    { name: 'Services', path: '/services', icon: Layers },
    { name: 'College Dashboard', path: '/colleges', icon: GraduationCap },
    { name: 'Hire a Mentor', path: '/mentor', icon: Sparkles },
    { name: 'My Documents', path: '/my-documents', icon: FileCheck2 },
    { name: 'Updates', path: '/updates', icon: Bell },
    { name: 'Service Centres', path: '/service-centers', icon: MapPin },
  ];

  const isAdmin = user?.role === 'SUPER_ADMIN' || user?.role === 'COLLEGE_ADMIN';

  return (
    <nav className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Brand Logo */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-2.5">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-700 via-brand-600 to-indigo-500 flex items-center justify-center text-white font-black text-xl shadow-md shadow-brand-500/20">
                1P
              </div>
              <div>
                <span className="text-xl font-black bg-gradient-to-r from-slate-950 via-slate-800 to-brand-700 bg-clip-text text-transparent">
                  OnePath
                </span>
                <span className="hidden sm:block text-[10px] text-slate-400 font-medium tracking-wide uppercase">
                  Understand. Apply. Stay Updated.
                </span>
              </div>
            </Link>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden lg:flex items-center space-x-1">
            {navLinks.map((link) => {
              const Icon = link.icon;
              return (
                <NavLink
                  key={link.path}
                  to={link.path}
                  className={({ isActive }) =>
                    `flex items-center space-x-1.5 px-3 py-2 rounded-lg text-sm font-medium transition ${
                      isActive
                        ? 'bg-brand-50 text-brand-700 font-semibold'
                        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{link.name}</span>
                </NavLink>
              );
            })}
          </div>

          {/* User Auth Section */}
          <div className="hidden lg:flex items-center space-x-3">
            {isAuthenticated && user ? (
              <div className="flex items-center space-x-3 pl-3 border-l border-slate-200">
                {isAdmin && (
                  <Link
                    to="/admin"
                    className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-bold bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 transition"
                  >
                    <Shield className="w-3.5 h-3.5 text-amber-600" />
                    <span>Admin Hub</span>
                  </Link>
                )}
                <Link
                  to="/profile"
                  className="flex items-center space-x-2 py-1.5 px-3 rounded-lg hover:bg-slate-100 text-slate-700 transition"
                >
                  <div className="w-8 h-8 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center font-bold text-xs ring-2 ring-brand-200">
                    {user.name.charAt(0).toUpperCase()}
                  </div>
                  <div className="text-left">
                    <p className="text-xs font-semibold text-slate-900 leading-tight truncate max-w-[120px]">
                      {user.name}
                    </p>
                    <p className="text-[10px] text-brand-600 font-medium leading-none">
                      {user.role === 'USER' ? 'STUDENT' : user.role === 'SUPER_ADMIN' ? 'ADMIN' : user.role}
                    </p>
                  </div>
                </Link>
                <button
                  onClick={handleLogout}
                  title="Log out"
                  className="p-2 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-rose-50 transition"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg shadow-sm shadow-brand-500/30 transition"
                >
                  Get Started
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="flex items-center lg:hidden">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-b border-slate-200 bg-white px-4 pt-2 pb-4 space-y-1">
          {navLinks.map((link) => {
            const Icon = link.icon;
            return (
              <NavLink
                key={link.path}
                to={link.path}
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium ${
                    isActive
                      ? 'bg-brand-50 text-brand-700 font-semibold'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`
                }
              >
                <Icon className="w-5 h-5" />
                <span>{link.name}</span>
              </NavLink>
            );
          })}
          <div className="pt-3 mt-2 border-t border-slate-200">
            {isAuthenticated && user ? (
              <div className="space-y-2">
                {isAdmin && (
                  <Link
                    to="/admin"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-bold text-amber-800 bg-amber-50 hover:bg-amber-100 border border-amber-200"
                  >
                    <Shield className="w-5 h-5 text-amber-600" />
                    <span>Admin Dashboard</span>
                  </Link>
                )}
                <Link
                  to="/profile"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-100"
                >
                  <UserIcon className="w-5 h-5 text-brand-600" />
                  <span>Profile ({user.name})</span>
                </Link>
                <button
                  onClick={() => {
                    setMobileMenuOpen(false);
                    handleLogout();
                  }}
                  className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium text-rose-600 hover:bg-rose-50"
                >
                  <LogOut className="w-5 h-5" />
                  <span>Sign Out</span>
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-2 pt-1">
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="text-center py-2 px-3 border border-slate-300 rounded-lg text-sm font-medium text-slate-700"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  onClick={() => setMobileMenuOpen(false)}
                  className="text-center py-2 px-3 bg-brand-600 text-white rounded-lg text-sm font-semibold"
                >
                  Get Started
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};
