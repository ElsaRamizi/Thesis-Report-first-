import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend);

export default function GroupTrendsCharts({ timeline }) {
  if (!timeline?.length) {
    return <p>No timeline data yet for the selected filter.</p>;
  }

  const labels = timeline.map((point) => point.label);
  const accuracyData = {
    labels,
    datasets: [
      {
        label: 'Group avg accuracy (%)',
        data: timeline.map((point) => point.avgAccuracy),
        borderColor: '#216c4a',
        backgroundColor: 'rgba(33, 108, 74, 0.15)',
        tension: 0.3,
        fill: true,
      },
    ],
  };

  const reactionData = {
    labels,
    datasets: [
      {
        label: 'Group avg RT (ms)',
        data: timeline.map((point) => point.avgReactionTime),
        borderColor: '#1f5f7a',
        backgroundColor: 'rgba(31, 95, 122, 0.15)',
        tension: 0.3,
        fill: true,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: { legend: { position: 'bottom' } },
    scales: { y: { beginAtZero: true } },
  };

  return (
    <div className="results-grid">
      <div className="chart-card">
        <h3>Group Accuracy Over Time</h3>
        <Line data={accuracyData} options={options} />
      </div>
      <div className="chart-card">
        <h3>Group Reaction Time Over Time</h3>
        <Line data={reactionData} options={options} />
      </div>
    </div>
  );
}
