import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import FormField from '../components/FormField';
import PermissionGate from '../components/PermissionGate';
import { useInstitutes } from '../hooks/useInstitutes';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { createDepartment, deleteDepartment, fetchDepartments, updateDepartment } from '../services/departmentService';
import { PERMISSIONS } from '../utils/permissions';

const EMPTY_FORM = { instituteId: '', name: '' };

export default function Departments() {
  const { operator } = useAuth();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const { data: institutes = [] } = useInstitutes();

  const [instituteFilter, setInstituteFilter] = useState(operator?.superAdmin ? '' : operator?.instituteId);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_FORM, instituteId: operator?.superAdmin ? '' : operator?.instituteId });
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const { data: departments = [], isLoading } = useQuery(
    ['departments', instituteFilter],
    () => fetchDepartments(instituteFilter),
    { enabled: !!instituteFilter }
  );

  const institutesById = Object.fromEntries(institutes.map((i) => [i.id, i.name]));

  const openCreate = () => {
    setEditing(null);
    setForm({ instituteId: operator?.superAdmin ? instituteFilter || '' : operator?.instituteId, name: '' });
    setModalOpen(true);
  };

  const openEdit = (department) => {
    setEditing(department);
    setForm({ instituteId: department.instituteId, name: department.name });
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = { instituteId: Number(form.instituteId), name: form.name };
      if (editing) {
        await updateDepartment(editing.id, payload);
        showToast('Department updated', 'success');
      } else {
        await createDepartment(payload);
        showToast('Department created', 'success');
      }
      setModalOpen(false);
      queryClient.invalidateQueries(['departments']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to save department', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteDepartment(deleteTarget.id);
      showToast('Department deleted', 'success');
      setDeleteTarget(null);
      queryClient.invalidateQueries(['departments']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete department', 'error');
    }
  };

  const columns = [
    { key: 'name', label: 'Department Name' },
    { key: 'institute', label: 'Institute', render: (row) => institutesById[row.instituteId] || row.instituteId },
    {
      key: 'actions',
      label: 'Actions',
      render: (row) => (
        <div className="flex gap-2">
          <PermissionGate permission={PERMISSIONS.DEPARTMENT_EDIT}>
            <button onClick={() => openEdit(row)} className="text-primary-600 hover:underline">Edit</button>
          </PermissionGate>
          <PermissionGate permission={PERMISSIONS.DEPARTMENT_DELETE}>
            <button onClick={() => setDeleteTarget(row)} className="text-red-600 hover:underline">Delete</button>
          </PermissionGate>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-xl font-semibold text-gray-800">Departments</h1>
        <PermissionGate permission={PERMISSIONS.DEPARTMENT_CREATE}>
          <button
            onClick={openCreate}
            disabled={!operator?.superAdmin && !operator?.instituteId}
            className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
          >
            + Add Department
          </button>
        </PermissionGate>
      </div>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        {operator?.superAdmin && (
          <select
            value={instituteFilter}
            onChange={(e) => setInstituteFilter(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="">Select institute</option>
            {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
          </select>
        )}
      </div>

      {!instituteFilter ? (
        <p className="text-sm text-gray-500">Select an institute to view its departments.</p>
      ) : (
        <DataTable columns={columns} rows={departments} loading={isLoading} />
      )}

      <Modal
        open={modalOpen}
        title={editing ? 'Edit Department' : 'Add Department'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <button onClick={() => setModalOpen(false)} className="rounded-md border px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
            <button
              form="department-form"
              type="submit"
              disabled={saving}
              className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </>
        }
      >
        <form id="department-form" onSubmit={handleSave}>
          <FormField label="Institute" required>
            <select
              required
              disabled={!operator?.superAdmin}
              value={form.instituteId}
              onChange={(e) => setForm({ ...form, instituteId: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"
            >
              <option value="">Select institute</option>
              {institutes.map((i) => <option key={i.id} value={i.id}>{i.name}</option>)}
            </select>
          </FormField>
          <FormField label="Department Name" required>
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        message={`Delete department "${deleteTarget?.name}"? Users already assigned to it will keep their existing assignment on record but it will no longer be selectable.`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
