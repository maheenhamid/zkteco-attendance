import api from './api';

export function fetchDashboardStats() {
  return api.get('/api/dashboard/stats').then((res) => res.data);
}
