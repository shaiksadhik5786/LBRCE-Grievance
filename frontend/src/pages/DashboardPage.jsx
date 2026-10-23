import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { useAuth } from '../context/AuthContext'
import { getComplaintPage, getShared } from '../services/api'
import StatusBadge from '../components/StatusBadge'
import { EmptyState, ErrorNotice, formatDay, Loading } from '../components/PageState'

const headings = { ROLE_STUDENT: 'Your voice matters.', ROLE_STAFF: 'Your work makes a difference.', ROLE_ADMIN: 'A clearer view of every concern.' }
const subtitles = { ROLE_STUDENT: 'Keep track of concerns you have raised and what happens next.', ROLE_STAFF: 'Review the concerns assigned to you and keep students informed.', ROLE_ADMIN: 'Review, classify and move student concerns toward resolution.' }
const statNames = {
  ROLE_STUDENT: [['TOTAL', 'Total complaints'], ['REGISTERED', 'Registered'], ['ACCEPTED', 'Accepted'], ['IN_PROGRESS', 'In progress'], ['SOLUTION_SUBMITTED', 'Solution submitted'], ['REOPENED', 'Reopened'], ['SOLVED', 'Solved'], ['REJECTED', 'Rejected']],
  ROLE_STAFF: [['ASSIGNED', 'Assigned to you'], ['IN_PROGRESS', 'In progress'], ['SOLUTION_SUBMITTED', 'Solution submitted'], ['REOPENED', 'Reopened'], ['SOLVED', 'Resolved']],
  ROLE_ADMIN: [['TOTAL', 'Total complaints'], ['REGISTERED', 'Pending complaints'], ['ACCEPTED', 'Accepted'], ['REJECTED', 'Rejected'], ['ASSIGNED', 'Assigned'], ['IN_PROGRESS', 'In progress'], ['SOLUTION_SUBMITTED', 'Needs verification'], ['REOPENED', 'Reopened'], ['SOLVED', 'Resolved'], ['PENDING_REGISTRATIONS', 'Registration requests']],
}

