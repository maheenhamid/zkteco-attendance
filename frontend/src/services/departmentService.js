import api from './api';

export function fetchDepartments(instituteId) {
  if (!instituteId) return Promise.resolve([]);
  return api.get('/api/departments', { params: { instituteId } }).then((res) => res.data);
}

export function createDepartment(payload) {
  return api.post('/api/departments', payload).then((res) => res.data);
}

export function updateDepartment(id, payload) {
  return api.put(`/api/departments/${id}`, payload).then((res) => res.data);
}

export function deleteDepartment(id) {
  return api.delete(`/api/departments/${id}`);
}
