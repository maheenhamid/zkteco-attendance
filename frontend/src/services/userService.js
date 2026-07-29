import api from './api';

export function fetchUsers(params) {
  return api.get('/api/users', { params }).then((res) => res.data);
}

export function createUser(payload) {
  return api.post('/api/users', payload).then((res) => res.data);
}

export function updateUser(id, payload) {
  return api.put(`/api/users/${id}`, payload).then((res) => res.data);
}

export function deleteUser(id) {
  return api.delete(`/api/users/${id}`);
}

export function bulkDeleteUsers(ids) {
  return api.post('/api/users/bulk-delete', { ids }).then((res) => res.data);
}

export function bulkDeleteUsersByFilter(instituteId, classId) {
  return api.post('/api/users/bulk-delete-by-filter', { instituteId, classId }).then((res) => res.data);
}

export function resendUser(id) {
  return api.post(`/api/users/${id}/resend`).then((res) => res.data);
}

export function resendUnsyncedUsers(params) {
  return api.post('/api/users/resend-unsynced', null, { params }).then((res) => res.data);
}

export function importUsersExcel(file, instituteId, deviceId, classId, className) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('instituteId', instituteId);
  formData.append('deviceId', deviceId);
  if (classId) {
    formData.append('classId', classId);
  }
  if (className) {
    formData.append('className', className);
  }
  return api
    .post('/api/users/import-excel', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data);
}
