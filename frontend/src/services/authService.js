import api from './api';

export function login(username, password) {
  return api.post('/api/auth/login', { username, password }).then((res) => res.data);
}

export function fetchProfile() {
  return api.get('/api/auth/me').then((res) => res.data);
}
