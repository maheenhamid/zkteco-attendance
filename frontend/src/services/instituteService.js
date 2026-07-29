import api from './api';

export function fetchInstitutes() {
  return api.get('/api/institutes').then((res) => res.data);
}

export function fetchClasses(instituteId) {
  return api.get(`/api/institutes/${instituteId}/classes`).then((res) => res.data);
}
