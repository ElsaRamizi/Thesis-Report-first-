import { Navigate, useLocation } from 'react-router-dom';
import LoadingState from '../components/ui/LoadingState';
import { useAuth } from '../context/useAuth';

// wraps pages that need login — UI guard only, real security is backend JwtFilter
export default function ProtectedRoute({ allowedRoles, children }) {
  const { isAuthenticated, hasRole, initializing } = useAuth();
  const location = useLocation();

  // still checking /api/auth/me on app load
  if (initializing) {
    return <LoadingState label="Checking session..." />;
  }

  // no role in context — send to login, remember where they wanted to go
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // logged in but wrong role (e.g. USER on clinician page)
  if (allowedRoles && !hasRole(allowedRoles)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
