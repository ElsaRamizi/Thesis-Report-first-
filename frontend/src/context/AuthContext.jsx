import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchCurrentUser, loginUser, logoutUser } from '../services/authService';
import { registerUnauthorizedHandler } from '../services/apiClient';
import { STORAGE_KEYS } from '../utils/storage';
import AuthContext from './authContextValue';

// wraps the app — login state available everywhere via useAuth()
export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [role, setRole] = useState(() => localStorage.getItem(STORAGE_KEYS.role));
  const [email, setEmail] = useState(null);
  const [initializing, setInitializing] = useState(true);

  /// call backend logout + clear local state, optionally go to login page
  const logout = useCallback(async (redirectToLogin = false) => {
    try {
      await logoutUser();
    } catch {
      // network fail on logout is fine, still clear local stuff
    }
    localStorage.removeItem(STORAGE_KEYS.role);
    setRole(null);
    setEmail(null);

    if (redirectToLogin) {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  /// when apiClient gets 401 and refresh fails — auto logout
  useEffect(() => {
    registerUnauthorizedHandler(() => {
      logout(true);
    });
  }, [logout]);

  /// on first load — ask backend /me if cookie still good (page refresh case)
  useEffect(() => {
    let active = true;

    fetchCurrentUser()
      .then((user) => {
        if (!active) {
          return;
        }
        setRole(user.role);
        setEmail(user.email);
        localStorage.setItem(STORAGE_KEYS.role, user.role);
      })
      .catch(() => {
        if (active) {
          setRole(null);
          setEmail(null);
          localStorage.removeItem(STORAGE_KEYS.role);
        }
      })
      .finally(() => {
        if (active) {
          setInitializing(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  /// called from LoginPage — hits API then updates role in memory + localStorage
  const login = useCallback(async (credentials) => {
    const data = await loginUser(credentials);
    setRole(data.role);
    setEmail(data.email);
    localStorage.setItem(STORAGE_KEYS.role, data.role);
    return data;
  }, []);

  const value = useMemo(() => ({
    token: null, // JWT stays in httpOnly cookie, not here on purpose
    role,
    email,
    initializing,
    login,
    logout,
    isAuthenticated: Boolean(role),
    hasRole: (allowedRoles) => Boolean(role) && allowedRoles.includes(role),
  }), [email, initializing, login, logout, role]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
