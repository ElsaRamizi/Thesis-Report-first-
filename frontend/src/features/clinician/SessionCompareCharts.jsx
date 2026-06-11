import ResultsCharts from '../results/ResultsCharts';

export default function SessionCompareCharts({ sessionA, sessionB, labelA, labelB }) {
  return (
    <div className="compare-grid">
      <div className="compare-panel">
        <h3>{labelA}</h3>
        <ResultsCharts result={sessionA} />
      </div>
      <div className="compare-panel">
        <h3>{labelB}</h3>
        <ResultsCharts result={sessionB} />
      </div>
    </div>
  );
}
