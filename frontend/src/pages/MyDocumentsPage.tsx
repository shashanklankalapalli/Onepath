import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { userDocumentsApi } from '../services/api';
import { UserDocument } from '../types';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorAlert } from '../components/ErrorAlert';
import {
  UploadCloud,
  FileText,
  Image as ImageIcon,
  Download,
  Eye,
  Trash2,
  Plus,
  X,
  AlertCircle,
  CheckCircle2,
  ShieldCheck,
  HardDrive,
  Calendar,
  FileCheck2,
  Sparkles,
} from 'lucide-react';

const CATEGORIES = [
  { id: 'ALL', label: 'All Documents' },
  { id: 'IDENTITY', label: 'Identity Proofs' },
  { id: 'EDUCATION', label: 'Educational Certificates' },
  { id: 'INCOME_CASTE', label: 'Income & Caste' },
  { id: 'VEHICLE_PROPERTY', label: 'Vehicle & Property' },
  { id: 'GENERAL', label: 'Other Documents' },
];

const CATEGORY_LABELS: Record<string, string> = {
  IDENTITY: 'Identity Proof',
  EDUCATION: 'Educational Certificate',
  INCOME_CASTE: 'Income & Caste Certificate',
  VEHICLE_PROPERTY: 'Vehicle & Property Record',
  GENERAL: 'General Document',
};

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

