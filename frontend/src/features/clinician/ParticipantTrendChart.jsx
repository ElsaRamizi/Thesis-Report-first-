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

const formatLabel = (session) => {
  const date = session.startTime ? new Date(session.startTime).toLocaleDateString() : 'Session';
  return `${session.taskTitle ?? session.taskType ?? 'Task'} · ${date}`;
};

export default function ParticipantTrendChart({ sessions }) {
  if (!sessions || sessions.length < 2) {
    return null;
  }

  const ordered = [...sessions].sort(
    (left, right) => new Date(left.startTime).getTime() - new Date(right.startTime).getTime(),
  );

  const labels = ordered.map(formatLabel);
  const firstAccuracy = ordered[0]?.accuracy ?? 0;
  const lastAccuracy = ordered[ordered.length - 1]?.accuracy ?? 0;
  const trendDelta = Math.round((lastAccuracy - firstAccuracy) * 10) / 10;
  const trendLabel = trendDelta > 2 ? 'Improving' : trendDelta < -2 ? 'Declining' : 'Stable';

  const data = {
    labels,
    datasets: [
      {
        label: 'Accuracy (%)',
        data: ordered.map((session) => session.accuracy ?? 0),
        borderColor: trendDelta < -2 ? '#b64d4d' : trendDelta > 2 ? '#216c4a' : '#1f5f7a',
        backgroundColor: 'rgba(31, 95, 122, 0.12)',
        tension: 0.35,
        fill: true,
      },
      {
        label: 'Avg RT (ms)',
        data: ordered.map((session) => session.avgReactionTime ?? 0),
        borderColor: '#c27b2a',
        backgroundColor: 'rgba(194, 123, 42, 0.08)',
        tension: 0.35,
        yAxisID: 'y1',
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
      title: {
        display: true,
        text: `Performance trend: ${trendLabel} (${trendDelta >= 0 ? '+' : ''}${trendDelta}% accuracy)`,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: { display: true, text: 'Accuracy %' },
      },
      y1: {
        beginAtZero: true,
        position: 'right',
        grid: { drawOnChartArea: false },
        title: { display: true, text: 'RT ms' },
      },
    },
  };

  return (
    <div className="chart-card">
      <Line data={data} options={options} />
    </div>
  );
}
