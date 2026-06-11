import { Link } from 'react-router-dom';
import QuickActionCard from '../components/layout/QuickActionCard';
import Card from '../components/ui/Card';
import LoadingState from '../components/ui/LoadingState';
import StatusMessage from '../components/ui/StatusMessage';
import DashboardTrendCharts from '../features/results/DashboardTrendCharts';
import useAsyncData from '../hooks/useAsyncData';
import { fetchOwnProfile } from '../services/profileService';
import { fetchLatestSessionResult, fetchSessionHistory } from '../services/sessionService';

const formatDate = (value) => {
  if (!value) {
    return 'No sessions yet';
  }
  return new Date(value).toLocaleString();
};

export default function UserDashboardPage() {
  const profile = useAsyncData(() => fetchOwnProfile(), []);
  const history = useAsyncData(() => fetchSessionHistory(), []);
  const latest = useAsyncData(() => fetchLatestSessionResult().catch(() => null), []);

  const sessionCount = history.data?.length ?? 0;
  const latestSession = latest.data;

  return (
    <div className="stack-lg">
      <div className="dashboard-grid">
        <Card title="Welcome" accent="warm">
          {profile.loading ? <LoadingState label="Loading profile..." /> : null}
          {profile.error ? (
            <StatusMessage tone="warning" title="Profile unavailable" message={profile.error} />
          ) : null}
          {!profile.loading && !profile.error ? (
            <>
              <p className="metric-value dashboard-email">{profile.data?.email}</p>
              <p>
                {profile.data?.age != null ? `Age ${profile.data.age}` : 'Add your date of birth on the profile page.'}
              </p>
              <Link to="/profile" className="inline-link">Edit profile</Link>
            </>
          ) : null}
        </Card>

        <Card title="Latest Session" accent="cool">
          {latest.loading || history.loading ? <LoadingState label="Loading sessions..." /> : null}
          {latest.error && sessionCount === 0 ? (
            <StatusMessage tone="warning" title="No sessions yet" message="Complete a task to see your first results here." />
          ) : null}
          {!latest.loading && latestSession ? (
            <>
              <p className="eyebrow">{latestSession.taskTitle}</p>
              <p>{formatDate(latestSession.startTime)}</p>
              <div className="session-summary compact-summary">
                <div>
                  <strong>{latestSession.avgReactionTime} ms</strong>
                  <span>Avg RT</span>
                </div>
                <div>
                  <strong>{latestSession.accuracy}%</strong>
                  <span>Accuracy</span>
                </div>
                <div>
                  <strong>{sessionCount}</strong>
                  <span>Saved sessions</span>
                </div>
              </div>
              <Link to={`/sessions/${latestSession.sessionId}`} className="inline-link">
                View full trial log
              </Link>
            </>
          ) : null}
          {!latest.loading && !latestSession && sessionCount > 0 ? (
            <Link to="/sessions" className="inline-link">Browse session history</Link>
          ) : null}
        </Card>
      </div>

      {!history.loading && history.data?.length > 0 ? (
        <Card title="Performance trends" accent="warm">
          <DashboardTrendCharts sessions={history.data} />
        </Card>
      ) : null}

      <section>
        <h3 className="section-title">Quick actions</h3>
        <div className="dashboard-grid">
          <QuickActionCard
            title="Start a task"
            description="Play Stroop, memory span, or n-back."
            to="/tasks"
            linkLabel="Open tasks"
            accent="warm"
          />
          <QuickActionCard
            title="Session history"
            description="List of saved sessions with trial details."
            to="/sessions"
            linkLabel="Open history"
          />
          <QuickActionCard
            title="Join research"
            description="Questionnaires for published studies."
            to="/research"
            linkLabel="Open research"
          />
          <QuickActionCard
            title="Share with clinician"
            description="Let a clinician see your results (with consent)."
            to="/data-sharing"
            linkLabel="Open data sharing"
            accent="cool"
          />
        </div>
      </section>
    </div>
  );
}
