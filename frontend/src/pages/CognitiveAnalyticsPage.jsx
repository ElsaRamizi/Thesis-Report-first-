import { useEffect, useState } from 'react';
import PageHero from '../components/layout/PageHero';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import CognitiveMetricsCards from '../features/analytics/CognitiveMetricsCards';
import {
  CognitiveRadarChart,
  CognitiveTimelineCharts,
  CohortHistogramChart,
  CohortTimelineChart,
} from '../features/analytics/CognitiveCharts';
import {
  fetchCohortAnalytics,
  fetchParticipantAnalytics,
  fetchSharedPatients,
} from '../services/analyticsService';

export default function CognitiveAnalyticsPage() {
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [cohortAnalytics, setCohortAnalytics] = useState(null);
  const [loadingPatients, setLoadingPatients] = useState(true);
  const [loadingAnalytics, setLoadingAnalytics] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSharedPatients()
      .then((data) => {
        setPatients(data);
        setSelectedPatient(data[0] ?? null);
        setLoadingPatients(false);
      })
      .catch((requestError) => {
        setError(requestError.response?.data?.message ?? 'Shared patients could not be loaded.');
        setLoadingPatients(false);
      });
  }, []);

  useEffect(() => {
    if (!selectedPatient) {
      return undefined;
    }

    let active = true;
    setLoadingAnalytics(true);
    setError('');

    Promise.all([
      fetchParticipantAnalytics(selectedPatient.participantId),
      fetchCohortAnalytics({ participantIds: patients.map((patient) => patient.participantId) }),
    ])
      .then(([participantData, cohortData]) => {
        if (active) {
          setAnalytics(participantData);
          setCohortAnalytics(cohortData);
          setLoadingAnalytics(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Analytics could not be loaded.');
          setLoadingAnalytics(false);
        }
      });

    return () => {
      active = false;
    };
  }, [selectedPatient, patients]);

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Analytics"
        title="Patient stats"
        description="Numbers and charts for people who shared data with you."
      />

      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}

      <div className="clinician-grid">
        <Card title="Shared Patients" accent="cool">
          {loadingPatients ? <LoadingState label="Loading shared patients..." /> : null}
          {!loadingPatients && patients.length === 0 ? (
            <StatusMessage tone="warning" message="Nobody shared gameplay data yet. They need to connect a doctor or join a study with sharing turned on." />
          ) : null}
          <div className="participant-list">
            {patients.map((patient) => (
              <button
                key={`${patient.participantId}-${patient.source}-${patient.studyId ?? 'direct'}`}
                type="button"
                className={`participant-button ${selectedPatient?.participantId === patient.participantId ? 'participant-button-active' : ''}`}
                onClick={() => setSelectedPatient(patient)}
              >
                <strong>{patient.displayName}</strong>
                <span>{patient.source.replace(/_/g, ' ')}</span>
                {patient.studyTitle ? <span>{patient.studyTitle}</span> : null}
                {patient.anonymous ? <span className="badge badge-anon">Anonymous</span> : null}
              </button>
            ))}
          </div>
        </Card>

        <Card title="Access Context">
          {selectedPatient ? (
            <div className="research-card-meta">
              <div><span>Display Name</span><strong>{selectedPatient.displayName}</strong></div>
              <div><span>Source</span><strong>{selectedPatient.source.replace(/_/g, ' ')}</strong></div>
              <div><span>Shared Since</span><strong>{selectedPatient.sharedSince ? new Date(selectedPatient.sharedSince).toLocaleString() : '-'}</strong></div>
            </div>
          ) : (
            <StatusMessage tone="neutral" message="Select a shared patient to open the cognitive analytics dashboard." />
          )}
        </Card>
      </div>

      {loadingAnalytics ? <LoadingState label="Loading cognitive analytics..." /> : null}

      {analytics && !loadingAnalytics ? (
        <>
          {analytics.cohortComparison?.summary ? (
            <StatusMessage tone="neutral" title="Vs group average" message={analytics.cohortComparison.summary} />
          ) : null}

          <CognitiveMetricsCards title="Latest Session Metrics" metrics={analytics.latestMetrics} />
          <CognitiveMetricsCards title="Overall Averages" metrics={analytics.overallAverage} />
          <CognitiveMetricsCards title="Rolling Averages" metrics={analytics.rollingAverage} comparison={analytics.cohortComparison} />

          <div className="results-grid">
            <CognitiveRadarChart profile={analytics.cognitiveProfile} />
          </div>

          <CognitiveTimelineCharts timeline={analytics.timeline} rollingTimeline={analytics.rollingTimeline} />

          <Card title="Session Analysis">
            <div className="research-question-list">
              {(analytics.sessions ?? []).map((session) => (
                <div key={session.sessionId} className="research-analytics-block">
                  <strong>{session.taskTitle} · {new Date(session.startTime).toLocaleString()}</strong>
                  <div className="chip-row">
                    <span className="chip">Trials: {session.analysis?.totalTrials ?? 0}</span>
                    <span className="chip">Incorrect: {session.analysis?.incorrectResponses ?? 0}</span>
                    {session.analysis?.fatigueIndicatorPercent != null ? (
                      <span className="chip">Fatigue: {session.analysis.fatigueIndicatorPercent}%</span>
                    ) : null}
                  </div>
                  {(session.analysis?.anomalies ?? []).length > 0 ? (
                    <StatusMessage tone="warning" message={session.analysis.anomalies.join(' · ')} />
                  ) : null}
                </div>
              ))}
            </div>
          </Card>
        </>
      ) : null}

      {cohortAnalytics && !loadingAnalytics ? (
        <>
          <CognitiveMetricsCards title="Cohort Average Metrics" metrics={cohortAnalytics.averageMetrics} />
          <div className="results-grid">
            <CohortTimelineChart timeline={cohortAnalytics.cohortTimeline} />
            <CohortHistogramChart histogram={cohortAnalytics.reactionTimeHistogram} />
          </div>
          {cohortAnalytics.statistics ? (
            <Card title="Cohort Statistics">
              <div className="dashboard-grid results-metrics">
                <div className="analytics-metric-card"><span>Std Dev (RT)</span><strong className="metric-value">{cohortAnalytics.statistics.reactionTimeStdDev ?? '-'}</strong></div>
                <div className="analytics-metric-card"><span>Accuracy Variance</span><strong className="metric-value">{cohortAnalytics.statistics.accuracyVariance ?? '-'}</strong></div>
                <div className="analytics-metric-card"><span>Median RT</span><strong className="metric-value">{cohortAnalytics.statistics.medianReactionTime ?? '-'} ms</strong></div>
              </div>
            </Card>
          ) : null}
        </>
      ) : null}
    </div>
  );
}
