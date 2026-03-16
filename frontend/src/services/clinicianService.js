import apiClient from './apiClient';

export const getClinicianDashboard = async () => {
  const response = await apiClient.get('/api/clinician/dashboard');
  return response.data;
};
