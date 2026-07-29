import { useQuery } from '@tanstack/react-query';
import { fetchDashboardStats } from '../services/dashboardService';
import DataTable from '../components/DataTable';
import { formatDateTime } from '../utils/dateFormat';

const CARDS = [
  { key: 'totalDevices', label: 'Total Devices', icon: '🖥️', color: 'bg-blue-50 text-blue-700' },
  { key: 'onlineDevices', label: 'Online Devices', icon: '🟢', color: 'bg-emerald-50 text-emerald-700' },
  { key: 'offlineDevices', label: 'Offline Devices', icon: '⚪', color: 'bg-gray-100 text-gray-700' },
  { key: 'todayAttendance', label: "Today's Attendance", icon: '🗓️', color: 'bg-purple-50 text-purple-700' },
  { key: 'totalInstitutes', label: 'Total Institutes', icon: '🏫', color: 'bg-amber-50 text-amber-700' },
  { key: 'totalCardUsers', label: 'Total Card Users', icon: '🪪', color: 'bg-indigo-50 text-indigo-700' },
  { key: 'syncedUsers', label: 'Synced Users', icon: '✅', color: 'bg-emerald-50 text-emerald-700' },
  { key: 'unsyncedUsers', label: 'Unsynced Users', icon: '❌', color: 'bg-red-50 text-red-700' },
];

const PUNCH_COLUMNS = [
  { key: 'enrollNo', label: 'Enroll No' },
  { key: 'userFullName', label: 'Name', render: (r) => r.userFullName || '-' },
  { key: 'deviceName', label: 'Device' },
  { key: 'punchTime', label: 'Punch Time', render: (r) => formatDateTime(r.punchTime) },
];

export default function Dashboard() {
  const { data, isLoading } = useQuery(['dashboard-stats'], fetchDashboardStats, {
    refetchInterval: 30000,
  });

  return (
    <div>
      <h1 className="mb-6 text-xl font-semibold text-gray-800">Dashboard</h1>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {CARDS.map((card) => (
          <div key={card.key} className="rounded-xl border bg-white p-5 shadow-sm">
            <div className={`mb-3 inline-flex h-10 w-10 items-center justify-center rounded-lg text-lg ${card.color}`}>
              {card.icon}
            </div>
            <p className="text-2xl font-bold text-gray-800">
              {isLoading ? '—' : data?.[card.key] ?? 0}
            </p>
            <p className="text-sm text-gray-500">{card.label}</p>
          </div>
        ))}
      </div>

      <h2 className="mb-4 mt-10 text-lg font-semibold text-gray-800">Latest 15 Punch Logs</h2>
      <DataTable columns={PUNCH_COLUMNS} rows={data?.recentPunches || []} loading={isLoading} />
    </div>
  );
}
