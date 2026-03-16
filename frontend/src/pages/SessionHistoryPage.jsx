import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { fetchSessionHistory } from '../services/sessionService';

export default function SessionHistoryPage() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    fetchSessionHistory()
      .then((data) => {
        if (active) {
          setHistory(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Session history could not be loaded.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="stack-lg">
      <section className="hero-panel">
        <p className="eyebrow">Session History</p>
        <h2>Saved cognitive sessions</h2>
        <p>Review previously completed assessments and revisit the latest result summary when needed.</p>
      </section>

      {loading ? <LoadingState label="Loading session history..." /> : null}
      {error ? <StatusMessage tone="error" title="History unavailable" message={error} /> : null}
      {!loading && !error && history.length === 0 ? (
        <StatusMessage tone="warning" title="No sessions yet" message="Complete a task to create your first saved session." />
      ) : null}

      {history.length > 0 ? (
        <Card title="Saved Sessions">
          <div className="trial-log">
            {history.map((item) => (
              <div key={item.sessionId} className="trial-row">
                <span>{item.taskTitle}</span>
                <span>{item.difficultyLevel}</span>
                <span>{new Date(item.startTime).toLocaleDateString()}</span>
                <span>{item.avgReactionTime} ms</span>
                <span className="result-good">{item.accuracy}%</span>
              </div>
            ))}
          </div>
        </Card>
      ) : null}

      <Link to="/results/latest" className="inline-link">Open latest result</Link>
    </div>
  );
}
