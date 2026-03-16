import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { fetchTasks } from '../services/taskService';

export default function TaskSelectionPage() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    fetchTasks()
      .then((data) => {
        if (active) {
          setTasks(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Tasks could not be loaded.');
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="stack-lg">
      <section className="hero-panel">
        <p className="eyebrow">Task Selection</p>
        <h2>Choose a cognitive activity</h2>
        <p>Each task is short, research-inspired, and designed to capture reaction time, accuracy, and error patterns.</p>
      </section>

      {loading ? <LoadingState label="Loading task catalog..." /> : null}
      {error ? <StatusMessage tone="error" title="Task catalog unavailable" message={error} /> : null}

      <div className="dashboard-grid">
        {tasks.map((task) => (
          <Card
            key={task.id}
            title={task.title}
            accent="warm"
            footer={<Link className="inline-link" to={`/tasks/${task.id}/start`}>Prepare session</Link>}
          >
            <p>{task.description}</p>
            <div className="chip-row">
              <span className="chip">{task.durationMinutes} min</span>
              <span className="chip">{task.difficulty}</span>
            </div>
            <ul className="metric-list">
              {task.metricFocus.map((metric) => <li key={metric}>{metric}</li>)}
            </ul>
          </Card>
        ))}
      </div>
    </div>
  );
}
