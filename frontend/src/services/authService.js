import apiClient from './apiClient';

// thin wrappers around /api/auth — used by AuthContext

/// POST register — no login yet, just creates account
export const registerUser = async (payload) => {
  const response = await apiClient.post('/api/auth/register', payload);
  return response.data;
};

/// POST login — backend sets httpOnly cookies, we get { email, role } back
export const loginUser = async (payload) => {
  const response = await apiClient.post('/api/auth/login', payload);
  return response.data;
};

/// GET me — check if cookie still valid on page load
export const fetchCurrentUser = async () => {
  const response = await apiClient.get('/api/auth/me');
  return response.data;
};

/// POST logout — clears cookies on server
export const logoutUser = async () => {
  await apiClient.post('/api/auth/logout');
};

/// POST refresh — get new access JWT without re-entering password
export const refreshSession = async () => {
  const response = await apiClient.post('/api/auth/refresh');
  return response.data;
};
