import { Navigate, Route, Routes } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import AdminLayout from '../layouts/AdminLayout';
import ProtectedRoute from './ProtectedRoute';
import Login from '../pages/Login';
import Dashboard from '../pages/Dashboard';
import Devices from '../pages/Devices';
import Users from '../pages/Users';
import Operators from '../pages/Operators';
import Roles from '../pages/Roles';
import Attendance from '../pages/Attendance';
import CommandMonitor from '../pages/CommandMonitor';
import NotFound from '../pages/NotFound';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AdminLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/devices" element={<Devices />} />
          <Route path="/users" element={<Users />} />
          <Route path="/operators" element={<Operators />} />
          <Route path="/roles" element={<Roles />} />
          <Route path="/attendance" element={<Attendance />} />
          <Route path="/commands" element={<CommandMonitor />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
