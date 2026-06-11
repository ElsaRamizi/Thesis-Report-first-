const metricDefinitions = [
  { key: 'avgReactionTime', label: 'Avg Reaction Time', suffix: 'ms', lowerIsBetter: true },
  { key: 'medianReactionTime', label: 'Median Reaction Time', suffix: 'ms', lowerIsBetter: true },
  { key: 'accuracy', label: 'Accuracy', suffix: '%', lowerIsBetter: false },
  { key: 'errorRate', label: 'Error Rate', suffix: '%', lowerIsBetter: true },
  { key: 'falseAlarmRate', label: 'False Alarm Rate', suffix: '%', lowerIsBetter: true },
  { key: 'missRate', label: 'Miss Rate', suffix: '%', lowerIsBetter: true },
  { key: 'maxNReached', label: 'Max N Reached', suffix: '', lowerIsBetter: false },
  { key: 'improvementRate', label: 'Improvement Rate', suffix: '%', lowerIsBetter: false },
  { key: 'responseVariability', label: 'Response Variability', suffix: 'ms', lowerIsBetter: true },
];

const performanceState = (value, lowerIsBetter) => {
  if (value == null) {
    return 'neutral';
  }
  if (lowerIsBetter ? value <= 35 : value >= 75) {
    return 'good';
  }
  if (lowerIsBetter ? value <= 60 : value >= 55) {
    return 'steady';
  }
  return 'watch';
};

const comparisonKeyMap = {
  avgReactionTime: 'reactionTimeDeltaPercent',
  medianReactionTime: 'reactionTimeDeltaPercent',
  accuracy: 'accuracyDeltaPercent',
  errorRate: 'errorRateDeltaPercent',
  missRate: 'missRateDeltaPercent',
};

export default function CognitiveMetricsCards({ title, metrics, comparison }) {
  if (!metrics) {
    return null;
  }

  return (
    <section>
      {title ? <h3 className="analytics-section-title">{title}</h3> : null}
      <div className="dashboard-grid results-metrics analytics-metrics-grid">
        {metricDefinitions.map((definition) => {
          const value = metrics[definition.key];
          const deltaKey = comparisonKeyMap[definition.key];
          const delta = comparison && deltaKey ? comparison[deltaKey] : null;
          const state = performanceState(
            definition.key.includes('Rate') || definition.key === 'accuracy' ? value : null,
            definition.lowerIsBetter,
          );

          return (
            <article key={definition.key} className={`analytics-metric-card analytics-state-${state}`}>
              <div className="analytics-metric-icon">{definition.label.slice(0, 1)}</div>
              <span className="analytics-metric-label">{definition.label}</span>
              <strong className="metric-value">
                {value ?? '-'}
                {value != null && definition.suffix ? definition.suffix : ''}
              </strong>
              {delta != null ? (
                <span className={`analytics-trend ${delta >= 0 ? 'trend-up' : 'trend-down'}`}>
                  {delta >= 0 ? '+' : ''}{delta}%
                </span>
              ) : null}
            </article>
          );
        })}
      </div>
    </section>
  );
}

export { metricDefinitions };
