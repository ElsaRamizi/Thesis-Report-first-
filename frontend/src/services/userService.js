import apiClient from './apiClient';

export const getUserProfile = async () => {
  const response = await apiClient.get('/api/user/profile');
  return response.data;
};
