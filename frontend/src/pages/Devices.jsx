import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import Pagination from '../components/Pagination';
import SearchBar from '../components/SearchBar';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import PermissionGate from '../components/PermissionGate';
import { useInstitutes } from '../hooks/useInstitutes';
import { useDebounce } from '../hooks/useDebounce';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { createDevice, deleteDevice, fetchDevices, pullUsersFromDevice, updateDevice } from '../services/deviceService';
import { PERMISSIONS } from '../utils/permissions';
import { formatDateTime } from '../utils/dateFormat';

const EMPTY_FORM = { serialNumber: '', name: '', instituteId: '', ipAddress: '', port: '', location: '' };

export default function Devices() {
  const { operator } = useAuth();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const { data: institutes = [] } = useInstitutes();

  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [instituteFilter, setInstituteFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const debouncedSearch = useDebounce(search);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const params = {
    page,
    size: 10,
    search: debouncedSearch || undefined,
    instituteId: instituteFilter || undefined,
    status: statusFilter || undefined,
  };

  const { data, isLoading } = useQuery(['devices', params], () => fetchDevices(params), { keepPreviousData: true });

  const institutesById = Object.fromEntries(institutes.map((i) => [i.id, i.name]));

  const openCreate = () => {
    setEditing(null);
    setForm({ ...EMPTY_FORM, instituteId: operator?.superAdmin ? '' : operator?.instituteId });
    setModalOpen(true);
  };

  const openEdit = (device) => {
    setEditing(device);
    setForm({
      serialNumber: device.serialNumber,
      name: device.name,
      instituteId: device.instituteId ?? '',
      ipAddress: device.ipAddress || '',
      port: device.port || '',
      location: device.location || '',
    });
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = { ...form, port: form.port ? Number(form.port) : null, instituteId: Number(form.instituteId) };
      if (editing) {
        await updateDevice(editing.id, payload);
        showToast('Device updated', 'success');
      } else {
        await createDevice(payload);
        showToast('Device created', 'success');
      }
      setModalOpen(false);
      queryClient.invalidateQueries(['devices']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to save device', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteDevice(deleteTarget.id);
      showToast('Device deleted', 'success');
      setDeleteTarget(null);
      queryClient.invalidateQueries(['devices']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete device', 'error');
    }
  };

  const handlePullUsers = async (device) => {
    try {
      await pullUsersFromDevice(device.id);
      showToast(`Pull request queued for ${device.name} - applies on its next check-in`, 'success');
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to queue pull request', 'error');
    }
  };

  const columns = [
    { key: 'name', label: 'Name' },
    { key: 'serialNumber', label: 'Serial Number' },
    { key: 'institute', label: 'Institute', render: (row) => institutesById[row.instituteId] || row.instituteId || '-' },
    { key: 'ipAddress', label: 'IP Address', render: (row) => row.ipAddress || '-' },
    { key: 'status', label: 'Status', render: (row) => <StatusBadge status={row.status} /> },
    { key: 'lastHeartbeat', label: 'Last Heartbeat', render: (row) => formatDateTime(row.lastHeartbeat) },
    {
      key: 'actions',
      label: 'Actions',
      render: (row) => (
        <div className="flex flex-wrap gap-2">
          <PermissionGate permission={PERMISSIONS.DEVICE_EDIT}>
            <button onClick={() => openEdit(row)} className="text-primary-600 hover:underline">
              Edit
            </button>
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.USER_CREATE}>
            <button onClick={() => handlePullUsers(row)} className="text-blue-600 hover:underline">
              Pull Users
            </button>
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.DEVICE_DELETE}>
            <button onClick={() => setDeleteTarget(row)} className="text-red-600 hover:underline">
              Delete
            </button>
          </PermissionGate>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-800">Device Management</h1>
        <PermissionGate permission={PERMISSIONS.DEVICE_CREATE}>
          <button onClick={openCreate} className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700">
            + Add Device
          </button>
        </PermissionGate>
      </div>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <SearchBar value={search} onChange={(v) => { setSearch(v); setPage(0); }} placeholder="Search name or serial..." />
        {operator?.superAdmin && (
          <select
            value={instituteFilter}
            onChange={(e) => { setInstituteFilter(e.target.value); setPage(0); }}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="">All Institutes</option>
            {institutes.map((i) => (
              <option key={i.id} value={i.id}>{i.name}</option>
            ))}
          </select>
        )}
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          <option value="">All Status</option>
          <option value="ONLINE">Online</option>
          <option value="OFFLINE">Offline</option>
        </select>
      </div>

      <DataTable columns={columns} rows={data?.content || []} loading={isLoading} />

      <div className="mt-4 flex justify-end">
        <Pagination page={page} totalPages={data?.totalPages || 0} onPageChange={setPage} />
      </div>

      <Modal
        open={modalOpen}
        title={editing ? 'Edit Device' : 'Add Device'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="rounded-md border px-4 py-2 text-sm hover:bg-gray-50">
              Cancel
            </button>
            <button
              form="device-form"
              type="submit"
              disabled={saving}
              className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </>
        }
      >
        <form id="device-form" onSubmit={handleSave}>
          <FormField label="Serial Number" required>
            <input
              required
              value={form.serialNumber}
              onChange={(e) => setForm({ ...form, serialNumber: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Device Name" required>
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Institute" required>
            <select
              required
              disabled={!operator?.superAdmin}
              value={form.instituteId}
              onChange={(e) => setForm({ ...form, instituteId: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select institute</option>
              {institutes.map((i) => (
                <option key={i.id} value={i.id}>{i.name}</option>
              ))}
            </select>
          </FormField>
          <FormField label="IP Address">
            <input
              value={form.ipAddress}
              onChange={(e) => setForm({ ...form, ipAddress: e.target.value })}
              placeholder="192.168.1.115"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Port">
            <input
              type="number"
              value={form.port}
              onChange={(e) => setForm({ ...form, port: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Location">
            <input
              value={form.location}
              onChange={(e) => setForm({ ...form, location: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        message={`Delete device "${deleteTarget?.name}"? This cannot be undone.`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
