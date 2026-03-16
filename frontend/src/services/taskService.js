import apiClient from './apiClient';

let taskCache = null;
const taskTrialCache = new Map();

export const fetchTasks = async () => {
  if (taskCache) {
    return taskCache;
  }

  const response = await apiClient.get('/api/tasks');
  taskCache = response.data;
  return taskCache;
};

export const fetchTaskById = async (taskId) => {
  const tasks = await fetchTasks();
  const cachedTask = tasks.find((task) => task.id === taskId);

  if (cachedTask) {
    return cachedTask;
  }

  const response = await apiClient.get(`/api/tasks/${taskId}`);
  return response.data;
};

export const fetchTaskTrials = async (taskId) => {
  if (taskTrialCache.has(taskId)) {
    return taskTrialCache.get(taskId);
  }

  const response = await apiClient.get(`/api/tasks/${taskId}/trials`);
  taskTrialCache.set(taskId, response.data);
  return response.data;
};
