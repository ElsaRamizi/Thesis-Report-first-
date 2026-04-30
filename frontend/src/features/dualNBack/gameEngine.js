export const OUTCOMES = {
  HIT: 'HIT',
  MISS: 'MISS',
  FALSE_ALARM: 'FALSE_ALARM',
  CORRECT_REJECTION: 'CORRECT_REJECTION',
};

export const DEFAULT_DUAL_N_BACK_CONFIG = {
  initialN: 2,
  minN: 1,
  maxN: 5,
  blockSize: 20,
  totalBlocks: 3,
  stimulusDuration: 500,
  interStimulusInterval: 2000,
  matchProbability: 0.3,
  consecutiveForcedMatchProbability: 0.08,
  letters: ['B', 'F', 'K', 'M', 'Q', 'R', 'T', 'X'],
  positions: [0, 1, 2, 3, 4, 5, 6, 7, 8],
};

export const createInitialEngineState = (config = DEFAULT_DUAL_N_BACK_CONFIG) => ({
  config,
  nLevel: config.initialN,
  history: [],
  currentBlock: 1,
});

const randomItem = (items) => items[Math.floor(Math.random() * items.length)];

const chooseDifferent = (items, disallowed) => {
  const candidates = items.filter((item) => item !== disallowed);
  return randomItem(candidates.length ? candidates : items);
};

const shouldForceMatch = ({ history, index, nLevel, stream, config }) => {
  if (index < nLevel) {
    return false;
  }

  const previousTrial = history[index - 1];
  const previousWasForced = previousTrial?.forcedMatches?.[stream] === true;
  const probability = previousWasForced
    ? config.consecutiveForcedMatchProbability
    : config.matchProbability;

  return Math.random() < probability;
};

const buildStreamStimulus = ({ history, index, nLevel, stream, items, forceMatch }) => {
  const comparisonTrial = history[index - nLevel];
  const comparisonValue = comparisonTrial?.[stream];

  if (forceMatch && comparisonTrial) {
    return comparisonValue;
  }

  if (!comparisonTrial) {
    return randomItem(items);
  }

  return chooseDifferent(items, comparisonValue);
};

export const generateTrial = (engineState) => {
  const { config, history, nLevel } = engineState;
  const index = history.length;
  const forcedPositionMatch = shouldForceMatch({
    history,
    index,
    nLevel,
    stream: 'position',
    config,
  });
  const forcedLetterMatch = shouldForceMatch({
    history,
    index,
    nLevel,
    stream: 'letter',
    config,
  });

  const position = buildStreamStimulus({
    history,
    index,
    nLevel,
    stream: 'position',
    items: config.positions,
    forceMatch: forcedPositionMatch,
  });
  const letter = buildStreamStimulus({
    history,
    index,
    nLevel,
    stream: 'letter',
    items: config.letters,
    forceMatch: forcedLetterMatch,
  });

  const comparisonTrial = history[index - nLevel];

  return {
    trialIndex: index + 1,
    nLevel,
    position,
    letter,
    expectedPositionMatch: comparisonTrial ? position === comparisonTrial.position : false,
    expectedLetterMatch: comparisonTrial ? letter === comparisonTrial.letter : false,
    forcedMatches: {
      position: forcedPositionMatch,
      letter: forcedLetterMatch,
    },
  };
};

export const appendTrialToHistory = (engineState, trial) => ({
  ...engineState,
  history: [...engineState.history, trial],
});

const classifyStream = (expectedMatch, pressed) => {
  if (expectedMatch && pressed) {
    return OUTCOMES.HIT;
  }
  if (expectedMatch && !pressed) {
    return OUTCOMES.MISS;
  }
  if (!expectedMatch && pressed) {
    return OUTCOMES.FALSE_ALARM;
  }
  return OUTCOMES.CORRECT_REJECTION;
};

export const evaluateTrial = ({ trial, responses }) => {
  const userPressedPosition = Boolean(responses.position);
  const userPressedLetter = Boolean(responses.letter);
  const positionOutcome = classifyStream(trial.expectedPositionMatch, userPressedPosition);
  const letterOutcome = classifyStream(trial.expectedLetterMatch, userPressedLetter);

  return {
    ...trial,
    userPressedPosition,
    userPressedLetter,
    positionOutcome,
    letterOutcome,
    reactionTimePosition: responses.position?.reactionTime ?? null,
    reactionTimeLetter: responses.letter?.reactionTime ?? null,
    positionInputValid: responses.position?.valid ?? false,
    letterInputValid: responses.letter?.valid ?? false,
    correct: [OUTCOMES.HIT, OUTCOMES.CORRECT_REJECTION].includes(positionOutcome)
      && [OUTCOMES.HIT, OUTCOMES.CORRECT_REJECTION].includes(letterOutcome),
  };
};

