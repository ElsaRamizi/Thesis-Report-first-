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
