import {
  ArcElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from 'chart.js';
import { Doughnut, Line } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend);

export default function ResultsCharts({ result }) {
  const labels = result.trials.map((trial) => `Trial ${trial.index}`);

  const reactionTimeData = {
    labels,
    datasets: [
      {
        label: 'Reaction Time (ms)',
        data: result.trials.map((trial) => trial.reactionTime ?? trial.reactionTimePosition ?? trial.reactionTimeLetter ?? 0),
        borderColor: '#1f5f7a',
        backgroundColor: 'rgba(31, 95, 122, 0.18)',
        tension: 0.35,
        fill: true,
      },
    ],
  };

  const accuracyData = {
    labels: result.falseAlarmRate != null ? ['Accuracy', 'False alarms'] : ['Accurate', 'Errors'],
    datasets: [
      {
        data: result.falseAlarmRate != null
          ? [result.accuracy, Math.round(result.falseAlarmRate * 1000) / 10]
          : [result.accuracy, result.errorRate],
        backgroundColor: ['#216c4a', '#b64d4d'],
        borderWidth: 0,
      },
    ],
  };

  const lineOptions = {
    responsive: true,
    plugins: {
      legend: { display: false },
    },
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  };

  const doughnutOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'bottom',
      },
    },
    cutout: '68%',
  };

  return (
    <div className="results-grid">
      <div className="chart-card">
        <h3>Reaction Time Trend</h3>
        <Line data={reactionTimeData} options={lineOptions} />
      </div>
      <div className="chart-card">
        <h3>Accuracy Breakdown</h3>
        <Doughnut data={accuracyData} options={doughnutOptions} />
      </div>
    </div>
  );
}
