import api from './api';

export function fetchAttendance(params) {
  return api.get('/api/attendance', { params }).then((res) => res.data);
}

export function downloadAttendanceCsv(params) {
  return api
    .get('/api/attendance/export', { params, responseType: 'blob' })
    .then((res) => {
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'attendance-export.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    });
}
