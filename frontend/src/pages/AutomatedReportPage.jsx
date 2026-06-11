import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import PageHero from '../components/layout/PageHero';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { fetchAutomatedReport } from '../services/clinicianService';

const severityClass = (value) => {
  if (value === 'ALERT') {
    return 'severity-badge severity-alert';
  }
  if (value === 'WATCH') {
    return 'severity-badge severity-watch';
  }
  return 'severity-badge severity-normal';
};

const trendLabel = (value) => {
  if (value === 'IMPROVING') {
    return 'Improving';
  }
  if (value === 'DECLINING') {
    return 'Declining';
  }
  if (value === 'INSUFFICIENT_DATA') {
    return 'Not enough data';
  }
  return 'Stable';
};

export default function AutomatedReportPage() {
  const { participantId } = useParams();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    fetchAutomatedReport(participantId)
      .then((data) => {
        if (active) {
          setReport(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Could not load report.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [participantId]);

  if (loading) {
    return <LoadingState label="Loading report..." />;
  }

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Report"
        title={report?.displayName ?? 'Participant report'}
        description="Simple summary if accuracy or RT changed a lot between sessions."
      />

      <Link to={`/clinician/participants/${participantId}`} className="inline-link">Back to participant</Link>

      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}

      {report ? (
        <>
          <div className="dashboard-grid results-metrics">
            <Card title="Sessions" accent="cool">
              <p className="metric-value">{report.sessionsAnalyzed}</p>
            </Card>
            <Card title="Trend" accent="warm">
              <p className="metric-value">{trendLabel(report.overallTrend)}</p>
            </Card>
            <Card title="Severity">
              <p className={`metric-value ${severityClass(report.severity)}`}>{report.severity ?? 'NORMAL'}</p>
            </Card>
          </div>

          <Card title="Findings">
            <ul className="metric-list">
              {report.findings.map((finding) => (
                <li key={finding}>{finding}</li>
              ))}
            </ul>
          </Card>

          <Card title="Recommendations">
            <ul className="metric-list">
              {report.recommendations.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </Card>
        </>
      ) : null}
    </div>
  );
}
