import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import LoadingState from '../../components/ui/LoadingState';
import StatusMessage from '../../components/ui/StatusMessage';
import {
  DEFAULT_DUAL_N_BACK_CONFIG,
  advanceBlock,
  appendTrialToHistory,
  buildTrialPayload,
  computeSessionSummary,
  createInitialEngineState,
  evaluateTrial,
  generateTrial,
} from './gameEngine';
import Controls from './Controls';
import ResultsSummary from './ResultsSummary';
import StimulusDisplay from './StimulusDisplay';
import { endLiveSession, recordLiveTrial, startLiveSession } from '../../services/sessionService';

export default function GameContainer({ task }) {
  const [selectedN, setSelectedN] = useState(DEFAULT_DUAL_N_BACK_CONFIG.initialN);
  const [gameMode, setGameMode] = useState('adaptive');
  const config = useMemo(() => ({
    ...DEFAULT_DUAL_N_BACK_CONFIG,
    initialN: selectedN,
  }), [selectedN]);
  const totalTrials = config.blockSize * config.totalBlocks;
  const trialWindow = config.stimulusDuration + config.interStimulusInterval;

  const [status, setStatus] = useState('setup');
  const [error, setError] = useState('');
  const [currentTrial, setCurrentTrial] = useState(null);
  const [phase, setPhase] = useState('stimulus');
  const [elapsed, setElapsed] = useState(0);
  const [responses, setResponses] = useState({});
  const [blockMetrics, setBlockMetrics] = useState([]);
  const [completedTrials, setCompletedTrials] = useState(0);
  const [currentBlock, setCurrentBlock] = useState(1);
  const [liveSummary, setLiveSummary] = useState(() => computeSessionSummary([]));
  const [result, setResult] = useState(null);

  const startedRef = useRef(false);
  const sessionIdRef = useRef(null);
  const engineRef = useRef(null);
  const responsesRef = useRef({});
  const evaluatedTrialsRef = useRef([]);
  const activeBlockTrialsRef = useRef([]);
  const timingRef = useRef({ stimulusStartEpoch: 0, performanceStart: 0 });
  const finalizingRef = useRef(false);

  const beginNextTrial = useCallback(() => {
    const trial = generateTrial(engineRef.current);
    engineRef.current = appendTrialToHistory(engineRef.current, trial);
    responsesRef.current = {};
    finalizingRef.current = false;
    timingRef.current = {
      stimulusStartEpoch: Date.now(),
      performanceStart: performance.now(),
    };
    setResponses({});
    setElapsed(0);
    setPhase('stimulus');
    setCurrentTrial(trial);
    setStatus('running');
  }, []);

  const startGame = useCallback(() => {
    if (startedRef.current) {
      return;
    }

    startedRef.current = true;
    setStatus('starting');
    engineRef.current = createInitialEngineState(config);
    startLiveSession({
      task: {
        ...task,
        difficulty: gameMode === 'adaptive' ? `Adaptive N, start N=${selectedN}` : `Fixed N=${selectedN}`,
      },
      initialN: config.initialN,
      startedAt: Date.now(),
    })
      .then((session) => {
        sessionIdRef.current = session.sessionId;
        beginNextTrial();
      })
      .catch((requestError) => {
        setError(requestError.response?.data?.message ?? 'The Dual N-Back session could not be started.');
        setStatus('error');
      });
  }, [beginNextTrial, config, gameMode, selectedN, task]);

  const recordInput = useCallback((stream) => {
    if (!currentTrial || finalizingRef.current || responsesRef.current[stream]) {
      return;
    }

    const elapsedMs = Math.round(performance.now() - timingRef.current.performanceStart);
    const input = {
      reactionTime: elapsedMs,
      valid: elapsedMs <= trialWindow,
    };

    responsesRef.current = {
      ...responsesRef.current,
      [stream]: input,
    };
    setResponses(responsesRef.current);
  }, [currentTrial, trialWindow]);

  const finalizeCurrentTrial = useCallback(async () => {
    if (!currentTrial || finalizingRef.current) {
      return;
    }

    finalizingRef.current = true;
    const timestampEpoch = Date.now();
    const evaluatedTrial = evaluateTrial({
      trial: currentTrial,
      responses: responsesRef.current,
    });

    evaluatedTrialsRef.current = [...evaluatedTrialsRef.current, evaluatedTrial];
    activeBlockTrialsRef.current = [...activeBlockTrialsRef.current, evaluatedTrial];
    setCompletedTrials(evaluatedTrialsRef.current.length);
    setLiveSummary(computeSessionSummary(evaluatedTrialsRef.current));

    try {
      await recordLiveTrial({
        sessionId: sessionIdRef.current,
        trial: buildTrialPayload({
          evaluatedTrial,
          stimulusStartEpoch: timingRef.current.stimulusStartEpoch,
          timestampEpoch,
        }),
      });

      const completedTrials = evaluatedTrialsRef.current.length;
      const blockFinished = completedTrials % config.blockSize === 0;
      const sessionFinished = completedTrials >= totalTrials;

      if (blockFinished) {
        const { nextState, metrics } = advanceBlock(engineRef.current, activeBlockTrialsRef.current);
        const fixedNextState = {
          ...nextState,
          nLevel: engineRef.current.nLevel,
        };
        const fixedMetrics = {
          ...metrics,
          nextN: engineRef.current.nLevel,
        };

        engineRef.current = nextState;
        if (gameMode === 'fixed') {
          engineRef.current = fixedNextState;
        }
        activeBlockTrialsRef.current = [];
        setBlockMetrics((items) => [...items, gameMode === 'fixed' ? fixedMetrics : metrics]);
        setCurrentBlock(engineRef.current.currentBlock);
      }

      if (sessionFinished) {
        setStatus('saving');
        const finalN = engineRef.current.nLevel;
        const savedResult = await endLiveSession({
          sessionId: sessionIdRef.current,
          endedAt: Date.now(),
          finalN,
        });
        setResult(savedResult);
        setStatus('complete');
        return;
      }

      beginNextTrial();
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'A trial could not be saved.');
      setStatus('error');
    }
  }, [beginNextTrial, config.blockSize, currentTrial, gameMode, totalTrials]);

  useEffect(() => {
    if (status !== 'running' || !currentTrial) {
      return undefined;
    }

    const timer = window.setInterval(() => {
      const elapsedMs = Math.round(performance.now() - timingRef.current.performanceStart);
      setElapsed(elapsedMs);

      if (elapsedMs >= config.stimulusDuration) {
        setPhase('response');
      }

      if (elapsedMs >= trialWindow) {
        finalizeCurrentTrial();
      }
    }, 25);

    return () => window.clearInterval(timer);
  }, [config.stimulusDuration, currentTrial, finalizeCurrentTrial, status, trialWindow]);

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.repeat) {
        return;
      }

      if (event.key.toLowerCase() === 'a') {
        recordInput('position');
      }

      if (event.key.toLowerCase() === 'l') {
        recordInput('letter');
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [recordInput]);

  if (status === 'setup') {
    return (
      <Card title="Choose Starting N" accent="cool">
        <p>
          Choose how far back you want to remember. If N = 2, compare the current square and letter with what appeared 2 rounds ago.
        </p>
        <div className="mode-picker" role="group" aria-label="Choose Dual N-Back mode">
          <button
            type="button"
            className={`mode-picker-button ${gameMode === 'adaptive' ? 'mode-picker-button-active' : ''}`}
            onClick={() => setGameMode('adaptive')}
          >
            <strong>Adaptive N</strong>
            <span>The game can make N higher or lower after each 20-round block.</span>
          </button>
          <button
            type="button"
            className={`mode-picker-button ${gameMode === 'fixed' ? 'mode-picker-button-active' : ''}`}
            onClick={() => setGameMode('fixed')}
          >
            <strong>Fixed N</strong>
            <span>You choose N once, and it stays the same for the whole game.</span>
          </button>
        </div>
        <div className="n-picker" role="group" aria-label="Choose starting N level">
          {Array.from({ length: config.maxN }, (_, index) => index + 1).map((level) => (
            <button
              key={level}
              type="button"
              className={`n-picker-button ${selectedN === level ? 'n-picker-button-active' : ''}`}
              onClick={() => setSelectedN(level)}
            >
              N = {level}
            </button>
          ))}
        </div>
        <div className="actions-row">
          <Button onClick={startGame}>Start Dual N-Back</Button>
        </div>
      </Card>
    );
  }

  if (status === 'starting') {
    return <LoadingState label="Starting adaptive Dual N-Back..." />;
  }

  if (status === 'error') {
    return <StatusMessage tone="error" title="Dual N-Back stopped" message={error} />;
  }

  if (status === 'saving') {
    return <LoadingState label="Saving trial data and computing metrics..." />;
  }

  if (status === 'complete' && result) {
    return <ResultsSummary result={result} />;
  }

  const progress = Math.round((completedTrials / totalTrials) * 100);
  const responseSeconds = Math.max(0, ((trialWindow - elapsed) / 1000).toFixed(1));

  return (
    <div className="stack-lg">
      <Card title={task.title} accent="cool" footer={`Trial ${completedTrials + 1} of ${totalTrials}`}>
        <div className="progress-shell">
          <div className="progress-bar" style={{ width: `${progress}%` }} />
        </div>
        <div className="dual-meta">
          <span>N = {currentTrial?.nLevel}</span>
          <span>{gameMode === 'adaptive' ? 'Adaptive' : 'Fixed N'}</span>
          <span>Block {currentBlock} of {config.totalBlocks}</span>
          <span>{phase === 'stimulus' ? 'Stimulus' : `${responseSeconds}s response window`}</span>
        </div>
        <StimulusDisplay trial={currentTrial} phase={phase} />
        <Controls
          onPosition={() => recordInput('position')}
          onLetter={() => recordInput('letter')}
          responses={responses}
          disabled={status !== 'running'}
        />
        <div className="dual-key-hints">
          <span>A = position match</span>
          <span>L = letter match</span>
        </div>
      </Card>

      <div className="dashboard-grid results-metrics">
        <Card title="Live Accuracy"><p className="metric-value">{liveSummary.accuracy}%</p></Card>
        <Card title="False Alarms"><p className="metric-value">{liveSummary.falseAlarmRate}%</p></Card>
        <Card title="Max N"><p className="metric-value">{liveSummary.maxNReached || currentTrial?.nLevel}</p></Card>
      </div>

      {blockMetrics.length ? (
        <Card title="Adaptive Block Log">
          <div className="trial-log">
            {blockMetrics.map((metrics, index) => (
              <div key={`${metrics.previousN}-${index}`} className="trial-row dual-block-row">
                <span>Block {index + 1}</span>
                <span>N {metrics.previousN} to {metrics.nextN}</span>
                <span>{Math.round(metrics.accuracy * 1000) / 10}% accuracy</span>
                <span>{Math.round(metrics.falseAlarmRate * 1000) / 10}% false alarms</span>
                <span>{metrics.averageReactionTime} ms</span>
              </div>
            ))}
          </div>
        </Card>
      ) : null}
    </div>
  );
}
