import { useAuth } from '../context/AuthContext';

export default function Navbar({ onToggleSidebar }) {
  const { operator, logout } = useAuth();

  return (
    <header className="flex h-16 items-center justify-between border-b bg-white px-4 md:px-6">
      <button onClick={onToggleSidebar} className="text-xl text-gray-500 md:hidden" aria-label="Toggle menu">
        ☰
      </button>
      <div className="hidden text-sm text-gray-500 md:block">
        {operator?.superAdmin ? 'All Institutes' : `Institute #${operator?.instituteId ?? '-'}`}
      </div>
      <div className="flex items-center gap-4">
        <div className="text-right">
          <p className="text-sm font-medium text-gray-800">{operator?.fullName}</p>
          <p className="text-xs text-gray-400">{[...(operator?.roles || [])].join(', ')}</p>
        </div>
        <button onClick={logout} className="rounded-md border px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50">
          Logout
        </button>
      </div>
    </header>
  );
}
