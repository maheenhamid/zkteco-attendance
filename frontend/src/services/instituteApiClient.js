import axios from 'axios';

// Public Shebashikkha API - no auth token, no backend proxy. Separate axios
// instance from `api.js` on purpose: that one attaches our own app's JWT
// Bearer token to every request, which must never be sent to a third-party API.
const baseURL = import.meta.env.VITE_INSTITUTE_API_BASE_URL || 'https://api.shebashikkha.com/public';

const instituteApiClient = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    Accept: 'application/json',
    // "User-Agent" is intentionally not set here: it's a forbidden header name
    // in browsers (Fetch/XHR spec) - axios/fetch silently drop it client-side,
    // so it cannot be used to work around 403s from this layer. If the public
    // API is blocking requests by User-Agent or bot detection, that can only
    // be resolved on the API's side, not from frontend code.
  },
});

export class InstituteApiError extends Error {
  constructor(message, { status = null, isNetworkError = false } = {}) {
    super(message);
    this.name = 'InstituteApiError';
    this.status = status;
    this.isNetworkError = isNetworkError;
  }
}

export function normalizeInstituteApiError(error, context) {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 403) {
        return new InstituteApiError(
          `${context}: request was forbidden (403) by the public API.`,
          { status }
        );
      }
      return new InstituteApiError(
        data?.message || `${context}: request failed with status ${status}.`,
        { status }
      );
    }
    if (error.request) {
      return new InstituteApiError(
        `${context}: no response received - check your network connection or the API's CORS configuration.`,
        { isNetworkError: true }
      );
    }
  }
  return new InstituteApiError(error.message || `${context}: unexpected error.`);
}

export default instituteApiClient;
