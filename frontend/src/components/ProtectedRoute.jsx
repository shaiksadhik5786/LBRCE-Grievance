import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ roles }) {
  const { role, isAuthenticated, initialized, loading } = useAuth()
  const location = useLocation()
  if (!initialized || loading) return <div className="screen-loader"><span className="spinner" /> Loading your workspace…</div>
  if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (roles && !roles.includes(role)) return <Navigate to="/forbidden" replace />
  return <Outlet />
}
