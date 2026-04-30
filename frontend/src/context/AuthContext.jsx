import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../services/authService';
import { registerUnauthorizedHandler } from '../services/apiClient';
import { decodeJwt, isTokenExpired } from '../utils/jwt';
import { STORAGE_KEYS } from '../utils/storage';
import AuthContext from './authContextValue';

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [token, setToken] = useState(() => {
    const storedToken = localStorage.getItem(STORAGE_KEYS.token);
    return storedToken && !isTokenExpired(storedToken) ? storedToken : null;
  });
  const [role, setRole] = useState(() => {
    const storedToken = localStorage.getItem(STORAGE_KEYS.token);
    if (!storedToken || isTokenExpired(storedToken)) {
      localStorage.removeItem(STORAGE_KEYS.token);
      localStorage.removeItem(STORAGE_KEYS.role);
      return null;
    }

    return localStorage.getItem(STORAGE_KEYS.role);
  });

  const logout = useCallback((redirectToLogin = false) => {
    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.role);
    setToken(null);
    setRole(null);

    if (redirectToLogin) {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      logout(true);
    });
  }, [logout]);

  useEffect(() => {
    if (!token) {
      return undefined;
    }

    const payload = decodeJwt(token);
    const timeoutMs = payload?.exp ? Math.max(0, payload.exp * 1000 - Date.now()) : 0;
    const timeoutId = window.setTimeout(() => logout(true), timeoutMs);
    return () => window.clearTimeout(timeoutId);
  }, [logout, token]);

  const persistAuth = useCallback((nextToken, nextRole) => {
    localStorage.setItem(STORAGE_KEYS.token, nextToken);
    localStorage.setItem(STORAGE_KEYS.role, nextRole);
    setToken(nextToken);
    setRole(nextRole);
  }, []);

  const login = useCallback(async (credentials) => {
    const data = await loginUser(credentials);
    persistAuth(data.token, data.role);
    return data;
  }, [persistAuth]);

  const value = useMemo(() => ({
    token,
    role,
    login,
    logout,
    isAuthenticated: Boolean(token),
    hasRole: (allowedRoles) => Boolean(role) && allowedRoles.includes(role),
  }), [login, logout, role, token]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
