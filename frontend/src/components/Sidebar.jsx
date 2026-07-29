import { NavLink } from 'react-router-dom';
import { usePermission } from '../hooks/usePermission';
import { PERMISSIONS } from '../utils/permissions';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', permission: PERMISSIONS.DASHBOARD_VIEW, icon: '📊' },
  { to: '/devices', label: 'Devices', permission: PERMISSIONS.DEVICE_VIEW, icon: '🖥️' },
  { to: '/users', label: 'Users', permission: PERMISSIONS.USER_VIEW, icon: '👥' },
  { to: '/operators', label: 'Operators', permission: PERMISSIONS.ROLE_VIEW, icon: '🧑‍💼' },
  { to: '/roles', label: 'Roles & Permissions', permission: PERMISSIONS.ROLE_VIEW, icon: '🔐' },
  { to: '/attendance', label: 'Attendance', permission: PERMISSIONS.ATTENDANCE_VIEW, icon: '🗓️' },
  { to: '/commands', label: 'Command Monitor', permission: PERMISSIONS.COMMAND_VIEW, icon: '📡' },
];

function NavItem({ item }) {
  const allowed = usePermission(item.permission);
  if (!allowed) return null;

  return (
    <NavLink
      to={item.to}
      className={({ isActive }) =>
        `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
          isActive ? 'bg-primary-600 text-white' : 'text-gray-300 hover:bg-gray-800 hover:text-white'
        }`
      }
    >
      <span>{item.icon}</span>
      {item.label}
    </NavLink>
  );
}

export default function Sidebar({ open }) {
  return (
    <aside
      className={`fixed inset-y-0 left-0 z-30 w-64 transform bg-gray-900 transition-transform md:static md:translate-x-0 ${
        open ? 'translate-x-0' : '-translate-x-full'
      }`}
    >
      <div className="flex h-16 items-center gap-2 px-5 text-white">
        <span className="text-xl">🔒</span>
        <span className="text-lg font-bold">ZKTeco Admin</span>
      </div>
      <nav className="flex flex-col gap-1 px-3">
        {NAV_ITEMS.map((item) => (
          <NavItem key={item.to} item={item} />
        ))}
      </nav>
    </aside>
  );
}
