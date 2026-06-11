import apiClient from './apiClient';

export const fetchDoctorConnections = async () => {
  const response = await apiClient.get('/api/user/doctor-connections');
  return response.data;
};

export const createDoctorConnection = async (payload) => {
  const response = await apiClient.post('/api/user/doctor-connections', payload);
  return response.data;
};

export const revokeDoctorConnection = async (connectionId) => {
  const response = await apiClient.post(`/api/user/doctor-connections/${connectionId}/revoke`);
  return response.data;
};

export const reactivateDoctorConnection = async (connectionId) => {
  const response = await apiClient.post(`/api/user/doctor-connections/${connectionId}/reactivate`);
  return response.data;
};
