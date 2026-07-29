import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import FormField from '../components/FormField';
import PermissionGate from '../components/PermissionGate';
import { useToast } from '../context/ToastContext';
import { createRole, deleteRole, fetchPermissions, fetchRoles, updateRole } from '../services/roleService';
import { PERMISSIONS } from '../utils/permissions';

export default function Roles() {
  const { showToast } = useToast();
  const queryClient = useQueryClient();

  const { data: roles = [], isLoading: rolesLoading } = useQuery(['roles'], fetchRoles);
  const { data: permissions = [] } = useQuery(['permissions'], fetchPermissions);

  const groupedPermissions = permissions.reduce((acc, p) => {
    (acc[p.module] = acc[p.module] || []).push(p);
    return acc;
  }, {});

  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState(null);
  const [roleForm, setRoleForm] = useState({ name: '', description: '', permissionIds: [] });
  const [savingRole, setSavingRole] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const openCreateRole = () => {
    setEditingRole(null);
    setRoleForm({ name: '', description: '', permissionIds: [] });
    setRoleModalOpen(true);
  };

  const openEditRole = (role) => {
    setEditingRole(role);
    setRoleForm({ name: role.name, description: role.description || '', permissionIds: role.permissions.map((p) => p.id) });
    setRoleModalOpen(true);
  };

  const togglePermission = (id) => {
    setRoleForm((prev) => ({
      ...prev,
      permissionIds: prev.permissionIds.includes(id)
        ? prev.permissionIds.filter((x) => x !== id)
        : [...prev.permissionIds, id],
    }));
  };

  const handleSaveRole = async (e) => {
    e.preventDefault();
    setSavingRole(true);
    try {
      if (editingRole) {
        await updateRole(editingRole.id, roleForm);
        showToast('Role updated', 'success');
      } else {
        await createRole(roleForm);
        showToast('Role created', 'success');
      }
      setRoleModalOpen(false);
      queryClient.invalidateQueries(['roles']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to save role', 'error');
    } finally {
      setSavingRole(false);
    }
  };

  const handleDeleteRole = async () => {
    try {
      await deleteRole(deleteTarget.id);
      showToast('Role deleted', 'success');
      setDeleteTarget(null);
      queryClient.invalidateQueries(['roles']);
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete role', 'error');
    }
  };

  const roleColumns = [
    { key: 'name', label: 'Role Name' },
    { key: 'description', label: 'Description', render: (r) => r.description || '-' },
    { key: 'permissions', label: 'Permissions', render: (r) => `${r.permissions.length} assigned` },
    {
      key: 'actions',
      label: 'Actions',
      render: (r) => (
        <div className="flex gap-2">
          <PermissionGate permission={PERMISSIONS.ROLE_MANAGE}>
            <button onClick={() => openEditRole(r)} disabled={r.name === 'SUPER_ADMIN'} className="text-primary-600 hover:underline disabled:text-gray-300">
              Edit
            </button>
            <button onClick={() => setDeleteTarget(r)} disabled={r.name === 'SUPER_ADMIN'} className="text-red-600 hover:underline disabled:text-gray-300">
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
        <h1 className="text-xl font-semibold text-gray-800">Roles & Permissions</h1>
        <PermissionGate permission={PERMISSIONS.ROLE_MANAGE}>
          <button onClick={openCreateRole} className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700">
            + Create Role
          </button>
        </PermissionGate>
      </div>

      <DataTable columns={roleColumns} rows={roles} loading={rolesLoading} />

      <Modal
        open={roleModalOpen}
        title={editingRole ? 'Edit Role' : 'Create Role'}
        size="lg"
        onClose={() => setRoleModalOpen(false)}
        footer={
          <>
            <button onClick={() => setRoleModalOpen(false)} className="rounded-md border px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
            <button
              form="role-form"
              type="submit"
              disabled={savingRole}
              className="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-60"
            >
              {savingRole ? 'Saving...' : 'Save'}
            </button>
          </>
        }
      >
        <form id="role-form" onSubmit={handleSaveRole}>
          <FormField label="Role Name" required>
            <input
              required
              value={roleForm.name}
              onChange={(e) => setRoleForm({ ...roleForm, name: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Description">
            <input
              value={roleForm.description}
              onChange={(e) => setRoleForm({ ...roleForm, description: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </FormField>
          <FormField label="Permissions" required>
            <div className="space-y-3">
              {Object.entries(groupedPermissions).map(([module, perms]) => (
                <div key={module} className="rounded-md border p-3">
                  <p className="mb-2 text-xs font-semibold uppercase text-gray-500">{module}</p>
                  <div className="grid grid-cols-2 gap-2">
                    {perms.map((p) => (
                      <label key={p.id} className="flex items-center gap-2 text-sm">
                        <input
                          type="checkbox"
                          checked={roleForm.permissionIds.includes(p.id)}
                          onChange={() => togglePermission(p.id)}
                        />
                        {p.description}
                      </label>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </FormField>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleteTarget}
        message={`Delete role "${deleteTarget?.name}"?`}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDeleteRole}
      />
    </div>
  );
}
