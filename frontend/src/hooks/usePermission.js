import { useAuth } from '../context/AuthContext';

export function usePermission(code) {
  const { hasPermission } = useAuth();
  return hasPermission(code);
}
