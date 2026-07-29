import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Pagination from '../components/Pagination';
import StatusBadge from '../components/StatusBadge';
import { fetchCommands } from '../services/commandService';
import { formatDateTime } from '../utils/dateFormat';

export default function CommandMonitor() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');

  const params = { page, size: 20, status: statusFilter || undefined };

  const { data, isLoading } = useQuery(['commands', params], () => fetchCommands(params), {
    keepPreviousData: true,
    refetchInterval: 10000,
  });

  const columns = [
    { key: 'deviceName', label: 'Device' },
    { key: 'commandType', label: 'Command Type' },
    { key: 'commandText', label: 'Command', render: (r) => <span className="font-mono text-xs">{r.commandText}</span> },
    { key: 'status', label: 'Status', render: (r) => <StatusBadge status={r.status} /> },
    { key: 'createdAt', label: 'Created', render: (r) => formatDateTime(r.createdAt) },
    { key: 'sentAt', label: 'Sent', render: (r) => formatDateTime(r.sentAt) },
    { key: 'executedAt', label: 'Executed', render: (r) => formatDateTime(r.executedAt) },
  ];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-800">Command Monitor</h1>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          <option value="">All Status</option>
          <option value="PENDING">Pending</option>
          <option value="SENT">Sent</option>
          <option value="EXECUTED">Executed</option>
          <option value="FAILED">Failed</option>
        </select>
      </div>

      <DataTable columns={columns} rows={data?.content || []} loading={isLoading} />

      <div className="mt-4 flex justify-end">
        <Pagination page={page} totalPages={data?.totalPages || 0} onPageChange={setPage} />
      </div>
    </div>
  );
}
