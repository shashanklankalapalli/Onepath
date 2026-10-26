import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { collegeService } from '../services/api';
import { ErrorAlert } from '../components/ErrorAlert';
import { GraduationCap, CheckCircle2, ArrowRight, ArrowLeft } from 'lucide-react';

export const ManageCollegePage: React.FC = () => {
  const [name, setName] = useState('');
  const [shortName, setShortName] = useState('');
  const [location, setLocation] = useState('');
  const [website, setWebsite] = useState('');
  const [description, setDescription] = useState('');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [createdCollegeId, setCreatedCollegeId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);

    try {
      const created = await collegeService.createCollege({
        name: name.trim(),
        shortName: shortName.trim() || undefined,
        location: location.trim() || undefined,
        website: website.trim() || undefined,
        description: description.trim() || undefined,
        active: true,
      });
      setCreatedCollegeId(created.id);
      // Navigate straight to the institution page where admin can immediately see the profile and add processes
      navigate(`/colleges/${created.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to register institution. Admin authorization required.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      <Link
        to="/colleges"
        className="inline-flex items-center text-sm font-semibold text-slate-600 hover:text-slate-900"
      >
        <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Dashboard
      </Link>

      <div>
        <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
          Register Institution
        </h1>
        <p className="text-slate-600 mt-1 text-sm">
          Add an authorized university or college entity. Once registered, you will be redirected to manage its processes and step-by-step checklists.
        </p>
      </div>

      {error && <ErrorAlert message={error} />}

      <form onSubmit={handleSubmit} className="bg-white rounded-3xl border border-slate-200/80 p-8 shadow-sm space-y-5">
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            College / University Name *
          </label>
          <input
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. National Institute of Technology Karnataka"
            className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Short Acronym / Code
            </label>
            <input
              type="text"
              value={shortName}
              onChange={(e) => setShortName(e.target.value)}
              placeholder="e.g. NITK"
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Campus City / Location
            </label>
            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g. Surathkal, Mangalore"
              className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Official Portal URL
          </label>
          <input
            type="text"
            value={website}
            onChange={(e) => setWebsite(e.target.value)}
            placeholder="e.g. https://www.nitk.ac.in"
            className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Institutional Description
          </label>
          <textarea
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Overview of the institution, departments, and services offered..."
            className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:bg-white"
          />
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3 px-4 rounded-xl text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 transition shadow-sm disabled:opacity-50 flex items-center justify-center"
        >
          {isSubmitting ? 'Registering Institution...' : 'Save & Publish Institution'}
          {!isSubmitting && <ArrowRight className="w-4 h-4 ml-1.5" />}
        </button>
      </form>
    </div>
  );
};
