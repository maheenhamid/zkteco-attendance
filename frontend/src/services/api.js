import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
});

const TOKEN_KEY = 'zkt_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let onUnauthorized = null;
let onError = null;

export function registerApiHandlers({ unauthorized, error }) {
  onUnauthorized = unauthorized;
  onError = error;
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      onUnauthorized && onUnauthorized();
    } else {
      const message = error.response?.data?.message || error.message || 'Something went wrong';
      onError && onError(message);
    }
    return Promise.reject(error);
  }
);

export default api;
