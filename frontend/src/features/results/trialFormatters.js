export function formatTrialStimulus(trial) {
  if (trial.letter) {
    return `N${trial.nLevel} | ${trial.letter} @ ${(trial.position ?? 0) + 1}`;
  }

  if (typeof trial.stimulus === 'string' && trial.stimulus.includes('|')) {
    const [word, ink, congruency] = trial.stimulus.split('|');
    return `${word} (${ink} ink, ${congruency})`;
  }

  if (typeof trial.stimulus === 'string' && trial.stimulus.includes('-')) {
    return trial.stimulus.replace(/-/g, ' - ');
  }

  return trial.stimulus ?? '—';
}

export function formatTrialResponse(trial) {
  if (trial.positionOutcome) {
    return `${trial.positionOutcome} / ${trial.letterOutcome}`;
  }

  if (typeof trial.response === 'string' && trial.response.includes('-')) {
    return trial.response.replace(/-/g, ' - ');
  }

  return trial.response ?? '—';
}

export function formatReactionTime(trial) {
  return trial.reactionTime ?? trial.reactionTimePosition ?? trial.reactionTimeLetter ?? 0;
}
