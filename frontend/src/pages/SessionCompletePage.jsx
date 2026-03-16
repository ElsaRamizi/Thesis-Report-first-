import { Link, Navigate, useLocation } from 'react-router-dom';
import Card from '../components/ui/Card';
import { getLatestSessionResult } from '../services/sessionService';

export default function SessionCompletePage() {
  const location = useLocation();
  const result = location.state?.result ?? getLatestSessionResult();

  if (!result) {
    return <Navigate to="/tasks" replace />;
  }

  return (
    <div className="stack-lg">
      <Card title="Session Complete" accent="cool">
        <p className="eyebrow">{result.taskTitle}</p>
        <h2 className="headline">Your assessment session is finished.</h2>
        <p>MindMetrics has calculated the initial performance summary and is ready to visualize the detailed trends.</p>
        <div className="session-summary">
          <div>
            <strong>{result.avgReactionTime} ms</strong>
            <span>Average reaction time</span>
          </div>
          <div>
            <strong>{result.accuracy}%</strong>
            <span>Accuracy</span>
          </div>
          <div>
            <strong>{result.errorRate}%</strong>
            <span>Error rate</span>
          </div>
        </div>
        <div className="actions-row">
          <Link to="/tasks" className="button-link button-link-secondary">Choose another task</Link>
          <Link to="/results/latest" className="button-link button-link-primary">View results</Link>
        </div>
      </Card>
    </div>
  );
}
