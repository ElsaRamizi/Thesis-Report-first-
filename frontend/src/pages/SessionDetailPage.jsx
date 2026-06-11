import { Link, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import SessionResultsDetail from '../features/results/SessionResultsDetail';
import { fetchSessionResult } from '../services/sessionService';

export default function SessionDetailPage() {
  const { sessionId } = useParams();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    fetchSessionResult(sessionId)
      .then((data) => {
        if (active) {
          setResult(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'This session could not be loaded.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [sessionId]);

  if (loading) {
    return <LoadingState label="Loading session details..." />;
  }

  return (
    <div className="stack-lg">
      <section className="hero-panel">
        <p className="eyebrow">Session details</p>
        <h2>{result?.taskTitle ?? 'Saved session'}</h2>
        <p>
          {result?.startTime ? new Date(result.startTime).toLocaleString() : 'Completed session'}
          {result?.difficultyLevel ? ` · ${result.difficultyLevel}` : ''}
        </p>
      </section>

      <div className="actions-row">
        <Link to="/sessions" className="inline-link">Back to session history</Link>
        <Link to="/results/latest" className="inline-link">Open latest result</Link>
      </div>

      {error ? <StatusMessage tone="error" title="Session unavailable" message={error} /> : null}
      {result ? <SessionResultsDetail result={result} /> : null}
    </div>
  );
}
