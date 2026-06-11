import apiClient from './apiClient';

export const browseResearchStudies = async () => {
  const response = await apiClient.get('/api/research/studies');
  return response.data;
};

export const fetchResearchStudy = async (studyId) => {
  const response = await apiClient.get(`/api/research/studies/${studyId}`);
  return response.data;
};

export const fetchMyParticipations = async () => {
  const response = await apiClient.get('/api/research/participations');
  return response.data;
};

export const joinResearchStudy = async (studyId, payload) => {
  const response = await apiClient.post(`/api/research/studies/${studyId}/join`, payload);
  return response.data;
};

export const withdrawFromStudy = async (participationId) => {
  const response = await apiClient.post(`/api/research/participations/${participationId}/withdraw`);
  return response.data;
};

export const updateParticipationAnswers = async (participationId, answers) => {
  const response = await apiClient.put(`/api/research/participations/${participationId}/answers`, answers);
  return response.data;
};

export const fetchClinicianStudies = async () => {
  const response = await apiClient.get('/api/clinician/research/studies');
  return response.data;
};

export const fetchClinicianStudy = async (studyId) => {
  const response = await apiClient.get(`/api/clinician/research/studies/${studyId}`);
  return response.data;
};

export const createResearchStudy = async (payload) => {
  const response = await apiClient.post('/api/clinician/research/studies', payload);
  return response.data;
};

export const updateResearchStudy = async (studyId, payload) => {
  const response = await apiClient.put(`/api/clinician/research/studies/${studyId}`, payload);
  return response.data;
};

export const publishResearchStudy = async (studyId) => {
  const response = await apiClient.post(`/api/clinician/research/studies/${studyId}/publish`);
  return response.data;
};

export const deleteResearchStudy = async (studyId) => {
  await apiClient.delete(`/api/clinician/research/studies/${studyId}`);
};

export const addResearchQuestion = async (studyId, payload) => {
  const response = await apiClient.post(`/api/clinician/research/studies/${studyId}/questions`, payload);
  return response.data;
};

export const updateResearchQuestion = async (questionId, payload) => {
  const response = await apiClient.put(`/api/clinician/research/questions/${questionId}`, payload);
  return response.data;
};

export const deleteResearchQuestion = async (questionId) => {
  const response = await apiClient.delete(`/api/clinician/research/questions/${questionId}`);
  return response.data;
};

export const fetchResearchCohorts = async (studyId) => {
  const response = await apiClient.get(`/api/clinician/research/studies/${studyId}/cohorts`);
  return response.data;
};

export const createResearchCohort = async (studyId, payload) => {
  const response = await apiClient.post(`/api/clinician/research/studies/${studyId}/cohorts`, payload);
  return response.data;
};

export const previewResearchFilter = async (studyId, filters) => {
  const response = await apiClient.post(`/api/clinician/research/studies/${studyId}/cohorts/preview`, { filters });
  return response.data;
};

export const fetchResearchAnalytics = async (studyId, filters = []) => {
  const response = await apiClient.post(`/api/clinician/research/studies/${studyId}/analytics`, { filters });
  return response.data;
};

export const compareResearchCohorts = async (studyId, payload) => {
  const response = await apiClient.post(`/api/clinician/research/studies/${studyId}/analytics/compare`, payload);
  return response.data;
};

export const exportResearchData = async (studyId, filters = []) => {
  const response = await apiClient.post(
    `/api/clinician/research/studies/${studyId}/export`,
    { filters },
    { responseType: 'blob' },
  );
  return response.data;
};
