import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Pagination from '../components/Pagination';
import PermissionGate from '../components/PermissionGate';
import { useInstitutes } from '../hooks/useInstitutes';
import { useClasses } from '../hooks/useClasses';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { fetchDevices } from '../services/deviceService';
import { downloadAttendanceCsv, fetchAttendance } from '../services/attendanceService';
import { PERMISSIONS } from '../utils/permissions';
import { formatDateTime } from '../utils/dateFormat';

const EMPTY_FILTERS = { instituteId: '', classId: '', deviceId: '', enrollNo: '', fromDate: '', toDate: '' };

export default function Attendance() {
  const { operator } = useAuth();
  const { showToast } = useToast();
  const { data: institutes = [] } = useInstitutes();

  const [filters, setFilters] = useState({
    ...EMPTY_FILTERS,
    instituteId: operator?.superAdmin ? '' : operator?.instituteId,
  });
  const [page, setPage] = useState(0);
  const [exporting, setExporting] = useState(false);

  const { data: classes = [] } = useClasses(filters.instituteId);
  const { data: devicesPage } = useQuery(
    ['devices-for-attendance', filters.instituteId],
    () => fetchDevices({ instituteId: filters.instituteId, size: 100 }),
    { enabled: !!filters.instituteId }
  );

  const params = {
    ...filters,
    instituteId: filters.instituteId || undefined,
    classId: filters.classId || undefined,
    deviceId: filters.deviceId || undefined,
    enrollNo: filters.enrollNo || undefined,
    fromDate: filters.fromDate || undefined,
    toDate: filters.toDate || undefined,
    page,
    size: 20,
  };

  const { data, isLoading } = useQuery(['attendance', params], () => fetchAttendance(params), { keepPreviousData: true });

  const updateFilter = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      await downloadAttendanceCsv(params);
    } catch (err) {
      showToast('Failed to export attendance', 'error');
    } finally {
      setExporting(false);
    }
  };

  const columns = [
    { key: 'enrollNo', label: 'Enroll No' },
    { key: 'userFullName', label: 'Name', render: (r) => r.userFullName || '-' },
    { key: 'deviceName', label: 'Device' },
    { key: 'punchTime', label: 'Punch Time', render: (r) => formatDateTime(r.punchTime) },
    { key: 'punchType', label: 'Type', render: (r) => r.punchType ?? '-' },
    { key: 'verifyMode', label: 'Verify Mode', render: (r) => r.verifyMode ?? '-' },
  ];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-800">Attendance</h1>
        <PermissionGate permission={PERMISSIONS.ATTENDANCE_EXPORT}>
          <button
            onClick={handleExport}
            disabled={exporting}
            className="rounded-md border border-primary-600 px-4 py-2 text-sm font-medium text-primary-600 hover:bg-primary-50 disabled:opacity-60"
          >
            {exporting ? 'Exporting...' : 'Export CSV'}
          </button>
        </PermissionGate>
      </div>

      <div className="mb-4 grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-6">
        {operator?.superAdmin && (
          <select
            value={filters.instituteId}
            onChange={(e) => updateFilter('instituteId', e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="">All Institutes</option>
            {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
          </select>
        )}
        <select
          value={filters.classId}
          onChange={(e) => updateFilter('classId', e.target.value)}
          disabled={!filters.instituteId}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
        >
          <option value="">All Classes</option>
          {classes.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select
          value={filters.deviceId}
          onChange={(e) => updateFilter('deviceId', e.target.value)}
          disabled={!filters.instituteId}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
        >
          <option value="">All Devices</option>
          {(devicesPage?.content || []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <input
          placeholder="Enroll No"
          value={filters.enrollNo}
          onChange={(e) => updateFilter('enrollNo', e.target.value)}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
        <input
          type="date"
          value={filters.fromDate}
          onChange={(e) => updateFilter('fromDate', e.target.value)}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
        <input
          type="date"
          value={filters.toDate}
          onChange={(e) => updateFilter('toDate', e.target.value)}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <DataTable columns={columns} rows={data?.content || []} loading={isLoading} />

      <div className="mt-4 flex justify-end">
        <Pagination page={page} totalPages={data?.totalPages || 0} onPageChange={setPage} />
      </div>
    </div>
  );
}