export const MyDocumentsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const activeCategory = searchParams.get('category') || 'ALL';
  const { isAuthenticated } = useAuth();

  const [documents, setDocuments] = useState<UserDocument[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [feedbackSuccess, setFeedbackSuccess] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);

  // Upload Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [docName, setDocName] = useState<string>('');
  const [category, setCategory] = useState<string>('IDENTITY');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [modalError, setModalError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Delete Confirmation Modal State
  const [docToDelete, setDocToDelete] = useState<UserDocument | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  const fetchDocuments = async () => {
    if (!isAuthenticated) {
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const data = await userDocumentsApi.getAll(activeCategory);
      setDocuments(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load your uploaded documents.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, [isAuthenticated, activeCategory]);

  const handleOpenUploadModal = () => {
    setDocName('');
    setCategory('IDENTITY');
    setSelectedFile(null);
    setModalError(null);
    setIsModalOpen(true);
  };

  const handleCloseUploadModal = () => {
    if (isUploading) return;
    setIsModalOpen(false);
    setSelectedFile(null);
    setDocName('');
    setModalError(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setModalError(null);
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];

      // Validate file size (10 MB)
      if (file.size > MAX_FILE_SIZE_BYTES) {
        setModalError(`Selected file exceeds 10 MB limit (${(file.size / (1024 * 1024)).toFixed(2)} MB). Please choose a smaller file.`);
        if (fileInputRef.current) fileInputRef.current.value = '';
        setSelectedFile(null);
        return;
      }

      // Validate extension
      const validExtensions = ['.pdf', '.jpg', '.jpeg', '.png'];
      const fileExt = '.' + file.name.split('.').pop()?.toLowerCase();
      if (!validExtensions.includes(fileExt)) {
        setModalError('Unsupported file type. Please select a PDF, JPG, JPEG, or PNG file.');
        if (fileInputRef.current) fileInputRef.current.value = '';
        setSelectedFile(null);
        return;
      }

      setSelectedFile(file);
      // Auto-populate document name if empty
      if (!docName.trim()) {
        const baseName = file.name.replace(/\.[^/.]+$/, '').replace(/[-_]/g, ' ');
        const capitalized = baseName.charAt(0).toUpperCase() + baseName.slice(1);
        setDocName(capitalized);
      }
    }
  };

  const handleUploadSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!docName.trim()) {
      setModalError('Please enter a document name.');
      return;
    }
    if (!selectedFile) {
      setModalError('Please select a file to upload.');
      return;
    }

    setIsUploading(true);
    setModalError(null);

    try {
      const uploaded = await userDocumentsApi.upload(selectedFile, docName.trim(), category);
      setDocuments((prev) => [uploaded, ...prev]);
      handleCloseUploadModal();
      setFeedbackSuccess(`"${uploaded.documentName}" was uploaded successfully.`);
      setTimeout(() => setFeedbackSuccess(null), 4000);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Upload failed. Please check file format and try again.';
      setModalError(msg);
    } finally {
      setIsUploading(false);
    }
  };

  const handleView = async (doc: UserDocument) => {
    setActionLoadingId(doc.id);
    try {
      await userDocumentsApi.viewFile(doc.id);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Could not view file. Please ensure you are logged in.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleDownload = async (doc: UserDocument) => {
    setActionLoadingId(doc.id);
    try {
      await userDocumentsApi.downloadFile(doc.id, doc.originalFileName);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Download failed. Please try again.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!docToDelete) return;
    setIsDeleting(true);
    try {
      await userDocumentsApi.delete(docToDelete.id);
      setDocuments((prev) => prev.filter((d) => d.id !== docToDelete.id));
      const deletedName = docToDelete.documentName;
      setDocToDelete(null);
      setFeedbackSuccess(`"${deletedName}" has been removed from your vault.`);
      setTimeout(() => setFeedbackSuccess(null), 4000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete document.');
    } finally {
      setIsDeleting(false);
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (!bytes || bytes === 0) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  const formatDate = (dateStr: string): string => {
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  const isPdf = (contentType: string, fileName: string): boolean => {
    return Boolean(
      (contentType && contentType.toLowerCase().includes('pdf')) ||
      (fileName && fileName.toLowerCase().endsWith('.pdf'))
    );
  };

  if (!isAuthenticated) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <EmptyState
          title="Sign in to access your Document Vault"
          description="Securely upload and store your personal documents, identity cards, educational certificates, and revenue proofs in one encrypted place."
          icon={FileCheck2}
          actionLabel="Sign In or Register"
          onAction={() => (window.location.href = '/login')}
        />
      </div>
    );
  }

  // Calculate vault stats
  const totalDocs = documents.length;
  const pdfCount = documents.filter((d) => isPdf(d.contentType, d.originalFileName)).length;
  const imageCount = totalDocs - pdfCount;
  const totalSizeBytes = documents.reduce((acc, curr) => acc + (curr.fileSize || 0), 0);

  const filteredDocs =
    activeCategory === 'ALL'
      ? documents
      : documents.filter((d) => (d.category || 'GENERAL').toUpperCase() === activeCategory.toUpperCase());

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-bold uppercase tracking-wider">
              <ShieldCheck className="w-3.5 h-3.5" /> Secure Vault
            </span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-950 tracking-tight">
            My Documents
          </h1>
          <p className="text-slate-600 mt-1 text-sm sm:text-base">
            Upload and securely store your personal certificates, Aadhaar, PAN, and identity proofs for government and university applications.
          </p>
        </div>

        <button
          type="button"
          id="add-document-btn"
          onClick={handleOpenUploadModal}
          className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white font-bold text-sm shadow-md shadow-brand-600/20 transition flex-shrink-0"
        >
          <Plus className="w-4 h-4 stroke-[3]" /> Upload Document
        </button>
      </div>

      {/* Metrics Bar */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center space-x-3.5">
          <div className="w-10 h-10 rounded-xl bg-brand-50 text-brand-700 flex items-center justify-center flex-shrink-0">
            <HardDrive className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500">Total Uploaded</p>
            <p className="text-lg font-black text-slate-900 mt-0.5">{totalDocs}</p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center space-x-3.5">
          <div className="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center flex-shrink-0">
            <FileText className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500">PDF Documents</p>
            <p className="text-lg font-black text-rose-600 mt-0.5">{pdfCount}</p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center space-x-3.5">
          <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center flex-shrink-0">
            <ImageIcon className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500">Image Files</p>
            <p className="text-lg font-black text-indigo-600 mt-0.5">{imageCount}</p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center space-x-3.5">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center flex-shrink-0">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500">Vault Storage</p>
            <p className="text-lg font-black text-emerald-600 mt-0.5">{formatFileSize(totalSizeBytes)}</p>
          </div>
        </div>
      </div>

      {/* Success Notification Alert */}
      {feedbackSuccess && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-900 text-sm font-semibold flex items-center gap-2.5 shadow-sm">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          <span>{feedbackSuccess}</span>
        </div>
      )}

      {/* Global Error Alert */}
      {error && <ErrorAlert message={error} onRetry={fetchDocuments} />}

      {/* Category Tabs */}
      <div className="flex border-b border-slate-200 overflow-x-auto no-scrollbar gap-1">
        {CATEGORIES.map((cat) => {
          const isActive = activeCategory === cat.id;
          return (
            <button
              key={cat.id}
              onClick={() => {
                if (cat.id === 'ALL') {
                  setSearchParams({});
                } else {
                  setSearchParams({ category: cat.id });
                }
              }}
              className={`pb-3 px-4 text-xs sm:text-sm font-semibold border-b-2 transition whitespace-nowrap ${
                isActive
                  ? 'border-brand-600 text-brand-600'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              {cat.label}
            </button>
          );
        })}
      </div>

      {/* Document Content List */}
      {isLoading ? (
        <LoadingSpinner message="Loading your documents vault..." />
      ) : filteredDocs.length === 0 ? (
        <div className="bg-white rounded-3xl border border-slate-200 p-8 sm:p-12 text-center space-y-4 shadow-sm">
          <div className="w-16 h-16 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center mx-auto">
            <UploadCloud className="w-8 h-8" />
          </div>
          <div className="max-w-md mx-auto space-y-1.5">
            <h3 className="text-lg font-bold text-slate-900">
              {activeCategory === 'ALL'
                ? 'Your document vault is empty'
                : `No documents uploaded in "${CATEGORIES.find((c) => c.id === activeCategory)?.label || activeCategory}"`}
            </h3>
            <p className="text-xs sm:text-sm text-slate-500 leading-relaxed">
              Upload your official documents (Aadhaar, PAN, marksheets, certificates) in PDF, JPG, or PNG format up to 10 MB. They are saved securely and remain available whenever you need them.
            </p>
          </div>
          <button
            type="button"
            onClick={handleOpenUploadModal}
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-semibold text-sm shadow-md shadow-brand-600/20 transition"
          >
            <Plus className="w-4 h-4 stroke-[3]" /> Upload Document
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredDocs.map((doc) => {
            const isPdfFile = isPdf(doc.contentType, doc.originalFileName);
            const isActionLoading = actionLoadingId === doc.id;

            return (
              <div
                key={doc.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:border-brand-300 transition flex flex-col justify-between space-y-4"
              >
                <div className="flex items-start gap-3.5">
                  <div
                    className={`w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0 ${
                      isPdfFile ? 'bg-rose-50 text-rose-600' : 'bg-indigo-50 text-indigo-600'
                    }`}
                  >
                    {isPdfFile ? <FileText className="w-6 h-6" /> : <ImageIcon className="w-6 h-6" />}
                  </div>

                  <div className="flex-1 min-w-0 space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="text-base font-bold text-slate-950 truncate" title={doc.documentName}>
                        {doc.documentName}
                      </h3>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 border border-slate-200">
                        {CATEGORY_LABELS[doc.category || 'GENERAL'] || doc.category || 'General'}
                      </span>
                    </div>

                    <p className="text-xs text-slate-500 truncate" title={doc.originalFileName}>
                      {doc.originalFileName}
                    </p>

                    <div className="flex items-center gap-3 text-[11px] text-slate-400 pt-1">
                      <span className="flex items-center gap-1">
                        <HardDrive className="w-3 h-3" /> {formatFileSize(doc.fileSize)}
                      </span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3" /> {formatDate(doc.createdAt)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Card Actions */}
                <div className="pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleView(doc)}
                      disabled={isActionLoading}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200 transition disabled:opacity-50"
                      title="View original file in new browser tab"
                    >
                      <Eye className="w-3.5 h-3.5" /> View
                    </button>

                    <button
                      type="button"
                      onClick={() => handleDownload(doc)}
                      disabled={isActionLoading}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 border border-slate-200 transition disabled:opacity-50"
                      title="Download original file"
                    >
                      <Download className="w-3.5 h-3.5" /> Download
                    </button>
                  </div>

                  <button
                    type="button"
                    onClick={() => setDocToDelete(doc)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition"
                    title="Delete document from vault"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* UPLOAD DOCUMENT MODAL */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl border border-slate-100 space-y-6 relative max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-xl font-extrabold text-slate-950 flex items-center gap-2">
                  <UploadCloud className="w-5 h-5 text-brand-600" /> Upload Document
                </h3>
                <p className="text-xs text-slate-500 mt-1">
                  Upload your original document to your private vault (PDF, JPG, PNG up to 10 MB).
                </p>
              </div>
              <button
                type="button"
                onClick={handleCloseUploadModal}
                disabled={isUploading}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {modalError && (
              <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold flex items-start gap-2">
                <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
                <span>{modalError}</span>
              </div>
            )}

            <form onSubmit={handleUploadSubmit} className="space-y-4">
              {/* Document Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-700 block">
                  Document Name <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  placeholder="e.g. Aadhaar Card, 10th Marksheet, Income Certificate"
                  value={docName}
                  onChange={(e) => setDocName(e.target.value)}
                  disabled={isUploading}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 text-sm font-medium"
                />
              </div>

              {/* Document Category */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-700 block">
                  Document Category
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  disabled={isUploading}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500 text-sm font-medium"
                >
                  <option value="IDENTITY">Identity Proof (Aadhaar, Voter ID, Passport, PAN)</option>
                  <option value="EDUCATION">Educational Certificate (10th, 12th, Degree, Diploma)</option>
                  <option value="INCOME_CASTE">Income, Caste & Domicile Certificate</option>
                  <option value="VEHICLE_PROPERTY">Vehicle & Property Records (DL, RC, Sale Deed)</option>
                  <option value="GENERAL">General / Other Document</option>
                </select>
              </div>

              {/* File Picker Zone */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-700 block">
                  Select File <span className="text-rose-500">*</span>
                </label>

                {!selectedFile ? (
                  <div
                    onClick={() => fileInputRef.current?.click()}
                    className="cursor-pointer border-2 border-dashed border-slate-300 hover:border-brand-500 hover:bg-brand-50/20 rounded-2xl p-6 text-center transition space-y-2"
                  >
                    <UploadCloud className="w-8 h-8 text-slate-400 mx-auto" />
                    <div className="text-sm font-bold text-slate-700">
                      Click to choose or drag a document file
                    </div>
                    <p className="text-xs text-slate-400">
                      Supported formats: PDF, JPG, JPEG, PNG (Max 10 MB)
                    </p>
                  </div>
                ) : (
                  <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 flex items-center justify-between gap-3">
                    <div className="flex items-center space-x-3 overflow-hidden">
                      <div className="w-10 h-10 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center flex-shrink-0">
                        {selectedFile.type.includes('pdf') || selectedFile.name.endsWith('.pdf') ? (
                          <FileText className="w-5 h-5 text-rose-600" />
                        ) : (
                          <ImageIcon className="w-5 h-5 text-indigo-600" />
                        )}
                      </div>
                      <div className="overflow-hidden">
                        <p className="text-xs font-bold text-slate-900 truncate">
                          {selectedFile.name}
                        </p>
                        <p className="text-[11px] text-slate-500">
                          {formatFileSize(selectedFile.size)}
                        </p>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedFile(null);
                        if (fileInputRef.current) fileInputRef.current.value = '';
                      }}
                      disabled={isUploading}
                      className="text-xs font-bold text-slate-500 hover:text-slate-800 px-2 py-1 rounded transition flex-shrink-0"
                    >
                      Change
                    </button>
                  </div>
                )}

                <input
                  ref={fileInputRef}
                  type="file"
                  id="document-file-input"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                  onChange={handleFileChange}
                  className="hidden"
                />
              </div>

              {/* Action Buttons */}
              <div className="pt-4 flex items-center justify-end gap-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={handleCloseUploadModal}
                  disabled={isUploading}
                  className="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-700 text-xs font-bold hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  id="submit-upload-btn"
                  disabled={isUploading || !docName.trim() || !selectedFile}
                  className="px-5 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold shadow-md shadow-brand-600/20 transition flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isUploading ? (
                    <>
                      <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      Uploading & Saving...
                    </>
                  ) : (
                    <>
                      <UploadCloud className="w-4 h-4" /> Upload & Save
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {docToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-100 space-y-4">
            <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
              <Trash2 className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1.5">
              <h3 className="text-lg font-bold text-slate-950">
                Delete &ldquo;{docToDelete.documentName}&rdquo;?
              </h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                This document will be permanently removed from your vault. The physical file &ldquo;{docToDelete.originalFileName}&rdquo; will also be deleted from disk.
              </p>
            </div>

            <div className="pt-2 flex items-center justify-center gap-3">
              <button
                type="button"
                onClick={() => setDocToDelete(null)}
                disabled={isDeleting}
                className="px-4 py-2 rounded-xl border border-slate-200 text-slate-700 text-xs font-bold hover:bg-slate-50 transition"
              >
                Cancel
              </button>
              <button
                type="button"
                id="confirm-delete-btn"
                onClick={handleDeleteConfirm}
                disabled={isDeleting}
                className="px-5 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold shadow-md shadow-rose-600/20 transition flex items-center gap-1.5"
              >
                {isDeleting ? (
                  <>
                    <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Deleting...
                  </>
                ) : (
                  'Yes, Delete'
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
