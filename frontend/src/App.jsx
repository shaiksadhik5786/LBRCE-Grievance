import { Link, Navigate, Route, Routes } from 'react-router'
import { dashboardPathForRole, useAuth } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import ComplaintsPage from './pages/ComplaintsPage'
import ComplaintFormPage from './pages/ComplaintFormPage'
import ComplaintDetailPage from './pages/ComplaintDetailPage'
import RegistrationRequestsPage from './pages/RegistrationRequestsPage'
import AdminStudentsPage from './pages/AdminStudentsPage'
import AdminManagementPage from './pages/AdminManagementPage'
import ProfilePage from './pages/ProfilePage'
import AdminAccountsPage from './pages/AdminAccountsPage'

function HomeRedirect() {
  const { role, isAuthenticated, initialized, loading } = useAuth()
  if (!initialized || loading) return <div className="screen-loader"><span className="spinner" /> Loading your workspace…</div>
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <Navigate to={dashboardPathForRole(role)} replace />
}

function ForbiddenPage() { return <div className="forbidden-page"><span className="eyebrow">ACCESS RESTRICTED</span><h1>This workspace isn’t available to your account.</h1><p className="muted">Your account role does not have permission to view that page.</p><Link to="/" className="button button-primary">Return to your dashboard</Link></div> }
function NotFoundPage() { return <div className="forbidden-page"><span className="eyebrow">PAGE NOT FOUND</span><h1>We couldn’t find that page.</h1><p className="muted">The link may be out of date.</p><Link to="/" className="button button-primary">Return to your dashboard</Link></div> }

export default function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/student/register" element={<RegisterPage />} />
    <Route element={<ProtectedRoute />}>
      <Route element={<Layout />}>
        <Route index element={<HomeRedirect />} />
        <Route path="forbidden" element={<ForbiddenPage />} />
        <Route element={<ProtectedRoute roles={['ROLE_STUDENT']} />}>
          <Route path="student/dashboard" element={<DashboardPage />} />
          <Route path="complaints/new" element={<ComplaintFormPage />} />
          <Route path="complaints/:id/edit" element={<ComplaintFormPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={['ROLE_STAFF']} />}>
          <Route path="staff/dashboard" element={<DashboardPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={['ROLE_ADMIN']} />}>
          <Route path="admin/dashboard" element={<DashboardPage />} />
          <Route path="admin/registrations" element={<RegistrationRequestsPage />} />
          <Route path="admin/students" element={<AdminStudentsPage />} />
          <Route path="admin/staff" element={<AdminManagementPage />} />
          <Route path="admin/admins" element={<AdminAccountsPage />} />
          <Route path="admin/streams" element={<AdminManagementPage />} />
          <Route path="admin/years" element={<AdminManagementPage />} />
        </Route>
        <Route element={<ProtectedRoute roles={['ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN']} />}>
          <Route path="complaints" element={<ComplaintsPage />} />
          <Route path="complaints/:id" element={<ComplaintDetailPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Route>
  </Routes>
}
