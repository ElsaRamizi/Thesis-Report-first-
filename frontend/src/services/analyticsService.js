import apiClient from './apiClient';

export const fetchSharedPatients = async () => {
  const response = await apiClient.get('/api/clinician/analytics/shared-patients');
  return response.data;
};

export const fetchParticipantAnalytics = async (participantId) => {
  const response = await apiClient.get(`/api/clinician/analytics/participants/${participantId}`);
  return response.data;
};

export const fetchCohortAnalytics = async (payload) => {
  const response = await apiClient.post('/api/clinician/analytics/cohorts', payload);
  return response.data;
};
