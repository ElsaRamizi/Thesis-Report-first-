import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageHero from '../components/layout/PageHero';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import GroupTrendsCharts from '../features/clinician/GroupTrendsCharts';
import { fetchGroupTrends } from '../services/clinicianService';

const trendClass = (trend) => {
  if (trend === 'IMPROVING') {
    return 'severity-badge severity-normal';
  }
  if (trend === 'DECLINING') {
    return 'severity-badge severity-alert';
  }
  return 'severity-badge severity-watch';
};

const severityClass = (severity) => {
  if (severity === 'ALERT') {
    return 'severity-badge severity-alert';
  }
  if (severity === 'WATCH') {
    return 'severity-badge severity-watch';
  }
  return 'severity-badge severity-normal';
};

export default function ClinicianGroupTrendsPage() {
  const [taskFilter, setTaskFilter] = useState('all');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);

    fetchGroupTrends(taskFilter)
      .then((response) => {
        if (active) {
          setData(response);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Could not load trends.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [taskFilter]);

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Group"
        title="Trends"
        description="Everyone you can see — accuracy and RT over time."
      />

      <div className="actions-row">
        <Link to="/clinician/dashboard" className="inline-link">Back to dashboard</Link>
      </div>

      <Card title="Filter by task">
        <div className="form-grid">
          <label className="form-field">
            <span>Task type</span>
            <select value={taskFilter} onChange={(event) => setTaskFilter(event.target.value)}>
              <option value="all">All tasks</option>
              <option value="memory-span">Memory Span</option>
              <option value="stroop">Stroop</option>
              <option value="dual-n-back">Dual N-Back</option>
            </select>
          </label>
        </div>
      </Card>

      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}
      {loading ? <LoadingState label="Loading group trends..." /> : null}

      {!loading && data ? (
        <>
          <GroupTrendsCharts timeline={data.groupTimeline} />

          <Card title="Participant caseload">
            {data.participants.length === 0 ? (
              <StatusMessage tone="warning" message="No participants with saved sessions match this filter." />
            ) : (
              <div className="trial-log">
                <div className="trial-row trial-log-header group-trends-header">
                  <span>Participant</span>
                  <span>Last task</span>
                  <span>Last session</span>
                  <span>Accuracy</span>
                  <span>RT</span>
                  <span>Trend</span>
                  <span>Actions</span>
                </div>
                {data.participants.map((row) => (
                  <div key={row.participantId} className="trial-row group-trends-row">
                    <span>{row.displayName}</span>
                    <span>{row.lastTaskTitle}</span>
                    <span>{row.lastSessionTime ? new Date(row.lastSessionTime).toLocaleString() : '—'}</span>
                    <span>{row.latestAccuracy ?? '—'}%</span>
                    <span>{row.latestReactionTime ?? '—'} ms</span>
                    <span>
                      <span className={trendClass(row.trend)}>{row.trend}</span>
                      <span className={severityClass(row.severity)}>{row.severity}</span>
                    </span>
                    <span className="group-actions">
                      <Link to={`/clinician/participants/${row.participantId}`} className="inline-link">Profile</Link>
                      <Link to={`/clinician/participants/${row.participantId}/report`} className="inline-link">Report</Link>
                      <Link to={`/clinician/participants/${row.participantId}/compare`} className="inline-link">Compare</Link>
                    </span>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </>
      ) : null}
    </div>
  );
}
