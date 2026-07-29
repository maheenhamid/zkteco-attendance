import api from './api';

export function fetchCommands(params) {
  return api.get('/api/commands', { params }).then((res) => res.data);
}
