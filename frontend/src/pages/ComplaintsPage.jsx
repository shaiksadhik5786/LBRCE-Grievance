import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import { getShared } from '../services/api'
import { useAuth } from '../context/AuthContext'
import StatusBadge from '../components/StatusBadge'
import { EmptyState, ErrorNotice, formatDay, Loading } from '../components/PageState'

const types = ['Student concern', 'Academic', 'Faculty & Teaching', 'Fees & Finance', 'Infrastructure', 'Hostel', 'Transport', 'Library', 'Examination', 'IT / Network', 'Student Affairs', 'Other']
const statuses = ['REGISTERED', 'ACCEPTED', 'REJECTED', 'ASSIGNED', 'IN_PROGRESS', 'SOLUTION_SUBMITTED', 'REOPENED', 'SOLVED']
const initial = { academicYearId: '', streamId: '', department: '', complaintType: '', status: '', verificationStatus: '', priority: '', assignedStaffId: '', rollId: '', studentName: '', from: '', to: '' }
function filtersFromParams(params) {
  return Object.fromEntries(Object.keys(initial).map(key => [key, params.get(key) || '']))
}
function submittedFromParams(params) {
  return Object.fromEntries(Object.entries(filtersFromParams(params)).filter(([, value]) => value !== ''))
}

