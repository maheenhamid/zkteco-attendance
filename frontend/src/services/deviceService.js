import api from './api';

export function fetchDevices(params) {
  return api.get('/api/devices', { params }).then((res) => res.data);
}

export function createDevice(payload) {
  return api.post('/api/devices', payload).then((res) => res.data);
}

export function updateDevice(id, payload) {
  return api.put(`/api/devices/${id}`, payload).then((res) => res.data);
}

export function deleteDevice(id) {
  return api.delete(`/api/devices/${id}`);
}

export function pullUsersFromDevice(id) {
  return api.post(`/api/devices/${id}/pull-users`).then((res) => res.data);
}
