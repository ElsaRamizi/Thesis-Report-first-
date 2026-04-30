import { Link, Navigate, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { fetchTaskById, fetchTaskTrials } from '../services/taskService';

export default function SessionStartPage() {
  const { taskId } = useParams();
  const [task, setTask] = useState(null);
  const [trials, setTrials] = useState([]);
  const [loading, setLoading] = useState(true);
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
          const message = requestError.response?.data?.message ?? 'Task setup could not be loaded.';
          setError(message);
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [taskId]);

  if (!loading && !task && !error) {
    return <Navigate to="/not-found" replace />;
  }

  const isDualNBack = task?.id === 'dual-n-back';

  return (
    <div className="stack-lg">
      {loading ? <LoadingState label="Loading session setup..." /> : null}
      {error ? <StatusMessage tone="error" title="Session unavailable" message={error} /> : null}
      {!task ? null : (
        <>
      <Card title={task.title} accent="cool">
        <p>{task.description}</p>
        <div className="session-summary">
          <div>
            <strong>{isDualNBack ? '60' : trials.length}</strong>
            <span>{isDualNBack ? 'Adaptive trials' : 'Trials'}</span>
          </div>
          <div>
            <strong>{task.durationMinutes} min</strong>
            <span>Estimated time</span>
          </div>
          <div>
            <strong>{task.difficulty}</strong>
            <span>Difficulty</span>
          </div>
        </div>
      </Card>

      <Card title="Before you begin">
        <ul className="metric-list">
          {isDualNBack ? (
            <>
              <li>Each round shows one square and one letter.</li>
              <li>If the square is in the same place as N rounds ago, press A.</li>
              <li>If the letter is the same as N rounds ago, press L.</li>
              <li>You can choose Fixed N, where N stays the same, or Adaptive N, where the game changes N after each 20-round block.</li>
            </>
          ) : (
            <>
              <li>Work in a quiet space and keep your focus on the prompt.</li>
              <li>Respond as quickly and accurately as possible.</li>
              <li>Your trial-by-trial responses will be summarized immediately after the session.</li>
            </>
          )}
        </ul>
        <div className="actions-row">
          <Link to="/tasks" className="inline-link">Back to task list</Link>
          <Link to={`/tasks/${task.id}/play`} className="button-link button-link-primary">Start session</Link>
        </div>
      </Card>
        </>
      )}
    </div>
  );
}
