import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  RadialLinearScale,
  Tooltip,
  BarElement,
} from 'chart.js';
import { Bar, Line, Radar } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  RadialLinearScale,
  BarElement,
  Tooltip,
  Legend,
);

const chartOptions = {
  responsive: true,
  plugins: { legend: { position: 'bottom' } },
  scales: { y: { beginAtZero: true } },
};

export function CognitiveTimelineCharts({ timeline = [], rollingTimeline = [] }) {
  const labels = timeline.map((point) => point.label);

  const reactionTimeData = {
    labels,
    datasets: [
      {
        label: 'Reaction Time',
        data: timeline.map((point) => point.avgReactionTime),
        borderColor: '#7d4f59',
        backgroundColor: 'rgba(125, 79, 89, 0.15)',
        tension: 0.35,
      },
      {
        label: 'Rolling Average',
        data: rollingTimeline.map((point) => point.avgReactionTime),
        borderColor: '#3f5650',
        borderDash: [6, 4],
        tension: 0.35,
      },
    ],
  };

  const accuracyData = {
    labels,
    datasets: [
      {
        label: 'Accuracy',
        data: timeline.map((point) => point.accuracy),
        borderColor: '#216c4a',
        backgroundColor: 'rgba(33, 108, 74, 0.12)',
        tension: 0.35,
      },
      {
        label: 'Error Rate',
        data: timeline.map((point) => point.errorRate),
        borderColor: '#b64d4d',
        backgroundColor: 'rgba(182, 77, 77, 0.12)',
        tension: 0.35,
      },
    ],
  };

  return (
    <div className="results-grid">
      <div className="chart-card">
        <h3>Reaction Time Over Time</h3>
        <Line data={reactionTimeData} options={chartOptions} />
      </div>
      <div className="chart-card">
        <h3>Accuracy & Error Trends</h3>
        <Line data={accuracyData} options={chartOptions} />
      </div>
    </div>
  );
}

export function CognitiveRadarChart({ profile }) {
  if (!profile) {
    return null;
  }

  const data = {
    labels: ['Memory', 'Reaction Speed', 'Attention', 'Consistency', 'Inhibition', 'Adaptability'],
    datasets: [
      {
        label: 'Cognitive Profile',
        data: [
          profile.memory,
          profile.reactionSpeed,
          profile.attention,
          profile.consistency,
          profile.inhibitionControl,
          profile.adaptability,
        ],
        backgroundColor: 'rgba(185, 124, 146, 0.25)',
        borderColor: '#7d4f59',
        pointBackgroundColor: '#3f5650',
      },
    ],
  };

  return (
    <div className="chart-card">
      <h3>Cognitive Profile</h3>
      <Radar
        data={data}
        options={{
          responsive: true,
          scales: { r: { beginAtZero: true, max: 100 } },
          plugins: { legend: { display: false } },
        }}
      />
    </div>
  );
}

export function CohortHistogramChart({ histogram = [] }) {
  const data = {
    labels: histogram.map((bucket) => bucket.label),
    datasets: [
      {
        label: 'Participants',
        data: histogram.map((bucket) => bucket.count),
        backgroundColor: 'rgba(63, 86, 80, 0.35)',
        borderColor: '#3f5650',
      },
    ],
  };

  return (
    <div className="chart-card">
      <h3>Reaction Time Distribution</h3>
      <Bar data={data} options={chartOptions} />
    </div>
  );
}

export function CohortTimelineChart({ timeline = [] }) {
  const data = {
    labels: timeline.map((point) => point.label),
    datasets: [
      {
        label: 'Cohort Avg Reaction Time',
        data: timeline.map((point) => point.avgReactionTime),
        borderColor: '#3f5650',
        backgroundColor: 'rgba(63, 86, 80, 0.15)',
        tension: 0.35,
        fill: true,
      },
    ],
  };

  return (
    <div className="chart-card">
      <h3>Cohort Average Timeline</h3>
      <Line data={data} options={chartOptions} />
    </div>
  );
}
