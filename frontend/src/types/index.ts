export interface User {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'COLLEGE_ADMIN' | 'SUPER_ADMIN';
  assignedCollegeId?: number;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'COLLEGE_ADMIN' | 'SUPER_ADMIN';
  assignedCollegeId?: number;
}

export interface College {
  id: number;
  name: string;
  shortName?: string;
  location?: string;
  website?: string;
  logoUrl?: string;
  description?: string;
  active: boolean;
  processCount: number;
  processes?: CollegeProcessSummary[];
}

export interface CollegeProcessSummary {
  id: number;
  collegeId: number;
  title: string;
  category?: string;
  timeline?: string;
  fees?: string;
  status: string;
  stepCount: number;
  documentCount: number;
}

export interface CollegeProcessStep {
  id: number;
  processId: number;
  stepNumber: number;
  title: string;
  description?: string;
  estimatedTime?: string;
  notes?: string;
}

export interface CollegeRequiredDocument {
  id: number;
  processId: number;
  documentName: string;
  description?: string;
  mandatory: boolean;
  acceptedFormats?: string;
  howToObtain?: string;
}

export interface CollegeProcessUpdate {
  id?: number;
  processId?: number;
  title?: string;
  updateTitle?: string;
  description?: string;
  announcementDetails?: string;
  updateType?: string;
  effectiveDate?: string;
}

export interface CollegeProcessDetail {
  id: number;
  collegeId: number;
  collegeName?: string;
  collegeShortName?: string;
  title: string;
  description?: string;
  category?: string;
  eligibility?: string;
  eligibilityCriteria?: string;
  timeline?: string;
  fees?: string;
  status: string;
  importantNotes?: string;
  officialPortalUrl?: string;
  steps: CollegeProcessStep[];
  requiredDocuments: CollegeRequiredDocument[];
  updates: CollegeProcessUpdate[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CivicService {
  id: number;
  name: string;
  slug: string;
  category: string;
  description?: string;
  eligibility?: string;
  timeline?: string;
  fees?: string;
  importantNotes?: string;
  officialPortalUrl?: string;
  hasQuestionnaire?: boolean;
  steps: CivicServiceStep[];
  requiredDocuments: CivicServiceDocument[];
}

export interface QuestionOption {
  value: string;
  label: string;
  description?: string;
  badge?: string;
}

export interface QuestionnaireQuestion {
  id: string;
  title: string;
  subtitle?: string;
  dependsOn?: string;
  options: QuestionOption[];
}

export interface QuestionnaireData {
  serviceSlug: string;
  serviceName: string;
  title: string;
  description?: string;
  questions: QuestionnaireQuestion[];
}

export interface PathwayEvaluationRequest {
  answers: Record<string, string>;
}

export interface PathwayEvaluationResult {
  pathwayCode: string;
  pathwayTitle: string;
  badgeText?: string;
  matchReason: string;
  validity?: string;
  emigrationStatus?: string;
  policeVerificationMode: string;
  policeVerificationDetails: string;
  calculatedFee: string;
  feeBreakdown: string;
  estimatedTimeline: string;
  officialPortalUrl: string;
  documents: CivicServiceDocument[];
  steps: CivicServiceStep[];
  criticalTips: string[];
}

export interface CivicServiceStep {
  id: number;
  stepNumber: number;
  title: string;
  description?: string;
  estimatedTime?: string;
  notes?: string;
}

export interface CivicServiceDocument {
  id: number;
  documentName: string;
  description?: string;
  mandatory: boolean;
  acceptedFormats?: string;
  howToObtain?: string;
}

export interface PlatformUpdate {
  id: number;
  title: string;
  description: string;
  category: string;
  relatedServiceOrProcess?: string;
  effectiveDate?: string;
  urgent: boolean;
  officialSourceUrl?: string;
  createdAt: string;
}

export interface ServiceCenter {
  id: number;
  name: string;
  slug?: string;
  category?: 'PASSPORT' | 'DRIVING_LICENCE' | 'CERTIFICATES' | string;
  serviceType?: string;
  address?: string;
  locality?: string;
  city?: string;
  district?: string;
  state?: string;
  pincode?: string;
  contactNumber?: string;
  email?: string;
  operatingHours?: string;
  supportedServices?: string;
  isGovernment?: boolean;
  isRpo?: boolean;
  officialWebsite?: string;
  verificationStatus?: string;
  lastVerified?: string;
  latitude?: number;
  longitude?: number;
}

export interface ChecklistItem {
  id?: number;
  userId?: number;
  targetType: 'COLLEGE_PROCESS' | 'SERVICE' | 'PERSONAL';
  targetId: number;
  documentName: string;
  completed: boolean;
  isMissing: boolean;
  userNotes?: string;
  updatedAt?: string;
}

export interface ChatMessage {
  id?: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string;
  suggestedActions?: string[];
  isError?: boolean;
}

export interface ChatRequest {
  message: string;
  conversationHistory?: Array<{ role: string; content: string }>;
  currentPage?: string;
}

export interface ChatResponse {
  reply: string;
  suggestedActions?: string[];
  configured: boolean;
  error: boolean;
}

export interface UserDocument {
  id: number;
  userId: number;
  documentName: string;
  category?: string;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  createdAt: string;
  updatedAt: string;
}

export type MentorRequestStatus = 'NEW' | 'CONTACTED' | 'ASSIGNED' | 'COMPLETED';

export interface MentorRequestCreate {
  fullName: string;
  email: string;
  departmentOrProcess: string;
  problemDescription: string;
  desiredOutcome: string;
  preferredContactMethod: 'Phone call' | 'WhatsApp' | 'Email' | string;
  phoneNumber?: string;
  additionalInfo?: string;
}

export interface MentorRequest {
  id: number;
  requestId: string;
  userId?: number;
  fullName: string;
  email: string;
  departmentOrProcess: string;
  problemDescription: string;
  desiredOutcome: string;
  preferredContactMethod: string;
  phoneNumber?: string;
  additionalInfo?: string;
  status: MentorRequestStatus;
  adminNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MentorStatusUpdate {
  status: MentorRequestStatus;
  adminNotes?: string;
}


