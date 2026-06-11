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
import { Line } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend);

const formatLabel = (session) => {
  const date = session.startTime ? new Date(session.startTime).toLocaleDateString() : 'Session';
  return `${session.taskTitle ?? session.taskId} · ${date}`;
};

export default function DashboardTrendCharts({ sessions }) {
  if (!sessions || sessions.length === 0) {
    return null;
  }

  const ordered = [...sessions].sort(
    (left, right) => new Date(left.startTime).getTime() - new Date(right.startTime).getTime(),
  );
  const labels = ordered.map(formatLabel);

  const accuracyData = {
    labels,
    datasets: [
      {
        label: 'Accuracy (%)',
        data: ordered.map((session) => session.accuracy ?? 0),
        borderColor: '#216c4a',
        backgroundColor: 'rgba(33, 108, 74, 0.15)',
        tension: 0.3,
        fill: true,
      },
    ],
  };

  const reactionTimeData = {
    labels,
    datasets: [
      {
        label: 'Avg RT (ms)',
        data: ordered.map((session) => session.avgReactionTime ?? 0),
        borderColor: '#1f5f7a',
        backgroundColor: 'rgba(31, 95, 122, 0.15)',
        tension: 0.3,
        fill: true,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
    },
    scales: {
      y: { beginAtZero: true },
    },
  };

  return (
    <div className="results-grid">
      <div className="chart-card">
        <h3>Accuracy over sessions</h3>
        <Line data={accuracyData} options={options} />
      </div>
      <div className="chart-card">
        <h3>Reaction time over sessions</h3>
        <Line data={reactionTimeData} options={options} />
      </div>
    </div>
  );
}
