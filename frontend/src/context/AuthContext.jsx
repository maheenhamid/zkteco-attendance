import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { fetchProfile, login as loginRequest } from '../services/authService';
import { getToken, registerApiHandlers, setToken } from '../services/api';
import { useToast } from './ToastContext';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const { showToast } = useToast();
  const [operator, setOperator] = useState(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    registerApiHandlers({
      unauthorized: () => {
        setToken(null);
        setOperator(null);
      },
      error: (message) => showToast(message, 'error'),
    });
  }, [showToast]);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setInitializing(false);
      return;
    }
    fetchProfile()
      .then(setOperator)
      .catch(() => setToken(null))
      .finally(() => setInitializing(false));
  }, []);

  const login = async (username, password) => {
    const data = await loginRequest(username, password);
    setToken(data.token);
    setOperator(data.operator);
    return data.operator;
  };

  const logout = () => {
    setToken(null);
    setOperator(null);
  };

  const hasPermission = (code) => {
    if (!operator) return false;
    if (operator.superAdmin) return true;
    return operator.permissions?.includes(code) ?? false;
  };

  const value = useMemo(
    () => ({ operator, initializing, isAuthenticated: !!operator, login, logout, hasPermission }),
    [operator, initializing]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
