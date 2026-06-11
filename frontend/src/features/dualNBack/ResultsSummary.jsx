import { Link } from 'react-router-dom';
import Card from '../../components/ui/Card';
import ResultsCharts from '../results/ResultsCharts';

export default function ResultsSummary({ result }) {
  return (
    <>
      <Card title="Adaptive Dual N-Back Complete" accent="cool">
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
            <strong>{Math.round((result.falseAlarmRate ?? 0) * 1000) / 10}%</strong>
            <span>False alarm rate</span>
          </div>
          <div>
            <strong>{result.maxNReached ?? '-'}</strong>
            <span>Max N reached</span>
          </div>
        </div>
      </Card>

      <ResultsCharts result={result} />

      <div className="actions-row">
        <Link to="/tasks" className="button-link button-link-secondary">Choose another task</Link>
        {result.sessionId ? (
          <Link to={`/sessions/${result.sessionId}`} className="button-link button-link-primary">View full trial log</Link>
        ) : (
          <Link to="/results/latest" className="button-link button-link-primary">View full results</Link>
        )}
      </div>
    </>
  );
}
