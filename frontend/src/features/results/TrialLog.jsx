import {
  formatReactionTime,
  formatTrialResponse,
  formatTrialStimulus,
} from './trialFormatters';

export default function TrialLog({ trials, variant = 'default' }) {
  if (!trials?.length) {
    return <p>No trial data was saved for this session.</p>;
  }

  const rowClassName = variant === 'clinician' ? 'trial-row clinician-trial-row' : 'trial-row';

  return (
    <div className="trial-log">
      <div className={`${rowClassName} trial-log-header`}>
        <span>Trial</span>
        <span>Stimulus</span>
        <span>Response</span>
        <span>Reaction time</span>
        <span>Outcome</span>
      </div>
      {trials.map((trial) => (
        <div key={trial.id ?? trial.index} className={rowClassName}>
          <span>Trial {trial.index}</span>
          <span>{formatTrialStimulus(trial)}</span>
          <span>{formatTrialResponse(trial)}</span>
          <span>{formatReactionTime(trial)} ms</span>
          <span className={trial.correct ? 'result-good' : 'result-bad'}>
            {trial.correct ? 'Correct' : 'Error'}
          </span>
        </div>
      ))}
    </div>
  );
}
