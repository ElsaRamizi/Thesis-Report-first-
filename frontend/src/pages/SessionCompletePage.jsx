import { Link, Navigate, useLocation } from 'react-router-dom';

import Card from '../components/ui/Card';

import ResultsCharts from '../features/results/ResultsCharts';

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

        <h2 className="headline">Session finished.</h2>

        <p>Summary and charts from this run:</p>

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

          {result.maxSpanReached != null ? (

            <div>

              <strong>{result.maxSpanReached}</strong>

              <span>Max span reached</span>

            </div>

          ) : null}

          {result.stroopInterferenceMs != null ? (

            <div>

              <strong>{result.stroopInterferenceMs} ms</strong>

              <span>Stroop interference</span>

            </div>

          ) : null}

        </div>

      </Card>



      <ResultsCharts result={result} />



      <div className="actions-row">

        <Link to="/tasks" className="button-link button-link-secondary">Choose another task</Link>

        {result.sessionId ? (

          <Link to={`/sessions/${result.sessionId}`} className="button-link button-link-primary">View full trial log</Link>

        ) : (

          <Link to="/results/latest" className="button-link button-link-primary">View results</Link>

        )}

      </div>

    </div>

  );

}


