import { useEffect, useState } from 'react';

import { Link, useParams, useSearchParams } from 'react-router-dom';

import PageHero from '../components/layout/PageHero';

import Card from '../components/ui/Card';

import Button from '../components/ui/Button';

import LoadingState from '../components/ui/LoadingState';

import StatusMessage from '../components/ui/StatusMessage';

import MultiSessionCompareCharts from '../features/clinician/MultiSessionCompareCharts';

import SessionCompareCharts from '../features/clinician/SessionCompareCharts';

import TrialLog from '../features/results/TrialLog';

import {

  compareMultipleSessions,

  compareParticipantSessions,

  fetchParticipantSessions,

} from '../services/clinicianService';



const formatSessionLabel = (session) => {

  if (!session) {

    return '';

  }

  return `${session.taskTitle} · ${new Date(session.startTime).toLocaleString()}`;

};



export default function SessionComparePage() {

  const { participantId } = useParams();

  const [searchParams, setSearchParams] = useSearchParams();

  const [sessions, setSessions] = useState([]);

  const [sessionAId, setSessionAId] = useState(searchParams.get('sessionA') ?? '');

  const [sessionBId, setSessionBId] = useState(searchParams.get('sessionB') ?? '');

  const [selectedIds, setSelectedIds] = useState([]);

  const [multiMode, setMultiMode] = useState(Boolean(searchParams.get('multi')));

  const [pairComparison, setPairComparison] = useState(null);

  const [multiComparison, setMultiComparison] = useState(null);

  const [loadingSessions, setLoadingSessions] = useState(true);

  const [loadingComparison, setLoadingComparison] = useState(false);

  const [error, setError] = useState('');



  useEffect(() => {

    let active = true;



    fetchParticipantSessions(participantId)

      .then((data) => {

        if (!active) {

          return;

        }

        setSessions(data);

        if (!searchParams.get('sessionA') && data[0]) {

          setSessionAId(String(data[0].sessionId));

        }

        if (!searchParams.get('sessionB') && data[1]) {

          setSessionBId(String(data[1].sessionId));

        }

        setLoadingSessions(false);

      })

      .catch((requestError) => {

        if (active) {

          setError(requestError.response?.data?.message ?? 'Sessions could not be loaded.');

          setLoadingSessions(false);

        }

      });



    return () => {

      active = false;

    };

  }, [participantId, searchParams]);



  useEffect(() => {

    const paramA = searchParams.get('sessionA');

    const paramB = searchParams.get('sessionB');

    if (multiMode || !paramA || !paramB || paramA === paramB || loadingSessions) {

      return;

    }



    setLoadingComparison(true);

    compareParticipantSessions(paramA, paramB)

      .then((data) => setPairComparison(data))

      .catch((requestError) => {

        setError(requestError.response?.data?.message ?? 'Sessions could not be compared.');

      })

      .finally(() => setLoadingComparison(false));

  }, [loadingSessions, multiMode, searchParams]);



  const handlePairCompare = async () => {

    if (!sessionAId || !sessionBId || sessionAId === sessionBId) {

      setError('Select two different sessions to compare.');

      return;

    }



    setLoadingComparison(true);

    setError('');

    setSearchParams({ sessionA: sessionAId, sessionB: sessionBId });



    try {

      const data = await compareParticipantSessions(sessionAId, sessionBId);

      setPairComparison(data);

      setMultiComparison(null);

    } catch (requestError) {

      setError(requestError.response?.data?.message ?? 'Sessions could not be compared.');

    } finally {

      setLoadingComparison(false);

    }

  };



  const handleMultiCompare = async () => {

    if (selectedIds.length < 2) {

      setError('Select at least two sessions.');

      return;

    }



    setLoadingComparison(true);

    setError('');

    setSearchParams({ multi: '1', sessions: selectedIds.join(',') });



    try {

      const data = await compareMultipleSessions(selectedIds.map(Number));

      setMultiComparison(data);

      setPairComparison(null);

    } catch (requestError) {

      setError(requestError.response?.data?.message ?? 'Sessions could not be compared.');

    } finally {

      setLoadingComparison(false);

    }

  };



  const toggleSelected = (sessionId) => {

    setSelectedIds((current) => (

      current.includes(sessionId)

        ? current.filter((id) => id !== sessionId)

        : [...current, sessionId]

    ));

  };



  const sessionA = sessions.find((session) => String(session.sessionId) === String(sessionAId));

  const sessionB = sessions.find((session) => String(session.sessionId) === String(sessionBId));



  return (

    <div className="stack-lg">

      <PageHero

        eyebrow="Compare"
        title={multiMode ? 'Several sessions' : 'Two sessions'}
        description="Pick sessions and see charts + trial log."

      />



      <Link to={`/clinician/participants/${participantId}`} className="inline-link">Back to participant</Link>



      <div className="actions-row">

        <Button variant="secondary" onClick={() => setMultiMode((value) => !value)}>

          {multiMode ? 'Compare two instead' : 'Compare several sessions'}

        </Button>

      </div>



      {error ? <StatusMessage tone="error" title="Error" message={error} /> : null}



      <Card title={multiMode ? 'Select sessions' : 'Select two sessions'}>

        {loadingSessions ? <LoadingState label="Loading sessions..." /> : null}

        {!loadingSessions && multiMode ? (

          <>

            <div className="trial-log">

              {sessions.map((session) => (

                <label key={session.sessionId} className="session-button">

                  <input

                    type="checkbox"

                    checked={selectedIds.includes(session.sessionId)}

                    onChange={() => toggleSelected(session.sessionId)}

                  />

                  <span>{formatSessionLabel(session)}</span>

                  <span>{session.accuracy ?? '-'}%</span>

                  <span>{session.avgReactionTime ?? '-'} ms</span>

                </label>

              ))}

            </div>

            <div className="actions-row">

              <Button onClick={handleMultiCompare} disabled={loadingComparison || selectedIds.length < 2}>

                {loadingComparison ? 'Comparing...' : `Compare ${selectedIds.length || 0} sessions`}

              </Button>

            </div>

          </>

        ) : null}

        {!loadingSessions && !multiMode ? (

          <>

            <div className="form-grid">

              <label className="form-field">

                <span>Session A</span>

                <select value={sessionAId} onChange={(event) => setSessionAId(event.target.value)}>

                  <option value="">Choose session</option>

                  {sessions.map((session) => (

                    <option key={session.sessionId} value={session.sessionId}>

                      {formatSessionLabel(session)}

                    </option>

                  ))}

                </select>

              </label>

              <label className="form-field">

                <span>Session B</span>

                <select value={sessionBId} onChange={(event) => setSessionBId(event.target.value)}>

                  <option value="">Choose session</option>

                  {sessions.map((session) => (

                    <option key={session.sessionId} value={session.sessionId}>

                      {formatSessionLabel(session)}

                    </option>

                  ))}

                </select>

              </label>

            </div>

            <div className="actions-row">

              <Button onClick={handlePairCompare} disabled={loadingComparison}>

                {loadingComparison ? 'Comparing...' : 'Compare sessions'}

              </Button>

            </div>

          </>

        ) : null}

      </Card>



      {pairComparison ? (

        <>

          {pairComparison.sessionA.taskId !== pairComparison.sessionB.taskId ? (

            <StatusMessage

              tone="warning"

              title="Different task types"

              message="These sessions use different tasks. Compare metrics with caution."

            />

          ) : null}



          <Card title="Comparison summary" accent="warm">

            <p>{pairComparison.comparison.summary}</p>

            <div className="dashboard-grid results-metrics">

              <Card title="Accuracy change"><p className="metric-value">{pairComparison.comparison.accuracyDeltaPercent ?? '—'}%</p></Card>

              <Card title="Reaction time change"><p className="metric-value">{pairComparison.comparison.reactionTimeDeltaPercent ?? '—'}%</p></Card>

              <Card title="Error rate change"><p className="metric-value">{pairComparison.comparison.errorRateDeltaPercent ?? '—'}%</p></Card>

            </div>

          </Card>



          <SessionCompareCharts

            sessionA={pairComparison.sessionA}

            sessionB={pairComparison.sessionB}

            labelA={formatSessionLabel(sessionA)}

            labelB={formatSessionLabel(sessionB)}

          />



          <div className="compare-grid">

            <Card title="Session A trial log">

              <TrialLog trials={pairComparison.sessionA.trials} variant="clinician" />

            </Card>

            <Card title="Session B trial log">

              <TrialLog trials={pairComparison.sessionB.trials} variant="clinician" />

            </Card>

          </div>

        </>

      ) : null}



      {multiComparison ? (

        <>

          <Card title="Multi-session summary" accent="warm">

            <p>{multiComparison.summary}</p>

            <p>{multiComparison.baselineComparison?.summary}</p>

          </Card>



          <MultiSessionCompareCharts timeline={multiComparison.timeline} />



          <Card title="Selected sessions">

            <div className="trial-log">

              {multiComparison.sessions.map((session) => (

                <div key={session.sessionId} className="annotation-item">

                  <strong>{session.taskTitle}</strong>

                  <span>{new Date(session.startTime).toLocaleString()}</span>

                  <span>{session.accuracy}% accuracy · {session.avgReactionTime} ms RT</span>

                </div>

              ))}

            </div>

          </Card>

        </>

      ) : null}

    </div>

  );

}


