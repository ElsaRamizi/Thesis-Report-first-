import { Link, Navigate, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import ResultsCharts from '../features/results/ResultsCharts';
import { fetchLatestSessionResult, getLatestSessionResult } from '../services/sessionService';

export default function ResultsPage() {
  const location = useLocation();
  const [result, setResult] = useState(() => location.state?.result ?? getLatestSessionResult());
  const [loading, setLoading] = useState(() => !location.state?.result && !getLatestSessionResult());
  const [error, setError] = useState('');

  useEffect(() => {
    if (result) {
      return;
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
      <div className="dashboard-grid results-metrics">
        <Card title="Average Reaction Time" accent="cool"><p className="metric-value">{result.avgReactionTime} ms</p></Card>
        <Card title="Accuracy" accent="warm"><p className="metric-value">{result.accuracy}%</p></Card>
        <Card title="Error Rate"><p className="metric-value">{result.errorRate}%</p></Card>
        {result.falseAlarmRate != null ? (
          <Card title="False Alarm Rate"><p className="metric-value">{Math.round(result.falseAlarmRate * 1000) / 10}%</p></Card>
        ) : null}
        {result.maxNReached != null ? (
          <Card title="Max N Reached"><p className="metric-value">{result.maxNReached}</p></Card>
        ) : null}
        {result.dPrime != null ? (
          <Card title="d-prime"><p className="metric-value">{result.dPrime}</p></Card>
        ) : null}
      </div>

      <ResultsCharts result={result} />

      <Card title="Trial Log">
        <div className="trial-log">
          {result.trials.map((trial) => (
            <div key={trial.index} className="trial-row">
              <span>Trial {trial.index}</span>
              <span>{trial.letter ? `N${trial.nLevel} | ${trial.letter} @ ${trial.position + 1}` : trial.stimulus}</span>
              <span>{trial.positionOutcome ? `${trial.positionOutcome} / ${trial.letterOutcome}` : trial.response}</span>
              <span>{trial.reactionTime} ms</span>
              <span className={trial.correct ? 'result-good' : 'result-bad'}>{trial.correct ? 'Correct' : 'Error'}</span>
            </div>
          ))}
        </div>
      </Card>

      <StatusMessage tone="neutral" message="This summary now reflects the latest saved TestSession, TrialData, and AggregatedMetrics records for the signed-in user." />
      <Link to="/tasks" className="inline-link">Back to task selection</Link>
        </>
      ) : null}
    </div>
  );
}
