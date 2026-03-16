import apiClient from './apiClient';
import { RESULT_STORAGE_KEY } from '../features/results/constants';

export const getSessionOverview = async () => {
  const response = await apiClient.get('/api/test');
  return response.data;
};

const normalizeResult = (payload) => {
  return {
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
    trials: (payload.trials ?? []).map((trial, index) => ({
      id: trial.id,
      index: index + 1,
      stimulus: trial.stimulus,
      response: trial.response,
      correct: trial.correct,
      reactionTime: trial.reactionTime,
    })),
  };
};

const cacheLatestResult = (result) => {
  sessionStorage.setItem(RESULT_STORAGE_KEY, JSON.stringify(result));
  return result;
};

export const completeSession = async ({ task, trials, startedAt, endedAt }) => {
  const payload = {
    taskType: task.id,
    difficultyLevel: task.difficulty,
    startTime: new Date(startedAt).toISOString(),
    endTime: new Date(endedAt).toISOString(),
    trials: trials.map((trial) => ({
      stimulus: trial.stimulus,
      response: trial.response,
      reactionTime: trial.reactionTime,
      correct: trial.correct,
      timestamp: trial.timestamp ?? new Date().toISOString(),
    })),
  };

  const response = await apiClient.post('/api/sessions/complete', payload);
  return cacheLatestResult(normalizeResult(response.data));
};

export const getLatestSessionResult = () => {
  const raw = sessionStorage.getItem(RESULT_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    sessionStorage.removeItem(RESULT_STORAGE_KEY);
    return null;
  }
};

export const fetchLatestSessionResult = async () => {
  const response = await apiClient.get('/api/sessions/latest');
  return cacheLatestResult(normalizeResult(response.data));
};

export const fetchSessionHistory = async () => {
  const response = await apiClient.get('/api/sessions');
  return response.data.map((item) => ({
    sessionId: item.sessionId,
    taskId: item.taskType,
    taskTitle: item.taskTitle ?? item.taskType,
    difficultyLevel: item.difficultyLevel,
    startTime: item.startTime,
    endTime: item.endTime,
    avgReactionTime: item.avgReactionTime,
    accuracy: item.accuracy,
    errorRate: item.errorRate,
  }));
};
