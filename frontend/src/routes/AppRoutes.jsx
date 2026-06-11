import { Navigate, Route, Routes } from 'react-router-dom';
import { RESEARCH_TITLE_BY_ROLE } from '../config/navigation';
import { useAuth } from '../context/useAuth';
import AppLayout from '../layouts/AppLayout';
import ClinicianDashboardPage from '../pages/ClinicianDashboardPage';
import ClinicianGroupTrendsPage from '../pages/ClinicianGroupTrendsPage';
import ClinicianParticipantPage from '../pages/ClinicianParticipantPage';
import AutomatedReportPage from '../pages/AutomatedReportPage';
import SessionComparePage from '../pages/SessionComparePage';
import ParticipantProfilePage from '../pages/ParticipantProfilePage';
import CognitiveAnalyticsPage from '../pages/CognitiveAnalyticsPage';
import DataSharingPage from '../pages/DataSharingPage';
import LoginPage from '../pages/LoginPage';
import NotFoundPage from '../pages/NotFoundPage';
import RegisterPage from '../pages/RegisterPage';
import ResearchPage from '../pages/ResearchPage';
import ResultsPage from '../pages/ResultsPage';
import SessionCompletePage from '../pages/SessionCompletePage';
import SessionDetailPage from '../pages/SessionDetailPage';
import SessionHistoryPage from '../pages/SessionHistoryPage';
import SessionStartPage from '../pages/SessionStartPage';
import TaskPlayPage from '../pages/TaskPlayPage';
import TaskSelectionPage from '../pages/TaskSelectionPage';
import UnauthorizedPage from '../pages/UnauthorizedPage';
import UserDashboardPage from '../pages/UserDashboardPage';
import ProtectedRoute from './ProtectedRoute';

function HomeRedirect() {
  const { isAuthenticated, role } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return role === 'CLINICIAN'
    ? <Navigate to="/clinician/dashboard" replace />
    : <Navigate to="/user/dashboard" replace />;
}

function AppShell({ title, children, allowedRoles = ['USER', 'CLINICIAN'] }) {
  return (
    <ProtectedRoute allowedRoles={allowedRoles}>
      <AppLayout title={title}>{children}</AppLayout>
    </ProtectedRoute>
  );
}

function ResearchShell() {
  const { role } = useAuth();
  const title = RESEARCH_TITLE_BY_ROLE[role] ?? 'Research';

  return (
    <AppShell title={title}>
      <ResearchPage />
    </AppShell>
  );
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="/not-found" element={<NotFoundPage />} />

      <Route path="/user/dashboard" element={<AppShell title="Dashboard"><UserDashboardPage /></AppShell>} />
      <Route path="/research" element={<ResearchShell />} />
      <Route path="/data-sharing" element={<AppShell title="Doctor Connections" allowedRoles={['USER']}><DataSharingPage /></AppShell>} />
      <Route path="/profile" element={<AppShell title="Profile" allowedRoles={['USER']}><ParticipantProfilePage /></AppShell>} />
      <Route path="/clinician/dashboard" element={<AppShell title="Clinician Dashboard" allowedRoles={['CLINICIAN']}><ClinicianDashboardPage /></AppShell>} />
      <Route path="/clinician/group" element={<AppShell title="Group Trends" allowedRoles={['CLINICIAN']}><ClinicianGroupTrendsPage /></AppShell>} />
      <Route path="/clinician/participants/:participantId" element={<AppShell title="Participant" allowedRoles={['CLINICIAN']}><ClinicianParticipantPage /></AppShell>} />
      <Route path="/clinician/participants/:participantId/compare" element={<AppShell title="Compare Sessions" allowedRoles={['CLINICIAN']}><SessionComparePage /></AppShell>} />
      <Route path="/clinician/participants/:participantId/report" element={<AppShell title="Automated Report" allowedRoles={['CLINICIAN']}><AutomatedReportPage /></AppShell>} />
      <Route path="/clinician/analytics" element={<AppShell title="Cognitive Analytics" allowedRoles={['CLINICIAN']}><CognitiveAnalyticsPage /></AppShell>} />
      <Route path="/tasks" element={<AppShell title="Task Selection"><TaskSelectionPage /></AppShell>} />
      <Route path="/tasks/:taskId/start" element={<AppShell title="Session Setup"><SessionStartPage /></AppShell>} />
      <Route path="/tasks/:taskId/play" element={<AppShell title="Live Session"><TaskPlayPage /></AppShell>} />
      <Route path="/session/complete" element={<AppShell title="Session Complete"><SessionCompletePage /></AppShell>} />
      <Route path="/sessions" element={<AppShell title="Session History"><SessionHistoryPage /></AppShell>} />
      <Route path="/sessions/:sessionId" element={<AppShell title="Session Details"><SessionDetailPage /></AppShell>} />
      <Route path="/results/latest" element={<AppShell title="Results"><ResultsPage /></AppShell>} />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
