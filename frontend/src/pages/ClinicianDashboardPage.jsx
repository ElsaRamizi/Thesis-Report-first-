import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import PageHero from '../components/layout/PageHero';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import ResultsCharts from '../features/results/ResultsCharts';
import ParticipantTrendChart from '../features/clinician/ParticipantTrendChart';
import TrialLog from '../features/results/TrialLog';
import {
  exportCognitiveMetrics,
  fetchClinicianParticipants,
  fetchClinicianSessionResult,
  fetchParticipantSessions,
} from '../services/clinicianService';

const formatDate = (value) => {
  if (!value) {
    return 'No saved session';
  }
  return new Date(value).toLocaleString();
};

export default function ClinicianDashboardPage() {
  const navigate = useNavigate();
  const [participants, setParticipants] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [selectedParticipant, setSelectedParticipant] = useState(null);
  const [selectedSession, setSelectedSession] = useState(null);
  const [compareSession, setCompareSession] = useState(null);
  const [compareMode, setCompareMode] = useState(false);
  const [result, setResult] = useState(null);
  const [loadingParticipants, setLoadingParticipants] = useState(true);
  const [loadingSessions, setLoadingSessions] = useState(false);
  const [loadingResult, setLoadingResult] = useState(false);
  const [error, setError] = useState('');
  const [exporting, setExporting] = useState(false);
  const [exportMessage, setExportMessage] = useState('');
  const [showExportPanel, setShowExportPanel] = useState(false);
  const [exportFilters, setExportFilters] = useState({
    taskType: '',
    startDate: '',
    endDate: '',
    assignedOnly: false,
  });

  useEffect(() => {
    let active = true;

    fetchClinicianParticipants()
      .then((data) => {
        if (!active) {
          return;
        }
        setParticipants(data);
        setSelectedParticipant(data[0] ?? null);
        setLoadingSessions(Boolean(data[0]));
        setLoadingParticipants(false);
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Participants could not be loaded.');
          setLoadingParticipants(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!selectedParticipant) {
      return undefined;
    }

    let active = true;

    fetchParticipantSessions(selectedParticipant.participantId)
      .then((data) => {
        if (!active) {
          return;
        }
        setSessions(data);
        setSelectedSession(data[0] ?? null);
        setCompareSession(data[1] ?? null);
        setLoadingResult(Boolean(data[0]));
        setLoadingSessions(false);
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Participant sessions could not be loaded.');
          setLoadingSessions(false);
        }
      });

    return () => {
      active = false;
    };
  }, [selectedParticipant]);

  useEffect(() => {
    if (!selectedSession) {
      return undefined;
    }

    let active = true;

    fetchClinicianSessionResult(selectedSession.sessionId)
      .then((data) => {
        if (active) {
          setResult(data);
          setLoadingResult(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Full session result could not be loaded.');
          setLoadingResult(false);
        }
      });

    return () => {
      active = false;
    };
  }, [selectedSession]);

  const handleSelectParticipant = (participant) => {
    setSelectedParticipant(participant);
    setSessions([]);
    setSelectedSession(null);
    setCompareSession(null);
    setResult(null);
    setLoadingSessions(true);
  };

  const handleSelectSession = (session) => {
    if (compareMode) {
      if (!compareSession || compareSession.sessionId === session.sessionId) {
        setCompareSession(session);
        return;
      }
      if (selectedSession?.sessionId === session.sessionId) {
        setSelectedSession(compareSession);
        setCompareSession(session);
        return;
      }
      setCompareSession(session);
      return;
    }

    setSelectedSession(session);
    setResult(null);
    setLoadingResult(true);
  };

  const handleCompare = () => {
    if (!selectedParticipant || !selectedSession || !compareSession) {
      setError('Select two sessions to compare.');
      return;
    }
    if (selectedSession.sessionId === compareSession.sessionId) {
      setError('Select two different sessions.');
      return;
    }

    navigate(
      `/clinician/participants/${selectedParticipant.participantId}/compare?sessionA=${selectedSession.sessionId}&sessionB=${compareSession.sessionId}`
    );
  };

  const handleExport = async () => {
    setExporting(true);
    setExportMessage('');
    setError('');

    try {
      await exportCognitiveMetrics({
        taskType: exportFilters.taskType || null,
        startDate: exportFilters.startDate || null,
        endDate: exportFilters.endDate || null,
        assignedOnly: exportFilters.assignedOnly,
      });
      setExportMessage('Export downloaded successfully.');
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Cognitive export failed.');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="stack-lg">
      <PageHero
        eyebrow="Clinician"
        title="Participant sessions"
        description="Pick a participant and session to see results."
      />

      <div className="actions-row">
        <Link to="/clinician/group" className="button-link">Group trends</Link>
        <Button variant="secondary" onClick={() => setShowExportPanel((value) => !value)}>
          Export options
        </Button>
        <Button onClick={handleExport} disabled={exporting}>
          {exporting ? 'Exporting...' : 'Download CSV'}
        </Button>
        <Button
          variant="secondary"
          onClick={() => setCompareMode((value) => !value)}
        >
          {compareMode ? 'Exit compare mode' : 'Compare sessions'}
        </Button>
        {compareMode ? (
          <Button onClick={handleCompare} disabled={!selectedSession || !compareSession}>
            Open comparison
          </Button>
        ) : null}
      </div>

      {showExportPanel ? (
        <Card title="Export filters">
          <div className="form-grid">
            <label className="form-field">
              <span>Task type</span>
              <select
                value={exportFilters.taskType}
                onChange={(event) => setExportFilters((current) => ({ ...current, taskType: event.target.value }))}
              >
                <option value="">All tasks</option>
                <option value="memory-span">Memory Span</option>
                <option value="stroop">Stroop</option>
                <option value="dual-n-back">Dual N-Back</option>
              </select>
            </label>
            <label className="form-field">
              <span>Start date</span>
              <input
                type="date"
                value={exportFilters.startDate}
                onChange={(event) => setExportFilters((current) => ({ ...current, startDate: event.target.value }))}
              />
            </label>
            <label className="form-field">
              <span>End date</span>
              <input
                type="date"
                value={exportFilters.endDate}
                onChange={(event) => setExportFilters((current) => ({ ...current, endDate: event.target.value }))}
              />
            </label>
            <label className="form-field">
              <span>Assigned participants only</span>
              <input
                type="checkbox"
                checked={exportFilters.assignedOnly}
                onChange={(event) => setExportFilters((current) => ({ ...current, assignedOnly: event.target.checked }))}
              />
            </label>
          </div>
        </Card>
      ) : null}

      {exportMessage ? <StatusMessage tone="success" message={exportMessage} /> : null}
      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}

      <div className="clinician-grid">
        <Card title="Participants" accent="cool">
          {loadingParticipants ? <LoadingState label="Loading participants..." /> : null}
          {!loadingParticipants && participants.length === 0 ? (
            <StatusMessage tone="warning" message="No accessible participants yet. Assign a participant or receive data-sharing consent." />
          ) : null}
          <div className="participant-list">
            {participants.map((participant) => (
              <button
                key={participant.participantId}
                type="button"
                className={`participant-button ${selectedParticipant?.participantId === participant.participantId ? 'participant-button-active' : ''}`}
                onClick={() => handleSelectParticipant(participant)}
              >
                <strong>{participant.email}</strong>
                <span>{participant.sessionCount} saved sessions</span>
                <span>Latest: {formatDate(participant.latestSessionTime)}</span>
                <Link
                  to={`/clinician/participants/${participant.participantId}`}
                  className="inline-link"
                  onClick={(event) => event.stopPropagation()}
                >
                  Manage profile
                </Link>
              </button>
            ))}
          </div>
        </Card>

        <Card title={compareMode ? 'Select two sessions' : 'Sessions'}>
          {compareMode ? (
            <StatusMessage tone="neutral" message="Click one session as baseline, then another session to compare." />
          ) : null}
          {loadingSessions ? <LoadingState label="Loading participant sessions..." /> : null}
          {!loadingSessions && selectedParticipant && sessions.length === 0 ? (
            <StatusMessage tone="warning" message="This participant has no saved sessions yet." />
          ) : null}
          <div className="trial-log">
            {sessions.map((session) => {
              const isPrimary = selectedSession?.sessionId === session.sessionId;
              const isCompare = compareSession?.sessionId === session.sessionId;
              return (
                <button
                  key={session.sessionId}
                  type="button"
                  className={`session-button ${isPrimary || isCompare ? 'session-button-active' : ''}`}
                  onClick={() => handleSelectSession(session)}
                >
                  <span>{session.taskTitle}</span>
                  <span>{session.difficultyLevel}</span>
                  <span>{formatDate(session.startTime)}</span>
                  <span>{session.avgReactionTime ?? '-'} ms</span>
                  <span>{session.accuracy ?? '-'}%</span>
                  {compareMode && isPrimary ? <span className="chip">Session A</span> : null}
                  {compareMode && isCompare && !isPrimary ? <span className="chip">Session B</span> : null}
                </button>
              );
            })}
          </div>
        </Card>
      </div>

      {loadingResult ? <LoadingState label="Loading full session result..." /> : null}

      {result ? (
        <>
          <div className="dashboard-grid results-metrics">
            <Card title="Participant"><p className="metric-value clinician-email">{selectedParticipant?.email}</p></Card>
            <Card title="Average Reaction Time" accent="cool"><p className="metric-value">{result.avgReactionTime} ms</p></Card>
            <Card title="Accuracy" accent="warm"><p className="metric-value">{result.accuracy}%</p></Card>
            <Card title="Error Rate"><p className="metric-value">{result.errorRate}%</p></Card>
          </div>

          {sessions.length >= 2 ? (
            <Card title="Performance trend" accent="warm">
              <ParticipantTrendChart sessions={sessions} />
            </Card>
          ) : null}

          <ResultsCharts result={result} />

          <Card title="Full Trial Log">
            <TrialLog trials={result.trials} variant="clinician" />
          </Card>
        </>
      ) : null}
    </div>
  );
}
