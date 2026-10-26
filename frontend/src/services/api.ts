import { apiClient } from '../api/client';
import {
  User,
  AuthResponse,
  College,
  CollegeProcessSummary,
  CollegeProcessDetail,
  CollegeProcessUpdate,
  CivicService,
  QuestionnaireData,
  PathwayEvaluationRequest,
  PathwayEvaluationResult,
  PlatformUpdate,
  ServiceCenter,
  ChecklistItem,
  UserDocument,
  ChatRequest,
  ChatResponse,
} from '../types';

export const authService = {
  login: async (credentials: { email: string; password: string }): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return res.data;
  },
  register: async (data: { name: string; email: string; password: string; role?: string; assignedCollegeId?: number }): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/auth/register', data);
    return res.data;
  },
  getCurrentUser: async (): Promise<User> => {
    const res = await apiClient.get<User>('/auth/me');
    return res.data;
  },
};

export const collegeService = {
  getAll: async (search?: string): Promise<College[]> => {
    const params = search ? { search } : {};
    const res = await apiClient.get<College[]>('/colleges', { params });
    return res.data;
  },
  getById: async (id: number | string): Promise<College> => {
    const res = await apiClient.get<College>(`/colleges/${id}`);
    return res.data;
  },
  getProcesses: async (collegeId: number | string, category?: string): Promise<CollegeProcessSummary[]> => {
    const params = category ? { category } : {};
    const res = await apiClient.get<CollegeProcessSummary[]>(`/colleges/${collegeId}/processes`, { params });
    return res.data;
  },
  getProcessDetail: async (collegeId: number | string, processId: number | string): Promise<CollegeProcessDetail> => {
    const res = await apiClient.get<CollegeProcessDetail>(`/colleges/${collegeId}/processes/${processId}`);
    return res.data;
  },
  createCollege: async (data: Partial<College>): Promise<College> => {
    const res = await apiClient.post<College>('/colleges', data);
    return res.data;
  },
  updateCollege: async (id: number | string, data: Partial<College>): Promise<College> => {
    const res = await apiClient.put<College>(`/colleges/${id}`, data);
    return res.data;
  },
  deleteCollege: async (id: number | string): Promise<void> => {
    await apiClient.delete(`/colleges/${id}`);
  },
  createProcess: async (collegeId: number | string, data: Partial<CollegeProcessDetail>): Promise<CollegeProcessDetail> => {
    const res = await apiClient.post<CollegeProcessDetail>(`/colleges/${collegeId}/processes`, data);
    return res.data;
  },
  updateProcess: async (collegeId: number | string, processId: number | string, data: Partial<CollegeProcessDetail>): Promise<CollegeProcessDetail> => {
    const res = await apiClient.put<CollegeProcessDetail>(`/colleges/${collegeId}/processes/${processId}`, data);
    return res.data;
  },
  deleteProcess: async (collegeId: number | string, processId: number | string): Promise<void> => {
    await apiClient.delete(`/colleges/${collegeId}/processes/${processId}`);
  },
  getProcessUpdates: async (collegeId: number | string, processId: number | string): Promise<CollegeProcessUpdate[]> => {
    const res = await apiClient.get<CollegeProcessUpdate[]>(`/colleges/${collegeId}/processes/${processId}/updates`);
    return res.data;
  },
  addProcessUpdate: async (collegeId: number | string, processId: number | string, data: Partial<CollegeProcessUpdate>): Promise<CollegeProcessUpdate> => {
    const res = await apiClient.post<CollegeProcessUpdate>(`/colleges/${collegeId}/processes/${processId}/updates`, data);
    return res.data;
  },
};

export const civicServicesApi = {
  getAll: async (category?: string, search?: string): Promise<CivicService[]> => {
    const params: Record<string, string> = {};
    if (category) params.category = category;
    if (search) params.search = search;
    const res = await apiClient.get<CivicService[]>('/services', { params });
    return res.data;
  },
  getByIdOrSlug: async (identifier: string): Promise<CivicService> => {
    const res = await apiClient.get<CivicService>(`/services/${identifier}`);
    return res.data;
  },
  getQuestionnaire: async (identifier: string): Promise<QuestionnaireData> => {
    const res = await apiClient.get<QuestionnaireData>(`/services/${identifier}/questionnaire`);
    return res.data;
  },
  evaluatePathway: async (identifier: string, request: PathwayEvaluationRequest): Promise<PathwayEvaluationResult> => {
    const res = await apiClient.post<PathwayEvaluationResult>(`/services/${identifier}/evaluate`, request);
    return res.data;
  },
};

