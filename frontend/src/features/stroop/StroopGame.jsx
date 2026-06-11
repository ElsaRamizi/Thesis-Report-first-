// Stroop colour-word task (thesis task 2)
// User picks ink colour; word may match or not (congruent / incongruent)
// All trials sent to backend in one POST when the run finishes

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import LoadingState from '../../components/ui/LoadingState';
import StatusMessage from '../../components/ui/StatusMessage';
import { completeSession } from '../../services/sessionService';

const COLOR_OPTIONS = [
  { id: 'red', label: 'Red', word: 'RED', hex: '#b64d4d' },
  { id: 'blue', label: 'Blue', word: 'BLUE', hex: '#1f5f7a' },
  { id: 'green', label: 'Green', word: 'GREEN', hex: '#216c4a' },
  { id: 'yellow', label: 'Yellow', word: 'YELLOW', hex: '#b8860b' },
];

const TOTAL_TRIALS = 40;
const FEEDBACK_MS = 650;

function buildTrials() {
  // alternate congruent / incongruent; random ink colour each trial
  const trials = [];
  for (let index = 0; index < TOTAL_TRIALS; index += 1) {
    const ink = COLOR_OPTIONS[Math.floor(Math.random() * COLOR_OPTIONS.length)];
    const congruent = index % 2 === 0;
    const word = congruent ? ink : COLOR_OPTIONS.filter((option) => option.id !== ink.id)[Math.floor(Math.random() * 3)];
    trials.push({
      inkColor: ink.id,
      inkHex: ink.hex,
      word: word.word,
      congruent,
      correctResponse: ink.label,
    });
  }
  return trials;
}

export default function StroopGame({ task }) {
  const navigate = useNavigate();
  const trials = useMemo(() => buildTrials(), []);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [responses, setResponses] = useState([]);
  const [phase, setPhase] = useState('ready');
  const [lastFeedback, setLastFeedback] = useState(null);
  const [hint, setHint] = useState('');
  const [finishing, setFinishing] = useState(false);
  const [error, setError] = useState('');
  const sessionStartedAt = useRef(Date.now());
  const trialStartedAtRef = useRef(Date.now());

  useEffect(() => {
    if (phase === 'ready') {
      trialStartedAtRef.current = Date.now();
    }
  }, [currentIndex, phase]);

  const finishSession = useCallback(async (nextResponses) => {
    setFinishing(true);
    setError('');

    try {
      const result = await completeSession({
        task: { ...task, difficulty: 'Fixed' },
        trials: nextResponses,
        startedAt: sessionStartedAt.current,
        endedAt: Date.now(),
      });
      navigate('/session/complete', { state: { result } });
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Save failed.');
      setFinishing(false);
    }
  }, [navigate, task]);

  const advanceTrial = (nextResponses, feedback) => {
    if ((currentIndex + 1) % 10 === 0) {
      setHint('Ignore the word — pick the ink colour.');
    } else {
      setHint('');
    }

    if (currentIndex === trials.length - 1) {
      finishSession(nextResponses);
      return;
    }

    setResponses(nextResponses);
    setLastFeedback(feedback);
    setPhase('feedback');
    setTimeout(() => {
      setPhase('ready');
      setCurrentIndex((value) => value + 1);
    }, FEEDBACK_MS);
  };

  const handleResponse = (option) => {
    if (finishing || phase !== 'ready') {
      return;
    }

    const trial = trials[currentIndex];
    const reactionTime = Math.max(200, Date.now() - trialStartedAtRef.current);
    const correct = option.label === trial.correctResponse;
    const nextResponses = [
      ...responses,
      {
        // stimulus string format: WORD|colourId|congruent|incongruent (used for metrics on backend)
        stimulus: `${trial.word}|${trial.inkColor}|${trial.congruent ? 'congruent' : 'incongruent'}`,
        response: option.label,
        correct,
        reactionTime,
        timestamp: new Date().toISOString(),
      },
    ];

    advanceTrial(nextResponses, {
      correct,
      message: correct ? 'Correct' : 'Incorrect',
    });
  };

  const trial = trials[currentIndex];
  const progress = Math.round(((currentIndex + 1) / trials.length) * 100);

  return (
    <div className="stack-lg">
      {error ? <StatusMessage tone="error" title="Save failed" message={error} /> : null}
      <Card title={task.title} accent="warm" footer={`Trial ${currentIndex + 1} of ${trials.length}`}>
        <div className="progress-shell">
          <div className="progress-bar" style={{ width: `${progress}%` }} />
        </div>
        <p className="eyebrow">Select the ink color, not the word meaning.</p>
        {hint ? <StatusMessage tone="neutral" message={hint} /> : null}
        {phase === 'feedback' && lastFeedback ? (
          <div className={`trial-feedback ${lastFeedback.correct ? 'trial-feedback-good' : 'trial-feedback-bad'}`}>
            {lastFeedback.message}
          </div>
        ) : (
          <>
            <div className="stimulus-card stroop-stimulus" style={{ color: trial.inkHex }}>
              {trial.word}
            </div>
            <div className="options-grid stroop-options">
              {COLOR_OPTIONS.map((option) => (
                <Button key={option.id} onClick={() => handleResponse(option)}>
                  {option.label}
                </Button>
              ))}
            </div>
          </>
        )}
      </Card>
      {finishing ? <LoadingState label="Saving session..." /> : null}
    </div>
  );
}
