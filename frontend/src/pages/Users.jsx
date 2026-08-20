import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import Pagination from '../components/Pagination';
import SearchBar from '../components/SearchBar';
import FormField from '../components/FormField';
import PermissionGate from '../components/PermissionGate';
import { useInstitutes } from '../hooks/useInstitutes';
import { useDepartments } from '../hooks/useDepartments';
import { useDebounce } from '../hooks/useDebounce';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { fetchDevices } from '../services/deviceService';
import {
  bulkDeleteUsers, bulkDeleteUsersByFilter, createUser, deleteUser, downloadUsersExcel, fetchUsers,
  importUsersExcel, resendUnsyncedUsers, resendUser, updateUser,
} from '../services/userService';
import { PERMISSIONS } from '../utils/permissions';

const EMPTY_FORM = {
  instituteId: '', classId: '', className: '', fullName: '', cardNo: '',
  devicePrivilege: 'COMMON', deviceId: '',
};

const EMPTY_IMPORT = { instituteId: '', classId: '', deviceId: '', file: null };

export default function Users() {
  const { operator } = useAuth();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const { data: institutes = [] } = useInstitutes();

  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [instituteFilter, setInstituteFilter] = useState(operator?.superAdmin ? '' : operator?.instituteId);
  const [departmentFilter, setDepartmentFilter] = useState('');
  const debouncedSearch = useDebounce(search);

  const { data: filterDepartments = [] } = useDepartments(instituteFilter);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [selectedIds, setSelectedIds] = useState([]);
  const [bulkConfirmOpen, setBulkConfirmOpen] = useState(false);
  const [filterDeleteConfirmOpen, setFilterDeleteConfirmOpen] = useState(false);

  const [importModalOpen, setImportModalOpen] = useState(false);
  const [importForm, setImportForm] = useState(EMPTY_IMPORT);
  const [importing, setImporting] = useState(false);
  const [resendingAll, setResendingAll] = useState(false);
  const [exporting, setExporting] = useState(false);

  const { data: formDepartments = [] } = useDepartments(form.instituteId);
  const { data: formDevices = [], isError: formDevicesError } = useQuery(
    ['devices-for-user-form', form.instituteId],
    () => fetchDevices({ instituteId: form.instituteId, size: 100 }),
    { enabled: !!form.instituteId }
  );
  const { data: importDepartments = [] } = useDepartments(importForm.instituteId);
  const { data: importDevices = [] } = useQuery(
    ['devices-for-import', importForm.instituteId],
    () => fetchDevices({ instituteId: importForm.instituteId, size: 100 }),
    { enabled: !!importForm.instituteId }
  );

  const params = {
    page,
    size: 10,
    search: debouncedSearch || undefined,
    instituteId: instituteFilter || undefined,
    classId: departmentFilter || undefined,
  };

  const { data, isLoading } = useQuery(['device-users', params], () => fetchUsers(params), { keepPreviousData: true });

  const institutesById = Object.fromEntries(institutes.map((i) => [i.id, i.name]));

  const openCreate = () => {
    setEditing(null);
    setForm({ ...EMPTY_FORM, instituteId: operator?.superAdmin ? '' : operator?.instituteId });
    setModalOpen(true);
  };

  const openEdit = (user) => {
    setEditing(user);
    setForm({
      instituteId: user.instituteId,
      classId: user.classId || '',
      className: user.className || '',
      fullName: user.fullName,
      cardNo: user.cardNo || '',
      devicePrivilege: user.devicePrivilege,
      deviceId: user.deviceId,
    });
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const selectedDepartment = formDepartments.find((d) => String(d.id) === String(form.classId));
      const payload = {
        ...form,
        instituteId: Number(form.instituteId),
        classId: form.classId ? Number(form.classId) : null,
        className: selectedDepartment?.name || form.className || null,
        deviceId: Number(form.deviceId),
        // enrollNo (device PIN) isn't collected in this form: left blank on create so the
        // backend auto-assigns the next free PIN, and preserved as-is on edit.
        enrollNo: editing ? editing.enrollNo : undefined,
      };
      if (editing) {
        await updateUser(editing.id, payload);
        showToast('User updated - sync queued to device', 'success');
      } else {
        await createUser(payload);
        showToast('User created - sync queued to device', 'success');
      }
      setModalOpen(false);
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to save user', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteUser(deleteTarget.id);
      showToast('User deleted from app and queued for device delete', 'success');
      setDeleteTarget(null);
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete user', 'error');
    }
  };

  const handleBulkDelete = async () => {
    try {
      const res = await bulkDeleteUsers(selectedIds);
      showToast(`${res.deleted} user(s) deleted`, 'success');
      setSelectedIds([]);
      setBulkConfirmOpen(false);
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Bulk delete failed', 'error');
    }
  };

  const handleFilterDelete = async () => {
    try {
      const res = await bulkDeleteUsersByFilter(instituteFilter, departmentFilter || null);
      showToast(`${res.deleted} user(s) deleted`, 'success');
      setFilterDeleteConfirmOpen(false);
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Bulk delete failed', 'error');
    } finally {
      setFilterDeleteConfirmOpen(false);
    }
  };

  const handleResend = async (user) => {
    try {
      await resendUser(user.id);
      showToast(`Resend queued for ${user.fullName}`, 'success');
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to resend', 'error');
    }
  };

  const handleResendAllUnsynced = async () => {
    setResendingAll(true);
    try {
      const res = await resendUnsyncedUsers({
        instituteId: instituteFilter || undefined,
        classId: departmentFilter || undefined,
      });
      showToast(`${res.resent} unsynced user(s) queued for resend`, 'success');
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to resend unsynced users', 'error');
    } finally {
      setResendingAll(false);
    }
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      await downloadUsersExcel({
        instituteId: instituteFilter || undefined,
        classId: departmentFilter || undefined,
        search: debouncedSearch || undefined,
      });
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to export users', 'error');
    } finally {
      setExporting(false);
    }
  };

  const openImport = () => {
    setImportForm({ ...EMPTY_IMPORT, instituteId: operator?.superAdmin ? '' : operator?.instituteId });
    setImportModalOpen(true);
  };

  const handleImport = async (e) => {
    e.preventDefault();
    if (!importForm.file) {
      showToast('Please choose a .xlsx file', 'error');
      return;
    }
    setImporting(true);
    try {
      const selectedDepartment = importDepartments.find((d) => String(d.id) === String(importForm.classId));
      const result = await importUsersExcel(
        importForm.file,
        Number(importForm.instituteId),
        Number(importForm.deviceId),
        importForm.classId ? Number(importForm.classId) : undefined,
        selectedDepartment?.name
      );
      showToast(`Import finished: ${result.successCount} succeeded, ${result.errorCount} failed`, result.errorCount > 0 ? 'error' : 'success');
      if (result.errors?.length) {
        console.warn('Excel import errors:', result.errors);
      }
      setImportModalOpen(false);
      queryClient.invalidateQueries(['device-users']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Import failed', 'error');
    } finally {
      setImporting(false);
    }
  };

  const rows = data?.content || [];

  const columns = [
    { key: 'fullName', label: 'Full Name' },
    { key: 'enrollNo', label: 'Enroll No' },
    { key: 'institute', label: 'Institute', render: (row) => institutesById[row.instituteId] || row.instituteId },
    { key: 'className', label: 'Department', render: (row) => row.className || '-' },
    { key: 'deviceName', label: 'Device' },
    { key: 'devicePrivilege', label: 'Privilege' },
    {
      key: 'syncStatus',
      label: 'Device Sync Status',
      render: (row) =>
        row.syncStatus === 'SYNCED' ? (
          <span className="font-medium text-emerald-600">✅ Synced</span>
        ) : (
          <span className="font-medium text-red-600">❌ Not Synced</span>
        ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row) => (
        <div className="flex flex-wrap gap-2">
          <PermissionGate permission={PERMISSIONS.USER_EDIT}>
            <button onClick={() => openEdit(row)} className="text-primary-600 hover:underline">Edit</button>
            {row.syncStatus !== 'SYNCED' && (
              <button onClick={() => handleResend(row)} className="text-blue-600 hover:underline">Resend</button>
            )}
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.USER_DELETE}>
            <button onClick={() => setDeleteTarget(row)} className="text-red-600 hover:underline">Delete</button>
          </PermissionGate>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-xl font-semibold text-gray-800">User Management</h1>
        <div className="flex flex-wrap gap-2">
          <PermissionGate permission={PERMISSIONS.USER_EDIT}>
            <button
              onClick={handleResendAllUnsynced}
              disabled={resendingAll}
              className="rounded-md border border-blue-300 px-4 py-2 text-sm font-medium text-blue-600 hover:bg-blue-50 disabled:opacity-60"
            >
              {resendingAll ? 'Resending...' : 'Resend Unsynced'}
            </button>
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.USER_DELETE}>
            {selectedIds.length > 0 && (
              <button
                onClick={() => setBulkConfirmOpen(true)}
                className="rounded-md border border-red-300 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
              >
                Delete Selected ({selectedIds.length})
              </button>
            )}
            {instituteFilter && (
              <button
                onClick={() => setFilterDeleteConfirmOpen(true)}
                className="rounded-md border border-red-300 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
              >
                Delete All In Filter
              </button>
            )}
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.USER_EXPORT}>
            <button
              onClick={handleExport}
              disabled={exporting}
              className="rounded-md border border-primary-600 px-4 py-2 text-sm font-medium text-primary-600 hover:bg-primary-50 disabled:opacity-60"
            >
              {exporting ? 'Exporting...' : 'Download Excel'}
            </button>
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.USER_CREATE}>
            <button onClick={openImport} className="rounded-md border border-primary-600 px-4 py-2 text-sm font-medium text-primary-600 hover:bg-primary-50">
              Upload Excel
            </button>
            <button onClick={openCreate} className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700">
              + Add User
            </button>
          </PermissionGate>
        </div>
      </div>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <SearchBar value={search} onChange={(v) => { setSearch(v); setPage(0); }} placeholder="Search name or enroll no..." />
        {operator?.superAdmin && (
          <select
            value={instituteFilter}
            onChange={(e) => { setInstituteFilter(e.target.value); setDepartmentFilter(''); setPage(0); }}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="">All Institutes</option>
            {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
          </select>
        )}
        <select
          value={departmentFilter}
          onChange={(e) => { setDepartmentFilter(e.target.value); setPage(0); }}
          disabled={!instituteFilter}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
        >
          <option value="">All Departments</option>
          {filterDepartments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        loading={isLoading}
        selectable
        selectedIds={selectedIds}
        onToggleAll={(checked) => setSelectedIds(checked ? rows.map((r) => r.id) : [])}
        onToggleRow={(id, checked) =>
          setSelectedIds((prev) => (checked ? [...prev, id] : prev.filter((x) => x !== id)))
        }
      />

      <div className="mt-4 flex justify-end">
        <Pagination page={page} totalPages={data?.totalPages || 0} onPageChange={setPage} />
      </div>

      <Modal
        open={modalOpen}
        title={editing ? 'Edit User' : 'Add User'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="rounded-md border px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
            <button
              form="user-form"
              type="submit"
              disabled={saving}
              className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </>
        }
      >
        <form id="user-form" onSubmit={handleSave}>
          <FormField label="Institute" required>
            <select
              required
              disabled={!operator?.superAdmin}
              value={form.instituteId}
              onChange={(e) => setForm({ ...form, instituteId: e.target.value, classId: '', deviceId: '' })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select institute</option>
              {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
            </select>
          </FormField>
          <FormField label="Department">
            <select
              value={form.classId}
              onChange={(e) => setForm({ ...form, classId: e.target.value })}
              disabled={!form.instituteId}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select department</option>
              {formDepartments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <FormField label="Full Name" required>
            <input
              required
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Card No">
            <input
              value={form.cardNo}
              onChange={(e) => setForm({ ...form, cardNo: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Device Privilege" required>
            <select
              required
              value={form.devicePrivilege}
              onChange={(e) => setForm({ ...form, devicePrivilege: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            >
              <option value="COMMON">Common User</option>
              <option value="ENROLLER">Enroller</option>
              <option value="ADMIN">Admin</option>
            </select>
          </FormField>
          <FormField label="Device" required>
            <select
              required
              value={form.deviceId}
              onChange={(e) => setForm({ ...form, deviceId: e.target.value })}
              disabled={!form.instituteId}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select device</option>
              {(formDevices?.content || []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            {formDevicesError && (
              <p className="mt-1 text-xs text-red-500">Could not load devices - check the backend is reachable and try again.</p>
            )}
            {!formDevicesError && form.instituteId && (formDevices?.content || []).length === 0 && (
              <p className="mt-1 text-xs text-gray-400">No devices found for this institute yet - add one on the Devices page first.</p>
            )}
          </FormField>
        </form>
      </Modal>

      <Modal
        open={importModalOpen}
        title="Upload Excel (.xlsx)"
        onClose={() => setImportModalOpen(false)}
        footer={
          <>
            <button onClick={() => setImportModalOpen(false)} className="rounded-md border px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
            <button
              form="import-form"
              type="submit"
              disabled={importing}
              className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
            >
              {importing ? 'Uploading...' : 'Upload'}
            </button>
          </>
        }
      >
        <form id="import-form" onSubmit={handleImport}>
          <p className="mb-4 text-xs text-gray-500">
            Expected column order (first row is a header and is skipped): <strong>userId, name, cardNumber, department</strong>.
          </p>
          <FormField label="Institute" required>
            <select
              required
              disabled={!operator?.superAdmin}
              value={importForm.instituteId}
              onChange={(e) => setImportForm({ ...importForm, instituteId: e.target.value, classId: '', deviceId: '' })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select institute</option>
              {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
            </select>
          </FormField>
          <FormField label="Department">
            <select
              value={importForm.classId}
              onChange={(e) => setImportForm({ ...importForm, classId: e.target.value })}
              disabled={!importForm.instituteId}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select department</option>
              {importDepartments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            <p className="mt-1 text-xs text-gray-400">
              Applied to every row in this file. Leave blank to use each row's own "department" column instead.
            </p>
          </FormField>
          <FormField label="Target Device" required>
            <select
              required
              value={importForm.deviceId}
              onChange={(e) => setImportForm({ ...importForm, deviceId: e.target.value })}
              disabled={!importForm.instituteId}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select device</option>
              {(importDevices?.content || []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>
          <FormField label="Excel File" required>
            <input
              type="file"
              required
              accept=".xlsx"
              onChange={(e) => setImportForm({ ...importForm, file: e.target.files[0] })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        message={`Delete user "${deleteTarget?.fullName}"? This will also queue a delete on their device.`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />

      <ConfirmDialog
        open={bulkConfirmOpen}
        message={`Delete ${selectedIds.length} selected user(s)? This will also queue delete commands on their devices.`}
        onCancel={() => setBulkConfirmOpen(false)}
        onConfirm={handleBulkDelete}
      />

      <ConfirmDialog
        open={filterDeleteConfirmOpen}
        message={`Delete ALL users in ${institutesById[instituteFilter] || 'this institute'}${departmentFilter ? ' / ' + (filterDepartments.find((d) => String(d.id) === String(departmentFilter))?.name || 'selected department') : ''}? This cannot be undone and will queue delete commands on their devices.`}
        onCancel={() => setFilterDeleteConfirmOpen(false)}
        onConfirm={handleFilterDelete}
      />
    </div>
  );
}
