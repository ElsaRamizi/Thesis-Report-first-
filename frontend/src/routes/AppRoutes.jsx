import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import AppLayout from '../layouts/AppLayout';
import ClinicianDashboardPage from '../pages/ClinicianDashboardPage';
import LoginPage from '../pages/LoginPage';
import NotFoundPage from '../pages/NotFoundPage';
import RegisterPage from '../pages/RegisterPage';
import ResultsPage from '../pages/ResultsPage';
import SessionCompletePage from '../pages/SessionCompletePage';
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

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="/not-found" element={<NotFoundPage />} />

      <Route path="/user/dashboard" element={<AppShell title="User Dashboard"><UserDashboardPage /></AppShell>} />
      <Route path="/clinician/dashboard" element={<AppShell title="Clinician Dashboard" allowedRoles={['CLINICIAN']}><ClinicianDashboardPage /></AppShell>} />
      <Route path="/tasks" element={<AppShell title="Task Selection"><TaskSelectionPage /></AppShell>} />
      <Route path="/tasks/:taskId/start" element={<AppShell title="Session Setup"><SessionStartPage /></AppShell>} />
      <Route path="/tasks/:taskId/play" element={<AppShell title="Live Session"><TaskPlayPage /></AppShell>} />
      <Route path="/session/complete" element={<AppShell title="Session Complete"><SessionCompletePage /></AppShell>} />
      <Route path="/sessions" element={<AppShell title="Session History"><SessionHistoryPage /></AppShell>} />
      <Route path="/results/latest" element={<AppShell title="Results"><ResultsPage /></AppShell>} />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
