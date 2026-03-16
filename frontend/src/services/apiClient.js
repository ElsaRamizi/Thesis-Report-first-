import axios from 'axios';
import { isTokenExpired } from '../utils/jwt';
import { STORAGE_KEYS } from '../utils/storage';

let onUnauthorized = null;

export const registerUnauthorizedHandler = (handler) => {
  onUnauthorized = handler;
};

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.token);

  if (!token) {
    return config;
  }

  if (isTokenExpired(token)) {
    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.role);

    if (onUnauthorized) {
      onUnauthorized();
    }

    return Promise.reject(new axios.Cancel('Session expired. Please log in again.'));
  }

  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && onUnauthorized) {
      onUnauthorized();
    }

    return Promise.reject(error);
  },
);

export default apiClient;
