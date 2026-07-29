const STYLES = {
  ONLINE: 'bg-emerald-100 text-emerald-700',
  ACTIVE: 'bg-emerald-100 text-emerald-700',
  SYNCED: 'bg-emerald-100 text-emerald-700',
  EXECUTED: 'bg-emerald-100 text-emerald-700',
  OFFLINE: 'bg-gray-200 text-gray-700',
  INACTIVE: 'bg-gray-200 text-gray-700',
  PENDING: 'bg-amber-100 text-amber-700',
  SENT: 'bg-blue-100 text-blue-700',
  FAILED: 'bg-red-100 text-red-700',
};

export default function StatusBadge({ status }) {
  const style = STYLES[status] || 'bg-gray-100 text-gray-600';
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${style}`}>
      {status || 'UNKNOWN'}
    </span>
  );
}
