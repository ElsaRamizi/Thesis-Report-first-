import { Link, Navigate, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import SessionResultsDetail from '../features/results/SessionResultsDetail';
import { fetchLatestSessionResult, getLatestSessionResult } from '../services/sessionService';

export default function ResultsPage() {
  const location = useLocation();
  const [result, setResult] = useState(() => location.state?.result ?? getLatestSessionResult());
  const [loading, setLoading] = useState(() => !location.state?.result && !getLatestSessionResult());
  const [error, setError] = useState('');

  useEffect(() => {
    if (result) {
      return undefined;
    }

    let active = true;
    fetchLatestSessionResult()
      .then((data) => {
        if (active) {
          setResult(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'No saved result was found for this account.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [result]);

  if (loading) {
    return <LoadingState label="Loading your latest saved result..." />;
  }

  if (!result && !error) {
    return <Navigate to="/tasks" replace />;
  }

  return (
    <div className="stack-lg">
      <section className="hero-panel">
        <p className="eyebrow">Results Summary</p>
        <h2>{result?.taskTitle ?? 'Latest saved session'}</h2>
        <p>Review the full trial-by-trial profile to spot attention drift, response slowing, and error patterns.</p>
      </section>

      {error ? <StatusMessage tone="warning" title="No saved result" message={error} /> : null}
      {!result ? <Link to="/tasks" className="inline-link">Start a task to create your first saved result</Link> : null}

      {result ? (
        <>
          <SessionResultsDetail result={result} />
          <div className="actions-row">
            {result.sessionId ? (
              <Link to={`/sessions/${result.sessionId}`} className="inline-link">
                Open this session permalink
              </Link>
            ) : null}
            <Link to="/sessions" className="inline-link">Browse all sessions</Link>
            <Link to="/tasks" className="inline-link">Back to task selection</Link>
          </div>
        </>
      ) : null}
    </div>
  );
}
