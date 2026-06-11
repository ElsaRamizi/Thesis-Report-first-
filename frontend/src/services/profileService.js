import apiClient from './apiClient';

export const fetchOwnProfile = async () => {
  const response = await apiClient.get('/api/user/profile');
  return response.data;
};

export const updateOwnProfile = async (payload) => {
  const response = await apiClient.put('/api/user/profile', payload);
  return response.data;
};
