import { useEffect, useState } from 'react';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import ResultsCharts from '../features/results/ResultsCharts';
import {
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
  const [participants, setParticipants] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [selectedParticipant, setSelectedParticipant] = useState(null);
  const [selectedSession, setSelectedSession] = useState(null);
  const [result, setResult] = useState(null);
  const [loadingParticipants, setLoadingParticipants] = useState(true);
  const [loadingSessions, setLoadingSessions] = useState(false);
  const [loadingResult, setLoadingResult] = useState(false);
  const [error, setError] = useState('');

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
    setResult(null);
    setLoadingSessions(true);
  };

  const handleSelectSession = (session) => {
    setSelectedSession(session);
    setResult(null);
    setLoadingResult(true);
  };

  return (
    <div className="stack-lg">
      <section className="hero-panel">
        <p className="eyebrow">Clinician Workspace</p>
        <h2>Participant results</h2>
        <p>Review participant sessions, performance trends, and full trial-by-trial cognitive task logs.</p>
      </section>

      {error ? <StatusMessage tone="error" title="Clinician data unavailable" message={error} /> : null}

      <div className="clinician-grid">
        <Card title="Participants" accent="cool">
          {loadingParticipants ? <LoadingState label="Loading participants..." /> : null}
          {!loadingParticipants && participants.length === 0 ? (
            <StatusMessage tone="warning" message="No participant accounts have saved results yet." />
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
              </button>
            ))}
          </div>
        </Card>

        <Card title="Sessions">
          {loadingSessions ? <LoadingState label="Loading participant sessions..." /> : null}
          {!loadingSessions && selectedParticipant && sessions.length === 0 ? (
            <StatusMessage tone="warning" message="This participant has no saved sessions yet." />
          ) : null}
          <div className="trial-log">
            {sessions.map((session) => (
              <button
                key={session.sessionId}
                type="button"
                className={`session-button ${selectedSession?.sessionId === session.sessionId ? 'session-button-active' : ''}`}
                onClick={() => handleSelectSession(session)}
              >
                <span>{session.taskTitle}</span>
                <span>{session.difficultyLevel}</span>
                <span>{formatDate(session.startTime)}</span>
                <span>{session.avgReactionTime ?? '-'} ms</span>
                <span>{session.accuracy ?? '-'}%</span>
              </button>
            ))}
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

          <Card title="Full Trial Log">
            <div className="trial-log">
              {result.trials.map((trial) => (
                <div key={trial.index} className="trial-row clinician-trial-row">
                  <span>Trial {trial.index}</span>
                  <span>{trial.letter ? `N${trial.nLevel} | ${trial.letter} @ ${trial.position + 1}` : trial.stimulus}</span>
                  <span>{trial.positionOutcome ? `${trial.positionOutcome} / ${trial.letterOutcome}` : trial.response}</span>
                  <span>{trial.reactionTime} ms</span>
                  <span className={trial.correct ? 'result-good' : 'result-bad'}>{trial.correct ? 'Correct' : 'Error'}</span>
                </div>
              ))}
            </div>
          </Card>
        </>
      ) : null}
    </div>
  );
}
