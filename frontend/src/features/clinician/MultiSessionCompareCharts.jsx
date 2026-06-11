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

export default function MultiSessionCompareCharts({ timeline }) {
  if (!timeline || timeline.length === 0) {
    return null;
  }

  const labels = timeline.map((point) => {
    const date = point.startTime ? new Date(point.startTime).toLocaleString() : `Session ${point.sessionId}`;
    return `${point.taskTitle ?? point.taskType} · ${date}`;
  });

  const data = {
    labels,
    datasets: [
      {
        label: 'Accuracy (%)',
        data: timeline.map((point) => point.accuracy ?? 0),
        borderColor: '#216c4a',
        backgroundColor: 'rgba(33, 108, 74, 0.15)',
        tension: 0.25,
      },
      {
        label: 'Avg RT (ms)',
        data: timeline.map((point) => point.avgReactionTime ?? 0),
        borderColor: '#1f5f7a',
        backgroundColor: 'rgba(31, 95, 122, 0.12)',
        tension: 0.25,
        yAxisID: 'y1',
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
    },
    scales: {
      y: { beginAtZero: true, title: { display: true, text: 'Accuracy %' } },
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
      <h3>Multi-session performance timeline</h3>
      <Line data={data} options={options} />
    </div>
  );
}
