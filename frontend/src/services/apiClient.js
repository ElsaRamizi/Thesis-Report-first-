import axios from 'axios';
import { STORAGE_KEYS } from '../utils/storage';

let onUnauthorized = null;
let refreshPromise = null;

/// AuthContext registers this — called when we get kicked out (401)
export const registerUnauthorizedHandler = (handler) => {
  onUnauthorized = handler;
};

// shared axios instance for whole app
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // important — browser sends JWT cookies
});

// if request fails with 401, try refresh once then retry
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isAuthRoute = originalRequest?.url?.includes('/api/auth/login')
      || originalRequest?.url?.includes('/api/auth/register')
      || originalRequest?.url?.includes('/api/auth/refresh');

    if (error.response?.status === 401 && !originalRequest?._retry && !isAuthRoute) {
      originalRequest._retry = true;

      try {
        if (!refreshPromise) {
          refreshPromise = apiClient.post('/api/auth/refresh').finally(() => {
            refreshPromise = null;
          });
        }
        await refreshPromise;
        return apiClient(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem(STORAGE_KEYS.role);
        if (onUnauthorized) {
          onUnauthorized();
        }
      }
    }

    if (error.response?.status === 401 && onUnauthorized && !isAuthRoute) {
      localStorage.removeItem(STORAGE_KEYS.role);
      onUnauthorized();
    }

    return Promise.reject(error);
  },
);

export default apiClient;
