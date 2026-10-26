import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ErrorAlert } from '../components/ErrorAlert';
import { Lock, Mail, User as UserIcon, ArrowRight, Building } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<'USER' | 'COLLEGE_ADMIN'>('USER');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await register({ name, email, password, role });
      navigate('/profile');
    } catch (err: any) {
      setError(
        err.response?.data?.message || 'Registration failed. Please check your inputs.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md bg-white rounded-3xl border border-slate-200/80 p-8 shadow-sm">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-2xl bg-brand-600 text-white flex items-center justify-center font-black text-xl mx-auto mb-3 shadow-md shadow-brand-500/20">
            1P
          </div>
          <h1 className="text-2xl font-black text-slate-950">Join OnePath</h1>
          <p className="text-sm text-slate-500 mt-1">
            Create an account to streamline your applications
          </p>
        </div>

        {error && <ErrorAlert message={error} className="mb-6" />}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Full Name
            </label>
            <div className="relative">
              <UserIcon className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Shashank Rao"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Email Address
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="student@example.edu"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Password
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                required
                minLength={6}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="At least 6 characters"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Account Type
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setRole('USER')}
                className={`py-2 px-3 rounded-xl text-xs font-semibold border text-center transition ${
                  role === 'USER'
                    ? 'bg-brand-50 border-brand-500 text-brand-700'
                    : 'bg-slate-50 border-slate-200 text-slate-600'
                }`}
              >
                Student / Youth
              </button>
              <button
                type="button"
                onClick={() => setRole('COLLEGE_ADMIN')}
                className={`py-2 px-3 rounded-xl text-xs font-semibold border text-center transition ${
                  role === 'COLLEGE_ADMIN'
                    ? 'bg-brand-50 border-brand-500 text-brand-700'
                    : 'bg-slate-50 border-slate-200 text-slate-600'
                }`}
              >
                College Admin
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3 px-4 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 transition shadow-sm disabled:opacity-50 flex items-center justify-center mt-2"
          >
            {isSubmitting ? 'Creating Account...' : 'Register'}
            {!isSubmitting && <ArrowRight className="w-4 h-4 ml-1.5" />}
          </button>
        </form>

        <div className="mt-6 text-center text-xs text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-brand-600 hover:text-brand-700">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};