export const updatesApi = {
  getAll: async (category?: string, urgent?: boolean): Promise<PlatformUpdate[]> => {
    const params: Record<string, any> = {};
    if (category) params.category = category;
    if (urgent !== undefined) params.urgent = urgent;
    const res = await apiClient.get<PlatformUpdate[]>('/updates', { params });
    return res.data;
  },
};

export const serviceCentersApi = {
  getAll: async (search?: string, city?: string, category?: string, serviceType?: string): Promise<ServiceCenter[]> => {
    const params: Record<string, string> = {};
    if (search) params.search = search;
    if (city && city !== 'ALL') params.city = city;
    if (category && category !== 'ALL') params.category = category;
    if (serviceType && serviceType !== 'ALL') params.serviceType = serviceType;
    const res = await apiClient.get<ServiceCenter[]>('/service-centers', { params });
    return res.data;
  },
  getById: async (id: number | string): Promise<ServiceCenter> => {
    const res = await apiClient.get<ServiceCenter>(`/service-centers/${id}`);
    return res.data;
  },
};

export const checklistApi = {
  getByTarget: async (targetType: string, targetId: number | string): Promise<ChecklistItem[]> => {
    const res = await apiClient.get<ChecklistItem[]>('/checklists', {
      params: { targetType, targetId },
    });
    return res.data;
  },
  getAllUserItems: async (): Promise<ChecklistItem[]> => {
    const res = await apiClient.get<ChecklistItem[]>('/checklists', {
      params: { targetType: 'PERSONAL' },
    });
    return res.data;
  },
  getPersonalDocuments: async (): Promise<ChecklistItem[]> => {
    const res = await apiClient.get<ChecklistItem[]>('/checklists', {
      params: { targetType: 'PERSONAL' },
    });
    return res.data;
  },
  getMissing: async (): Promise<ChecklistItem[]> => {
    const res = await apiClient.get<ChecklistItem[]>('/checklists/missing', {
      params: { targetType: 'PERSONAL' },
    });
    return res.data;
  },
  saveItem: async (item: ChecklistItem): Promise<ChecklistItem> => {
    const res = await apiClient.post<ChecklistItem>('/checklists', item);
    return res.data;
  },
  updateItem: async (id: number, item: Partial<ChecklistItem>): Promise<ChecklistItem> => {
    const res = await apiClient.put<ChecklistItem>(`/checklists/${id}`, item);
    return res.data;
  },
  deleteItem: async (id: number): Promise<void> => {
    await apiClient.delete(`/checklists/${id}`);
  },
};

export const chatbotApi = {
  sendMessage: async (request: ChatRequest): Promise<ChatResponse> => {
    const res = await apiClient.post<ChatResponse>('/chat', request);
    return res.data;
  },
};

export const userDocumentsApi = {
  getAll: async (category?: string): Promise<UserDocument[]> => {
    const params = category && category !== 'ALL' ? { category } : {};
    const res = await apiClient.get<UserDocument[]>('/documents', { params });
    return res.data;
  },
  upload: async (file: File, documentName: string, category?: string): Promise<UserDocument> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentName', documentName);
    if (category) formData.append('category', category);
    const res = await apiClient.post<UserDocument>('/documents', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },
  viewFile: async (id: number): Promise<void> => {
    const res = await apiClient.get(`/documents/${id}/file`, {
      responseType: 'blob',
    });
    const contentType = (res.headers['content-type'] as string) || 'application/pdf';
    const blob = new Blob([res.data], { type: contentType });
    const blobUrl = window.URL.createObjectURL(blob);
    window.open(blobUrl, '_blank');
  },
  downloadFile: async (id: number, originalFileName: string): Promise<void> => {
    const res = await apiClient.get(`/documents/${id}/file`, {
      params: { download: true },
      responseType: 'blob',
    });
    const contentType = (res.headers['content-type'] as string) || 'application/octet-stream';
    const blob = new Blob([res.data], { type: contentType });
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.setAttribute('download', originalFileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  },
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/documents/${id}`);
  },
};

