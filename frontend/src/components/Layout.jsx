import { NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import lbrceLogo from '../assets/lbrce-logo.jpg'

const menus = {
  ROLE_STUDENT: [
    ['◫', 'Dashboard', '/student/dashboard'], ['▤', 'Complaints', '/complaints'], ['＋', 'New complaint', '/complaints/new'], ['◎', 'Profile', '/profile'],
  ],
  ROLE_STAFF: [
    ['◫', 'Dashboard', '/staff/dashboard'], ['▤', 'Complaints', '/complaints'], ['◎', 'Profile', '/profile'],
  ],
  ROLE_ADMIN: [
    ['◫', 'Dashboard', '/admin/dashboard'], ['▤', 'Complaints', '/complaints'], ['♧', 'Registration requests', '/admin/registrations'],
    ['♙', 'Students', '/admin/students'], ['♟', 'Staff', '/admin/staff'], ['♛', 'Administrators', '/admin/admins'], ['◇', 'Streams', '/admin/streams'], ['▦', 'Academic years', '/admin/years'], ['◎', 'Profile', '/profile'],
  ],
}

export default function Layout() {
  const { user, signOut } = useAuth()
  const [open, setOpen] = useState(false)
  const location = useLocation()
  const lastComplaintSearch = useRef('')
  const navigate = useNavigate()
  const roleLabel = user?.role?.replace('ROLE_', '')?.toLowerCase()
  useEffect(() => {
    if (location.pathname === '/complaints') lastComplaintSearch.current = location.search
  }, [location.pathname, location.search])
  function menuPath(path) {
    const complaintSearch = location.pathname === '/complaints' ? location.search : lastComplaintSearch.current
    return user?.role === 'ROLE_ADMIN' && path === '/admin/staff' && complaintSearch
      ? `${path}${complaintSearch}`
      : path
  }
  function logout() {
    signOut()
    setOpen(false)
    navigate('/login', { replace: true })
  }

  return <div className="app-shell">
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="brand"><img className="brand-logo" src={lbrceLogo} alt="Lakireddy Bali Reddy College of Engineering logo" /><div><b>LBRCE</b><span>Grievance portal</span></div></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav aria-label="Main navigation">{(menus[user?.role] || []).map(([icon, label, path]) => <NavLink key={path} to={menuPath(path)} onClick={() => setOpen(false)} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}><span className="nav-icon">{icon}</span>{label}</NavLink>)}
        <button type="button" className="nav-item logout-nav-item" onClick={logout}><span className="nav-icon" aria-hidden="true">↗</span>Logout</button>
      </nav>
      <div className="sidebar-bottom"><div className="support-card"><span className="support-pulse" /><div><b>Here to help</b><small>Your concerns matter to us.</small></div></div><div className="user-mini"><div className="avatar">{user?.name?.slice(0, 1)?.toUpperCase() || 'U'}</div><div className="user-mini-copy"><b>{user?.name || user?.rollId}</b><small>{roleLabel} · {user?.rollId}</small></div></div></div>
    </aside>
    {open && <button aria-label="Close menu" className="mobile-backdrop" onClick={() => setOpen(false)} />}
    <div className="main-wrap">
      <header className="topbar"><button className="menu-toggle" onClick={() => setOpen(v => !v)} aria-label="Toggle navigation">☰</button><div className="crumb">College grievance management <span>/</span> <b>{roleLabel} workspace</b></div><div className="top-actions"><span className="secure-label"><i /> Secure portal</span><button className="top-user" onClick={() => navigate('/profile')} aria-label="Open profile"><span className="top-user-copy"><b>{user?.name || user?.rollId}</b><small>{roleLabel}</small></span><span className="top-avatar" aria-hidden="true">{user?.name?.slice(0, 1)?.toUpperCase() || 'U'}</span></button></div></header>
      <main className="page-content"><Outlet /></main>
      <footer className="footer"><span>CampusCare · College grievance management</span><span>Need assistance? Contact your college office</span></footer>
    </div>
  </div>
}