export const computeBlockMetrics = (evaluatedTrials) => {
  const outcomes = evaluatedTrials.flatMap((trial) => [trial.positionOutcome, trial.letterOutcome]);
  const correctCount = outcomes.filter((outcome) => outcome === OUTCOMES.HIT || outcome === OUTCOMES.CORRECT_REJECTION).length;
  const falseAlarms = outcomes.filter((outcome) => outcome === OUTCOMES.FALSE_ALARM).length;
  const nonMatchCount = evaluatedTrials.reduce((count, trial) => (
    count
      + (trial.expectedPositionMatch ? 0 : 1)
      + (trial.expectedLetterMatch ? 0 : 1)
  ), 0);
  const reactionTimes = evaluatedTrials.flatMap((trial) => [
    trial.reactionTimePosition,
    trial.reactionTimeLetter,
  ]).filter((value) => Number.isFinite(value));

  return {
    accuracy: outcomes.length ? correctCount / outcomes.length : 0,
    falseAlarmRate: nonMatchCount ? falseAlarms / nonMatchCount : 0,
    averageReactionTime: reactionTimes.length
      ? Math.round(reactionTimes.reduce((sum, value) => sum + value, 0) / reactionTimes.length)
      : 0,
  };
};

export const adaptNLevel = (nLevel, metrics, config = DEFAULT_DUAL_N_BACK_CONFIG) => {
  if (metrics.accuracy >= 0.85 && metrics.falseAlarmRate <= 0.1) {
    return Math.min(config.maxN, nLevel + 1);
  }

  if (metrics.accuracy <= 0.6) {
    return Math.max(config.minN, nLevel - 1);
  }

  return nLevel;
};

export const advanceBlock = (engineState, evaluatedBlockTrials) => {
  const metrics = computeBlockMetrics(evaluatedBlockTrials);
  const nextN = adaptNLevel(engineState.nLevel, metrics, engineState.config);

  return {
    nextState: {
      ...engineState,
      nLevel: nextN,
      currentBlock: engineState.currentBlock + 1,
    },
    metrics: {
      ...metrics,
      previousN: engineState.nLevel,
      nextN,
    },
  };
};

export const buildTrialPayload = ({ evaluatedTrial, stimulusStartEpoch, timestampEpoch }) => ({
  trialIndex: evaluatedTrial.trialIndex,
  nLevel: evaluatedTrial.nLevel,
  position: evaluatedTrial.position,
  letter: evaluatedTrial.letter,
  expectedPositionMatch: evaluatedTrial.expectedPositionMatch,
  expectedLetterMatch: evaluatedTrial.expectedLetterMatch,
  userPressedPosition: evaluatedTrial.userPressedPosition,
  userPressedLetter: evaluatedTrial.userPressedLetter,
  positionOutcome: evaluatedTrial.positionOutcome,
  letterOutcome: evaluatedTrial.letterOutcome,
  reactionTimePosition: evaluatedTrial.reactionTimePosition,
  reactionTimeLetter: evaluatedTrial.reactionTimeLetter,
  stimulusStartTime: new Date(stimulusStartEpoch).toISOString(),
  timestamp: new Date(timestampEpoch).toISOString(),
  stimulus: `position:${evaluatedTrial.position},letter:${evaluatedTrial.letter}`,
  response: `position:${evaluatedTrial.userPressedPosition},letter:${evaluatedTrial.userPressedLetter}`,
  reactionTime: Math.round(
    [evaluatedTrial.reactionTimePosition, evaluatedTrial.reactionTimeLetter]
      .filter((value) => Number.isFinite(value))
      .reduce((sum, value, _, values) => sum + value / values.length, 0),
  ),
  correct: evaluatedTrial.correct,
});

export const computeSessionSummary = (trials) => {
  const metrics = computeBlockMetrics(trials);
  const maxNReached = trials.reduce((max, trial) => Math.max(max, trial.nLevel), 0);

  return {
    accuracy: Math.round(metrics.accuracy * 1000) / 10,
    falseAlarmRate: Math.round(metrics.falseAlarmRate * 1000) / 10,
    averageReactionTime: metrics.averageReactionTime,
    maxNReached,
  };
};
