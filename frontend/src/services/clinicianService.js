import apiClient from './apiClient';

export const getClinicianDashboard = async () => {
  const response = await apiClient.get('/api/clinician/dashboard');
  return response.data;
};

const normalizeResult = (payload) => ({
  sessionId: payload.sessionId,
  taskId: payload.taskType,
  taskTitle: payload.taskTitle ?? payload.taskType,
  difficultyLevel: payload.difficultyLevel,
  startTime: payload.startTime,
  endTime: payload.endTime,
  completedAt: payload.endTime,
  avgReactionTime: payload.avgReactionTime,
  accuracy: payload.accuracy,
  errorRate: payload.errorRate,
  falseAlarmRate: payload.falseAlarmRate,
  maxNReached: payload.maxNReached,
  maxSpanReached: payload.maxSpanReached,
  stroopInterferenceMs: payload.stroopInterferenceMs,
  stroopCongruentAccuracy: payload.stroopCongruentAccuracy,
  stroopIncongruentAccuracy: payload.stroopIncongruentAccuracy,
  medianReactionTime: payload.medianReactionTime,
  missRate: payload.missRate,
  responseVariability: payload.responseVariability,
  dPrime: payload.dPrime,
  trials: (payload.trials ?? []).map((trial, index) => ({
    id: trial.id,
    index: trial.trialIndex ?? index + 1,
    stimulus: trial.stimulus,
    response: trial.response,
    correct: trial.correct,
    reactionTime: trial.reactionTime,
    nLevel: trial.nLevel,
    position: trial.position,
    letter: trial.letter,
    expectedPositionMatch: trial.expectedPositionMatch,
    expectedLetterMatch: trial.expectedLetterMatch,
    userPressedPosition: trial.userPressedPosition,
    userPressedLetter: trial.userPressedLetter,
    positionOutcome: trial.positionOutcome,
    letterOutcome: trial.letterOutcome,
    reactionTimePosition: trial.reactionTimePosition,
    reactionTimeLetter: trial.reactionTimeLetter,
  })),
});

export const fetchClinicianParticipants = async () => {
  const response = await apiClient.get('/api/clinician/participants');
  return response.data;
};

export const fetchParticipantSessions = async (participantId) => {
  const response = await apiClient.get(`/api/clinician/participants/${participantId}/sessions`);
  return response.data;
};

export const fetchClinicianSessionResult = async (sessionId) => {
  const response = await apiClient.get(`/api/clinician/sessions/${sessionId}/results`);
  return normalizeResult(response.data);
};

export const fetchParticipantProfile = async (participantId) => {
  const response = await apiClient.get(`/api/clinician/participants/${participantId}/profile`);
  return response.data;
};

export const updateParticipantProfile = async (participantId, payload) => {
  const response = await apiClient.put(`/api/clinician/participants/${participantId}/profile`, payload);
  return response.data;
};

export const fetchParticipantAnnotations = async (participantId) => {
  const response = await apiClient.get(`/api/clinician/participants/${participantId}/annotations`);
  return response.data;
};

export const addParticipantAnnotation = async (participantId, payload) => {
  const response = await apiClient.post(`/api/clinician/participants/${participantId}/annotations`, payload);
  return response.data;
};

export const compareParticipantSessions = async (sessionA, sessionB) => {
  const response = await apiClient.get('/api/clinician/sessions/compare', {
    params: { sessionA, sessionB },
  });

  return {
    sessionA: normalizeResult(response.data.sessionA),
    sessionB: normalizeResult(response.data.sessionB),
    comparison: response.data.comparison,
  };
};

export const compareMultipleSessions = async (sessionIds) => {
  const response = await apiClient.post('/api/clinician/sessions/compare-multi', {
    sessionIds,
  });

  return {
    sessions: (response.data.sessions ?? []).map(normalizeResult),
    timeline: response.data.timeline ?? [],
    baselineComparison: response.data.baselineComparison,
    summary: response.data.summary,
  };
};

export const fetchAutomatedReport = async (participantId) => {
  const response = await apiClient.get(`/api/clinician/participants/${participantId}/automated-report`);
  return response.data;
};

export const fetchClinicianDirectory = async () => {
  const response = await apiClient.get('/api/clinician/directory');
  return response.data;
};

export const fetchGroupTrends = async (taskType = 'all') => {
  const response = await apiClient.get('/api/clinician/group-trends', {
    params: { taskType },
  });
  return response.data;
};

export const exportCognitiveMetrics = async (filters = {}) => {
  const response = await apiClient.post('/api/clinician/export/cognitive', filters, {
    responseType: 'blob',
  });

  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', 'cognitive-metrics-anonymized.csv');
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};
