import { useEffect, useRef, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import GameContainer from '../features/dualNBack/GameContainer';
import { completeSession } from '../services/sessionService';
import { fetchTaskById, fetchTaskTrials } from '../services/taskService';

const getCurrentTime = () => Date.now();

export default function TaskPlayPage() {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const [task, setTask] = useState(null);
  const [trials, setTrials] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [sessionStartedAt] = useState(() => Date.now());
  const trialStartedAtRef = useRef(null);
  const [responses, setResponses] = useState([]);
  const [finishing, setFinishing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    Promise.all([fetchTaskById(taskId), fetchTaskTrials(taskId)])
      .then(([taskData, trialData]) => {
        if (active) {
          setTask(taskData);
          setTrials(trialData);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'This task could not be loaded.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [taskId]);

  useEffect(() => {
    trialStartedAtRef.current = getCurrentTime();
  }, [currentIndex]);

  if (loading) {
    return <LoadingState label="Loading task session..." />;
  }

  if (error && !task) {
    return <StatusMessage tone="error" title="Task unavailable" message={error} />;
  }

  if (task?.id === 'dual-n-back') {
    return <GameContainer task={task} />;
  }

  if (!task || !trials.length) {
    return <Navigate to="/tasks" replace />;
  }

  const currentTrial = trials[currentIndex];
  const progress = Math.round((currentIndex / trials.length) * 100);

  const handleResponse = async (response) => {
    if (finishing) {
      return;
    }

    const responseTime = getCurrentTime();
    const trialStartedAt = trialStartedAtRef.current ?? sessionStartedAt;
    const reactionTime = Math.max(250, responseTime - trialStartedAt);
    const correct = response === currentTrial.correctResponse;
    const nextResponses = [
      ...responses,
      {
        stimulus: currentTrial.stimulus,
        response,
        correct,
        reactionTime,
        timestamp: new Date().toISOString(),
      },
    ];

    if (currentIndex === trials.length - 1) {
      setFinishing(true);
      setError('');

      try {
        const result = await completeSession({
          task,
          trials: nextResponses,
          startedAt: sessionStartedAt,
          endedAt: getCurrentTime(),
        });
        navigate('/session/complete', { state: { result } });
      } catch (requestError) {
        setError(requestError.response?.data?.message ?? 'The session could not be saved.');
        setFinishing(false);
      }
      return;
    }

    setResponses(nextResponses);
    setCurrentIndex((value) => value + 1);
  };

  return (
    <div className="stack-lg">
      {error ? <StatusMessage tone="error" title="Task unavailable" message={error} /> : null}
      <Card title={task.title} accent="warm" footer={`Trial ${currentIndex + 1} of ${trials.length}`}>
        <div className="progress-shell">
          <div className="progress-bar" style={{ width: `${progress}%` }} />
        </div>
        <p className="eyebrow">{currentTrial.prompt}</p>
        <div className="stimulus-card" style={currentTrial.displayColor ? { color: currentTrial.displayColor } : undefined}>
          {currentTrial.stimulus}
        </div>
        <div className="options-grid">
          {currentTrial.options.map((option) => (
            <Button key={option} onClick={() => handleResponse(option)}>{option}</Button>
          ))}
        </div>
      </Card>

      {finishing ? <LoadingState label="Compiling your session summary..." /> : null}
      {error ? <StatusMessage tone="error" title="Session save failed" message={error} /> : null}
    </div>
  );
}
