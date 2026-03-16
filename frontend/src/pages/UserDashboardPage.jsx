import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import { getSessionOverview } from '../services/sessionService';
import { getUserProfile } from '../services/userService';

export default function UserDashboardPage() {
  const [profileStatus, setProfileStatus] = useState({ loading: true, error: '', text: '' });
  const [sessionStatus, setSessionStatus] = useState({ loading: true, error: '', text: '' });

  useEffect(() => {
    let active = true;

    getUserProfile()
      .then((data) => {
        if (active) {
          setProfileStatus({ loading: false, error: '', text: data });
        }
      })
      .catch((error) => {
        if (active) {
          setProfileStatus({
            loading: false,
            error: error.response?.data?.message ?? 'Profile endpoint connected, but data could not be loaded.',
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
      <Card title="Welcome" accent="warm" footer="Protected user endpoint connected.">
        {profileStatus.loading ? <LoadingState label="Loading your protected profile..." /> : null}
        {!profileStatus.loading && profileStatus.error ? (
          <StatusMessage tone="error" title="Profile unavailable" message={profileStatus.error} />
        ) : null}
        {!profileStatus.loading && !profileStatus.error ? <p>{profileStatus.text}</p> : null}
      </Card>
      <Card title="Task Hub" footer={<Link className="inline-link" to="/tasks">Open task selection</Link>}>
        {sessionStatus.loading ? <LoadingState label="Checking session service..." /> : null}
        {!sessionStatus.loading && sessionStatus.error ? (
          <StatusMessage tone="warning" title="Session service" message={sessionStatus.error} />
        ) : null}
        {!sessionStatus.loading && !sessionStatus.error ? <p>{sessionStatus.text}</p> : null}
      </Card>
      <Card title="Progress Snapshot" footer={<Link className="inline-link" to="/results/latest">Open latest results</Link>}>
        <StatusMessage tone="neutral" message="Reaction time, accuracy, and error-rate visual summaries are now ready for chart-based reporting." />
      </Card>
      <Card title="Settings">
        <StatusMessage tone="neutral" message="Profile and account settings can be added without changing the layout foundation." />
      </Card>
    </div>
  );
}
