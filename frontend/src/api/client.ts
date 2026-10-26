import axios from 'axios';

/**
 * Dynamically resolves the backend API base URL:
 * 1. Preserves explicit environment variable if set to a non-localhost remote URL or proxy path (e.g. '/api').
 * 2. In browser environments:
 *    - If accessed on the laptop via localhost or 127.0.0.1, routes to http://localhost:8080/api (or 127.0.0.1:8080/api).
 *    - If accessed on a mobile phone or other LAN device via laptop's IP (e.g. http://10.212.53.153:5173),
 *      dynamically uses window.location.hostname so requests go to http://10.212.53.153:8080/api instead of
 *      pointing to localhost on the phone itself.
 * 3. Configurable via VITE_API_BASE_URL or VITE_BACKEND_PORT.
 */
export const getApiBaseUrl = (): string => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;

  if (typeof window !== 'undefined' && window.location && window.location.hostname) {
    const hostname = window.location.hostname;
    const protocol = window.location.protocol || 'http:';
    const backendPort = import.meta.env.VITE_BACKEND_PORT || '8080';

    // When accessed from a remote device / phone (hostname is not localhost or 127.0.0.1):
    if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
      // Relative URL for reverse proxy (e.g. '/api')
      if (envUrl && envUrl.startsWith('/')) {
        return envUrl;
      }
      // If envUrl is undefined or was set to localhost/127.0.0.1, route to laptop's LAN IP
      if (!envUrl || envUrl.includes('localhost') || envUrl.includes('127.0.0.1')) {
        return `${protocol}//${hostname}:${backendPort}/api`;
      }
      return envUrl;
    }

    // When accessed on the laptop directly:
    if (envUrl) {
      return envUrl;
    }
    return `${protocol}//${hostname}:${backendPort}/api`;
  }

  return envUrl || 'http://localhost:8080/api';
};

export const API_BASE_URL = getApiBaseUrl();

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('onepath_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle unauthenticated 401 gracefully
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Optional: Clear token if session expired
      // localStorage.removeItem('onepath_token');
    }
    return Promise.reject(error);
  }
);