export default function ComplaintsPage() {
  const { user, complaintRevision } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const queryString = searchParams.toString()
  const synchronizedQuery = useRef(queryString)
  const admin = user?.role === 'ROLE_ADMIN'
  const staffRole = user?.role === 'ROLE_STAFF'
  const [filters, setFilters] = useState(() => filtersFromParams(searchParams))
  const [rows, setRows] = useState([])
  const [metadata, setMetadata] = useState({ years: [], streams: [], staff: [], departments: [] })
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0, totalElements: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [submitted, setSubmitted] = useState(() => submittedFromParams(searchParams))
  const [page, setPage] = useState(Number(searchParams.get('page') || 0))

  useEffect(() => {
    if (synchronizedQuery.current === queryString) return
    synchronizedQuery.current = queryString
    const params = new URLSearchParams(queryString)
    setFilters(filtersFromParams(params))
    setSubmitted(submittedFromParams(params))
    setPage(Math.max(0, Number(params.get('page') || 0)))
  }, [queryString])

  useEffect(() => {
    if (!admin) return
    let active = true
    Promise.all([getShared('/academic-years'), getShared('/streams'), getShared('/staff'), getShared('/departments')])
      .then(([years, streams, staff, departments]) => { if (active) setMetadata({ years: years.data, streams: streams.data, staff: staff.data, departments: departments.data }) })
      .catch(err => { if (active) setError(err) })
    return () => { active = false }
  }, [admin])

  useEffect(() => {
    let alive = true
    setLoading(true); setError(null)
    const params = { page, size: 15, ...submitted }
    Object.keys(params).forEach(key => { if (params[key] === '' || params[key] == null) delete params[key] })
    getShared('/complaints', { params }).then(({ data }) => {
      if (alive) { setRows(Array.isArray(data) ? data : data.content || []); setPageInfo({ number: data.number || 0, totalPages: data.totalPages || 0, totalElements: data.totalElements || 0 }) }
    }).catch(err => { if (alive) setError(err) }).finally(() => { if (alive) setLoading(false) })
    return () => { alive = false }
  }, [page, submitted, complaintRevision])

  function change(e) { setFilters(f => ({ ...f, [e.target.name]: e.target.value })) }
  function apply(e) {
    e.preventDefault()
    const params = new URLSearchParams(Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '')))
    synchronizedQuery.current = params.toString()
    setPage(0)
    setSubmitted(filters)
    setSearchParams(params)
  }
  function reset() {
    synchronizedQuery.current = ''
    setFilters(initial)
    setSubmitted({})
    setPage(0)
    setSearchParams({})
  }
  const title = admin ? 'All complaints' : staffRole ? 'Assigned complaints' : 'My complaints'

  return <div>
    <section className="welcome-row page-title-row"><div><span className="eyebrow">{admin ? 'CASE MANAGEMENT' : staffRole ? 'YOUR WORK QUEUE' : 'YOUR RECORDS'}</span><h1>{title}</h1><p className="muted">{admin ? 'Search, filter and review concerns from across the college.' : staffRole ? 'Only concerns assigned to your account appear here.' : 'Follow each concern from registration through resolution.'}</p></div>{user?.role === 'ROLE_STUDENT' && <Link className="button button-primary" to="/complaints/new">＋ New complaint</Link>}</section>
    {admin && <form className="filter-card" onSubmit={apply}><div className="filter-head"><div><span className="eyebrow">FIND A CASE</span><h2>Filters</h2></div><button className="text-button" type="button" onClick={reset}>Reset filters</button></div>
      <div className="filter-grid">
        <label className="field-label">Academic year<select name="academicYearId" value={filters.academicYearId} onChange={change}><option value="">All years</option>{metadata.years.map(y => <option key={y.academicYearId} value={y.academicYearId}>{y.yearName}</option>)}</select></label>
        <label className="field-label">Stream<select name="streamId" value={filters.streamId} onChange={change}><option value="">All streams</option>{metadata.streams.map(s => <option key={s.streamId} value={s.streamId}>{s.streamName}</option>)}</select></label>
        <label className="field-label">Department<select name="department" value={filters.department} onChange={change}><option value="">All departments</option>{metadata.departments.map(d => <option key={d} value={d}>{d}</option>)}</select></label>
        <label className="field-label">Complaint type<select name="complaintType" value={filters.complaintType} onChange={change}><option value="">All types</option>{types.map(t => <option key={t}>{t}</option>)}</select></label>
        <label className="field-label">Status<select name="status" value={filters.status} onChange={change}><option value="">All statuses</option>{statuses.map(s => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}</select></label>
        <label className="field-label">Verification<select name="verificationStatus" value={filters.verificationStatus} onChange={change}><option value="">All verification</option>{['PENDING', 'VERIFIED', 'REOPENED'].map(s => <option key={s}>{s}</option>)}</select></label>
        <label className="field-label">Priority<select name="priority" value={filters.priority} onChange={change}><option value="">All priorities</option>{['NORMAL', 'LOW', 'MEDIUM', 'HIGH', 'URGENT'].map(s => <option key={s}>{s}</option>)}</select></label>
        <label className="field-label">Assigned staff<select name="assignedStaffId" value={filters.assignedStaffId} onChange={change}><option value="">All staff</option>{metadata.staff.map(s => <option key={s.staffId} value={s.staffId}>{s.name}</option>)}</select></label>
        <label className="field-label">Roll ID<input name="rollId" value={filters.rollId} onChange={change} placeholder="Search Roll ID" /></label>
        <label className="field-label">Student name<input name="studentName" value={filters.studentName} onChange={change} placeholder="Search student name" /></label>
        <label className="field-label">From<input type="date" name="from" value={filters.from} onChange={change} /></label>
        <label className="field-label">To<input type="date" name="to" value={filters.to} onChange={change} /></label>
      </div>
      <div className="filter-actions"><span>Filters are applied by the server.</span><button className="button button-primary" type="submit">Apply filters <span>→</span></button></div>
    </form>}
    <div className="list-summary"><span>{loading ? 'Loading…' : `${pageInfo.totalElements} ${pageInfo.totalElements === 1 ? 'complaint' : 'complaints'}`}</span><span>{admin ? 'All departments' : 'Private to your account'}</span></div>
    <ErrorNotice error={error} />
    <section className="section-card list-card">
      {loading ? <Loading label="Loading complaints…" /> : rows.length ? <>
        <div className="table-scroll"><table><thead><tr><th>Complaint</th>{!user?.role?.includes('STUDENT') && <th>Student</th>}<th>Stream</th><th>Priority</th><th>Submitted</th><th>Status</th><th>Action</th></tr></thead><tbody>{rows.map(row => <tr key={row.complaintId}>
          <td><Link className="table-title" to={`/complaints/${row.complaintId}`}>#{row.complaintId} · {row.subject}</Link><small>{row.complaintType} · {row.academicYear}</small></td>
          {!user?.role?.includes('STUDENT') && <td>{row.studentName}<small>{row.studentRollId} · {row.department}</small></td>}
          <td>{row.streamName || 'Not classified'}</td><td><span className={`priority priority-${(row.priority || 'medium').toLowerCase()}`}>{row.priority || '—'}</span></td><td>{formatDay(row.createdDate)}</td><td><StatusBadge status={row.status} /></td><td><Link to={`/complaints/${row.complaintId}`} className="text-link">View Details <span>→</span></Link></td>
        </tr>)}</tbody></table></div>
        {pageInfo.totalPages > 1 && <div className="pagination"><span>Page {pageInfo.number + 1} of {pageInfo.totalPages}</span><div><button className="button button-secondary button-small" disabled={pageInfo.number === 0} onClick={() => setPage(p => Math.max(0, p - 1))}>← Previous</button><button className="button button-secondary button-small" disabled={pageInfo.number + 1 >= pageInfo.totalPages} onClick={() => setPage(p => p + 1)}>Next →</button></div></div>}
      </> : <EmptyState title="No complaints found" action={user?.role === 'ROLE_STUDENT' ? <Link className="button button-primary" to="/complaints/new">Raise your first concern</Link> : null}>{submitted && Object.keys(submitted).some(k => submitted[k]) ? 'Try changing or resetting the filters.' : 'Complaints that match your workspace will appear here.'}</EmptyState>}
    </section>
  </div>
}
