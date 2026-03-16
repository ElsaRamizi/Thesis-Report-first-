import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../services/authService';
import { registerUnauthorizedHandler } from '../services/apiClient';
import { decodeJwt, isTokenExpired } from '../utils/jwt';
import { STORAGE_KEYS } from '../utils/storage';

const AuthContext = createContext(null);

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

  const logout = (redirectToLogin = false) => {
    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.role);
    setToken(null);
    setRole(null);

    if (redirectToLogin) {
      navigate('/login', { replace: true });
    }
  };

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      logout(true);
    });
  }, []);

  useEffect(() => {
    if (!token) {
      return undefined;
    }

    const payload = decodeJwt(token);
    if (!payload?.exp) {
      logout(true);
      return undefined;
    }

    const timeoutMs = payload.exp * 1000 - Date.now();
    if (timeoutMs <= 0) {
      logout(true);
      return undefined;
    }

    const timeoutId = window.setTimeout(() => logout(true), timeoutMs);
    return () => window.clearTimeout(timeoutId);
  }, [token]);

  const persistAuth = (nextToken, nextRole) => {
    localStorage.setItem(STORAGE_KEYS.token, nextToken);
    localStorage.setItem(STORAGE_KEYS.role, nextRole);
    setToken(nextToken);
    setRole(nextRole);
  };

  const login = async (credentials) => {
    const data = await loginUser(credentials);
    persistAuth(data.token, data.role);
    return data;
  };

  const value = useMemo(() => ({
    token,
    role,
    login,
    logout,
    isAuthenticated: Boolean(token),
    hasRole: (allowedRoles) => Boolean(role) && allowedRoles.includes(role),
  }), [token, role]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
}