export default function DashboardPage() {
  const { user, complaintRevision } = useAuth()
  const [stats, setStats] = useState(null)
  const [directoryCounts, setDirectoryCounts] = useState(null)
  const [complaints, setComplaints] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    setStats(null)
    setComplaints([])
    setDirectoryCounts(null)
    const dashboardRequest = Promise.all([getShared('/dashboard/stats'), getComplaintPage({ size: 5 })])
    const directoryRequest = user?.role === 'ROLE_ADMIN'
      ? Promise.all([
        getShared('/students'),
        getShared('/staff'),
        getShared('/admins'),
        getShared('/streams'),
      ])
      : Promise.resolve(null)
    Promise.allSettled([dashboardRequest, directoryRequest])
      .then(([dashboardResult, directoryResult]) => {
        if (!active) return
        const errors = []
        if (dashboardResult.status === 'fulfilled') {
          const [s, rows] = dashboardResult.value
          setStats(s.data)
          setComplaints(rows)
        } else errors.push(dashboardResult.reason)
        if (user?.role === 'ROLE_ADMIN' && directoryResult.status === 'fulfilled' && directoryResult.value) {
          const [students, staff, admins, streams] = directoryResult.value
          const studentCount = Array.isArray(students.data) ? students.data.length : 0
          const staffCount = Array.isArray(staff.data) ? staff.data.length : 0
          const adminCount = Array.isArray(admins.data) ? admins.data.length : 0
          setDirectoryCounts({ students: studentCount, staff: staffCount, admins: adminCount,
            users: studentCount + staffCount + adminCount,
            streams: Array.isArray(streams.data) ? streams.data.length : 0 })
        } else if (user?.role === 'ROLE_ADMIN' && directoryResult.status === 'rejected') errors.push(directoryResult.reason)
        setError(errors[0] || null)
      })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [user?.role, complaintRevision])
  const role = user?.role || 'ROLE_STUDENT'
  const greeting = user?.name?.split(' ')[0] || 'there'
  const complaintView = key => key === 'PENDING_REGISTRATIONS'
    ? '/admin/registrations'
    : key === 'TOTAL' ? '/complaints' : `/complaints?status=${key}`
  return <div className="dashboard-page">
    <section className="welcome-row"><div><span className="eyebrow">{new Intl.DateTimeFormat('en', { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date())}</span><h1>{headings[role]}</h1><p className="muted">{subtitles[role]}</p></div><div className="welcome-actions">{role === 'ROLE_STUDENT' && <Link to="/complaints/new" className="button button-primary">＋ New complaint</Link>}{role === 'ROLE_ADMIN' && <Link to="/admin/registrations" className="button button-secondary">Review registrations <span>→</span></Link>}</div></section>
    <div className="greeting-line">Good day, <b>{greeting}</b> <span>·</span> Here’s what needs your attention.</div>
    <ErrorNotice error={error} />
    {loading ? <Loading label="Gathering your latest updates…" /> : !stats ? <EmptyState title="Dashboard data is unavailable">Try refreshing this page in a moment.</EmptyState> : <>
      <section className={`stat-grid stat-grid-${role.replace('ROLE_', '').toLowerCase()}`}>
        {(statNames[role] || statNames.ROLE_STUDENT).map(([key, label], i) => <article className={`stat-card stat-accent-${i % 5}`} key={key}><div className="stat-head"><span>{label}</span><span className="stat-dot" /></div><strong>{stats?.[key] ?? 0}</strong><small>{key === 'PENDING_REGISTRATIONS' ? 'Waiting for review' : key === 'TOTAL' ? 'Across your workspace' : 'Complaints'}</small><Link className="text-link stat-view" to={complaintView(key)}>View <span>→</span></Link></article>)}
      </section>
      {role === 'ROLE_ADMIN' && directoryCounts && <section className="directory-counts"><div className="section-heading compact"><div><span className="eyebrow">COLLEGE DIRECTORY</span><h2>Accounts and streams</h2></div></div><div className="stat-grid stat-grid-admin">
        {[
          ['students', 'Total Students', '/admin/students'],
          ['staff', 'Total Staff', '/admin/staff'],
          ['admins', 'Total Admins', null],
          ['users', 'Total Users', null],
          ['streams', 'Streams', '/admin/streams'],
        ].map(([key, label, destination], i) => <article className={`stat-card stat-accent-${i % 5}`} key={key}><div className="stat-head"><span>{label}</span><span className="stat-dot" /></div><strong>{directoryCounts[key]}</strong><small>{key === 'streams' ? 'Available categories' : 'Active accounts'}</small>{destination && <Link className="text-link stat-view" to={destination}>View <span>→</span></Link>}</article>)}
      </div></section>}
      <section className="section-card recent-section"><div className="section-heading"><div><span className="eyebrow">LATEST ACTIVITY</span><h2>{role === 'ROLE_STUDENT' ? 'Your complaints' : role === 'ROLE_STAFF' ? 'Assigned to you' : 'Recent complaints'}</h2></div><Link to="/complaints" className="text-link">View all <span>→</span></Link></div>
        {complaints.length ? <div className="table-scroll"><table><thead><tr><th>Complaint</th><th>Student</th><th>Submitted</th><th>Status</th><th /></tr></thead><tbody>{complaints.map(item => <tr key={item.complaintId}><td><Link className="table-title" to={`/complaints/${item.complaintId}`}>#{item.complaintId} · {item.subject}</Link><small>{item.complaintType}</small></td><td>{role === 'ROLE_STUDENT' ? 'You' : <>{item.studentName}<small>{item.studentRollId}</small></>}</td><td>{formatDay(item.createdDate)}</td><td><StatusBadge status={item.status} /></td><td><Link to={`/complaints/${item.complaintId}`} className="row-arrow" aria-label="Open complaint">↗</Link></td></tr>)}</tbody></table></div> : <EmptyState title="No complaints yet">Your recent complaints will show here when they are submitted.</EmptyState>}
      </section>
    </>}
    <section className="help-strip"><span className="help-icon">✳</span><div><b>{role === 'ROLE_STUDENT' ? 'Not sure where to start?' : 'Every update keeps the process clear.'}</b><p>{role === 'ROLE_STUDENT' ? 'Share the details of your concern and your college team will take it from there.' : 'Use the timeline on each complaint to review every action and update.'}</p></div>{role === 'ROLE_STUDENT' && <Link to="/complaints/new" className="text-link">Raise a concern <span>→</span></Link>}</section>
  </div>
}
