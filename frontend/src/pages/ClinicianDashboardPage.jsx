import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { getClinicianDashboard } from '../services/clinicianService';
import { getSessionOverview } from '../services/sessionService';

export default function ClinicianDashboardPage() {
  const [dashboardStatus, setDashboardStatus] = useState({ loading: true, error: '', text: '' });
  const [sessionStatus, setSessionStatus] = useState({ loading: true, error: '', text: '' });

  useEffect(() => {
    let active = true;

    getClinicianDashboard()
      .then((data) => {
        if (active) {
          setDashboardStatus({ loading: false, error: '', text: data });
        }
      })
      .catch((error) => {
        if (active) {
          setDashboardStatus({
            loading: false,
            error: error.response?.data?.message ?? 'Clinician endpoint connected, but dashboard data could not be loaded.',
            text: '',
          });
        }
      });

    getSessionOverview()
      .then((data) => {
        if (active) {
          setSessionStatus({ loading: false, error: '', text: data });
        }
      })
      .catch((error) => {
        if (active) {
          setSessionStatus({
            loading: false,
            error: error.response?.data?.message ?? 'Session service is not ready yet.',
            text: '',
          });
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="dashboard-grid">
      <Card title="Overview" accent="cool" footer="Protected clinician endpoint connected.">
        {dashboardStatus.loading ? <LoadingState label="Loading clinician workspace..." /> : null}
        {!dashboardStatus.loading && dashboardStatus.error ? (
          <StatusMessage tone="error" title="Dashboard unavailable" message={dashboardStatus.error} />
        ) : null}
        {!dashboardStatus.loading && !dashboardStatus.error ? <p>{dashboardStatus.text}</p> : null}
      </Card>
      <Card title="Participant Management">
        <StatusMessage tone="neutral" message="Participant lists and annotations will plug into this panel later." />
      </Card>
      <Card title="Reports" footer={<Link className="inline-link" to="/results/latest">Review latest session summary</Link>}>
        {sessionStatus.loading ? <LoadingState label="Checking shared session pipeline..." /> : null}
        {!sessionStatus.loading && sessionStatus.error ? (
          <StatusMessage tone="warning" title="Shared session service" message={sessionStatus.error} />
        ) : null}
        {!sessionStatus.loading && !sessionStatus.error ? <p>{sessionStatus.text}</p> : null}
      </Card>
      <Card title="Task Flow" footer={<Link className="inline-link" to="/tasks">Launch participant task flow</Link>}>
        <StatusMessage tone="neutral" message="A complete selection -> session -> results journey is now available for prototype testing." />
      </Card>
    </div>
  );
}
