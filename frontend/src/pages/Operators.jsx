import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import FormField from '../components/FormField';
import StatusBadge from '../components/StatusBadge';
import PermissionGate from '../components/PermissionGate';
import { useInstitutes } from '../hooks/useInstitutes';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { createOperator, deleteOperator, fetchOperators, fetchRoles, updateOperator } from '../services/roleService';
import { PERMISSIONS } from '../utils/permissions';

const EMPTY_FORM = { username: '', password: '', fullName: '', email: '', instituteId: '', roleIds: [] };

export default function Operators() {
  const { operator: currentOperator } = useAuth();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const { data: institutes = [] } = useInstitutes();
  const { data: roles = [] } = useQuery(['roles'], fetchRoles);
  const { data: operators = [], isLoading } = useQuery(['operators'], fetchOperators);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const institutesById = Object.fromEntries(institutes.map((i) => [i.id, i.name]));
  const rolesByName = Object.fromEntries(roles.map((r) => [r.name, r.id]));

  const openCreate = () => {
    setEditing(null);
    setForm({ ...EMPTY_FORM, instituteId: currentOperator?.superAdmin ? '' : currentOperator?.instituteId });
    setModalOpen(true);
  };

  const openEdit = (op) => {
    setEditing(op);
    setForm({
      username: op.username,
      password: '',
      fullName: op.fullName,
      email: op.email || '',
      instituteId: op.instituteId ?? '',
      roleIds: op.roles.map((name) => rolesByName[name]).filter(Boolean),
    });
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        instituteId: form.instituteId ? Number(form.instituteId) : null,
      };
      if (editing && !payload.password) {
        delete payload.password;
      }
      if (editing) {
        await updateOperator(editing.id, payload);
        showToast('Operator updated', 'success');
      } else {
        await createOperator(payload);
        showToast('Operator created', 'success');
      }
      setModalOpen(false);
      queryClient.invalidateQueries(['operators']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to save operator', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteOperator(deleteTarget.id);
      showToast('Operator deleted', 'success');
      setDeleteTarget(null);
      queryClient.invalidateQueries(['operators']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete operator', 'error');
    }
  };

  const columns = [
    { key: 'username', label: 'Username' },
    { key: 'fullName', label: 'Full Name' },
    { key: 'email', label: 'Email', render: (o) => o.email || '-' },
    {
      key: 'institute',
      label: 'Institute',
      render: (o) => (o.instituteId ? institutesById[o.instituteId] || o.instituteId : 'All (Super Admin)'),
    },
    { key: 'roles', label: 'Roles', render: (o) => o.roles.join(', ') || '-' },
    { key: 'status', label: 'Status', render: (o) => <StatusBadge status={o.status} /> },
    {
      key: 'actions',
      label: 'Actions',
      render: (o) => (
        <div className="flex gap-2">
          <PermissionGate permission={PERMISSIONS.ROLE_MANAGE}>
            <button onClick={() => openEdit(o)} className="text-primary-600 hover:underline">Edit</button>
            <button
              onClick={() => setDeleteTarget(o)}
              disabled={o.username === currentOperator?.username}
              className="text-red-600 hover:underline disabled:text-gray-300"
            >
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
        <h1 className="text-xl font-semibold text-gray-800">Operator Management</h1>
        <PermissionGate permission={PERMISSIONS.ROLE_MANAGE}>
          <button onClick={openCreate} className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700">
            + Add Operator
          </button>
        </PermissionGate>
      </div>

      <DataTable columns={columns} rows={operators} loading={isLoading} />

      <Modal
        open={modalOpen}
        title={editing ? 'Edit Operator' : 'Add Operator'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="rounded-md border px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
            <button
              form="operator-form"
              type="submit"
              disabled={saving}
              className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </>
        }
      >
        <form id="operator-form" onSubmit={handleSave}>
          <FormField label="Username" required>
            <input
              required
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label={editing ? 'Password (leave blank to keep current)' : 'Password'} required={!editing}>
            <input
              type="password"
              required={!editing}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Full Name" required>
            <input
              required
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Email">
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Institute">
            <select
              disabled={!currentOperator?.superAdmin}
              value={form.instituteId}
              onChange={(e) => setForm({ ...form, instituteId: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">All Institutes (Super Admin)</option>
              {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
            </select>
          </FormField>
          <FormField label="Roles" required>
            <div className="space-y-2 rounded-md border p-3">
              {roles.length === 0 && <span className="text-sm text-gray-400">No roles defined yet</span>}
              {roles.map((r) => (
                <label key={r.id} className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={form.roleIds.includes(r.id)}
                    onChange={() =>
                      setForm((prev) => ({
                        ...prev,
                        roleIds: prev.roleIds.includes(r.id)
                          ? prev.roleIds.filter((x) => x !== r.id)
                          : [...prev.roleIds, r.id],
                      }))
                    }
                  />
                  {r.name}
                </label>
              ))}
            </div>
          </FormField>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        message={`Delete operator "${deleteTarget?.username}"? They will immediately lose access to the panel.`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
