// Memory span — show digits, user repeats on keypad
// Span starts at 3, goes up on correct recall, ends after 2 wrong at same level

import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import LoadingState from '../../components/ui/LoadingState';
import StatusMessage from '../../components/ui/StatusMessage';
import { completeSession } from '../../services/sessionService';

const MIN_SPAN = 3;
const MAX_SPAN = 7;
const DIGIT_DISPLAY_MS = 900;
const DIGIT_GAP_MS = 350;
const MAX_FAILURES = 2;
const FEEDBACK_MS = 900;

function randomDigit() {
  return Math.floor(Math.random() * 10);
}

function buildSequence(length) {
  return Array.from({ length }, randomDigit);
}

export default function MemorySpanGame({ task }) {
  const navigate = useNavigate();
  const [span, setSpan] = useState(MIN_SPAN);
  const [phase, setPhase] = useState('ready');
  const [sequence, setSequence] = useState([]);
  const [shownDigit, setShownDigit] = useState('');
  const [input, setInput] = useState([]);
  const [responses, setResponses] = useState([]);
  const [failuresAtSpan, setFailuresAtSpan] = useState(0);
  const [feedback, setFeedback] = useState(null);
  const [finishing, setFinishing] = useState(false);
  const [error, setError] = useState('');
  const sessionStartedAt = useRef(Date.now());
  const recallStartedAtRef = useRef(Date.now());

  const finishSession = useCallback(async (nextResponses) => {
    setFinishing(true);
    setError('');

    try {
      const maxSpan = nextResponses.reduce((best, trial) => {
        const length = trial.stimulus.split('-').length;
        return trial.correct && length > best ? length : best;
      }, MIN_SPAN - 1);

      const result = await completeSession({
        task: {
          ...task,
          difficulty: `Progressive span (max ${Math.max(maxSpan, MIN_SPAN)})`,
        },
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

  const startRound = useCallback((nextSpan) => {
    const nextSequence = buildSequence(nextSpan);
    setSequence(nextSequence);
    setInput([]);
    setFeedback(null);
    setPhase('showing');
    setShownDigit('');

    let index = 0;
    const showNext = () => {
      if (index >= nextSequence.length) {
        setShownDigit('');
        setPhase('recall');
        recallStartedAtRef.current = Date.now();
        return;
      }

      setShownDigit(String(nextSequence[index]));
      index += 1;
      setTimeout(() => {
        setShownDigit('');
        setTimeout(showNext, DIGIT_GAP_MS);
      }, DIGIT_DISPLAY_MS);
    };

    showNext();
  }, []);

  useEffect(() => {
    startRound(MIN_SPAN);
  }, [startRound]);

  const continueAfterFeedback = (nextResponses, nextSpan, nextFailures, shouldFinish) => {
    if (shouldFinish) {
      finishSession(nextResponses);
      return;
    }

    setResponses(nextResponses);
    setSpan(nextSpan);
    setFailuresAtSpan(nextFailures);
    setTimeout(() => startRound(nextSpan), FEEDBACK_MS);
  };

  const handleDigitPress = (digit) => {
    if (phase !== 'recall' || finishing) {
      return;
    }

    const nextInput = [...input, digit];
    setInput(nextInput);

    if (nextInput.length < span) {
      return;
    }

    const expected = sequence.join('-'); // e.g. "3-7-1" stored as stimulus
    const actual = nextInput.join('-');
    const correct = actual === expected;
    const reactionTime = Math.max(300, Date.now() - recallStartedAtRef.current);
    const nextResponses = [
      ...responses,
      {
        stimulus: expected,
        response: actual,
        correct,
        reactionTime,
        timestamp: new Date().toISOString(),
      },
    ];

    const shouldFinish = nextResponses.length >= 8
      || (!correct && failuresAtSpan + 1 >= MAX_FAILURES && span >= MIN_SPAN);

    setFeedback({
      correct,
      message: correct
        ? `Correct — span increases to ${Math.min(MAX_SPAN, span + 1)}`
        : `Incorrect — expected ${expected.replace(/-/g, ' ')}`,
    });
    setPhase('feedback');

    if (correct) {
      continueAfterFeedback(
        nextResponses,
        Math.min(MAX_SPAN, span + 1),
        0,
        shouldFinish
      );
      return;
    }

    const nextFailures = failuresAtSpan + 1;
    continueAfterFeedback(nextResponses, span, nextFailures, shouldFinish || nextFailures >= MAX_FAILURES);
  };

  const handleClear = () => {
    if (phase === 'recall') {
      setInput([]);
    }
  };

  return (
    <div className="stack-lg">
      {error ? <StatusMessage tone="error" title="Save failed" message={error} /> : null}
      <Card title={task.title} accent="cool" footer={`Current span: ${span} digits`}>
        <p className="eyebrow">
          {phase === 'showing' && 'Watch the digits carefully.'}
          {phase === 'recall' && 'Enter the sequence in the same order.'}
          {phase === 'feedback' && 'Round feedback'}
          {phase === 'ready' && 'Preparing sequence...'}
        </p>
        {phase === 'feedback' && feedback ? (
          <div className={`trial-feedback ${feedback.correct ? 'trial-feedback-good' : 'trial-feedback-bad'}`}>
            {feedback.message}
          </div>
        ) : (
          <div className="stimulus-card memory-span-display">
            {phase === 'showing' ? shownDigit || '·' : phase === 'recall' ? input.join(' ') || 'Your turn' : '—'}
          </div>
        )}
        {phase === 'recall' ? (
          <>
            <div className="digit-keypad">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9, 0].map((digit) => (
                <Button key={digit} onClick={() => handleDigitPress(digit)}>{digit}</Button>
              ))}
            </div>
            <div className="actions-row">
              <Button variant="secondary" onClick={handleClear}>Clear</Button>
            </div>
          </>
        ) : null}
      </Card>
      {finishing ? <LoadingState label="Saving session..." /> : null}
    </div>
  );
}
