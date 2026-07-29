import api from './api';

export function fetchRoles() {
  return api.get('/api/roles').then((res) => res.data);
}

export function fetchPermissions() {
  return api.get('/api/roles/permissions').then((res) => res.data);
}

export function createRole(payload) {
  return api.post('/api/roles', payload).then((res) => res.data);
}

export function updateRole(id, payload) {
  return api.put(`/api/roles/${id}`, payload).then((res) => res.data);
}

export function deleteRole(id) {
  return api.delete(`/api/roles/${id}`);
}

export function fetchOperators() {
  return api.get('/api/operators').then((res) => res.data);
}

export function createOperator(payload) {
  return api.post('/api/operators', payload).then((res) => res.data);
}

export function updateOperator(id, payload) {
  return api.put(`/api/operators/${id}`, payload).then((res) => res.data);
}

export function assignOperatorRoles(id, roleIds) {
  return api.put(`/api/operators/${id}/roles`, { roleIds }).then((res) => res.data);
}

export function deleteOperator(id) {
  return api.delete(`/api/operators/${id}`);
}
