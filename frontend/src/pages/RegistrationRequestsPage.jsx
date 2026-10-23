import { useCallback, useEffect, useRef, useState } from 'react'
import api, { getShared, messageFromError } from '../services/api'
import { ErrorNotice, formatDate, Loading } from '../components/PageState'

export default function RegistrationRequestsPage() {
  const [requests, setRequests] = useState([])
  const [years, setYears] = useState({})
  const [status, setStatus] = useState('PENDING')
  const [reasons, setReasons] = useState({})
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(null)
  const [error, setError] = useState(null)
  const requestRef = useRef(0)
  const mounted = useRef(false)
  const load = useCallback(async () => {
    const requestId = ++requestRef.current
    setLoading(true); setError(null)
    try {
      const [requestsResponse, yearsResponse] = await Promise.all([getShared('/student-registrations', { params: status ? { status } : {} }), getShared('/academic-years')])
      if (requestId === requestRef.current) {
        setRequests(requestsResponse.data)
        setYears(Object.fromEntries(yearsResponse.data.map(y => [y.academicYearId, y.yearName])))
      }
    } catch (err) { if (requestId === requestRef.current) setError({ message: messageFromError(err) }) }
    finally { if (requestId === requestRef.current) setLoading(false) }
  }, [status])
  useEffect(() => {
    mounted.current = true
    void load()
    return () => { mounted.current = false; requestRef.current += 1 }
  }, [load])
  async function review(request, decision) {
    if (decision === 'reject' && !reasons[request.id]?.trim()) { setError({ message: 'Enter a reason before rejecting this request.' }); return }
    setBusy(request.id); setError(null)
    try {
      const path = `/student-registrations/${request.id}/${decision}`
      await api.put(path, decision === 'reject' ? { reason: reasons[request.id] } : {})
      if (mounted.current) await load()
    } catch (err) { if (mounted.current) setError({ message: messageFromError(err) }) }
    finally { if (mounted.current) setBusy(null) }
  }
  return <div><section className="welcome-row page-title-row"><div><span className="eyebrow">STUDENT ONBOARDING</span><h1>Registration requests</h1><p className="muted">Approve verified student details before they can sign in.</p></div><label className="field-label compact-field">Request status<select value={status} onChange={e => setStatus(e.target.value)}><option value="PENDING">Pending</option><option value="APPROVED">Approved</option><option value="REJECTED">Rejected</option><option value="">All requests</option></select></label></section>
    <ErrorNotice error={error} />
    <section className="section-card list-card">{loading ? <Loading label="Loading registration requests…" /> : requests.length ? <div className="table-scroll"><table className="registration-table"><thead><tr><th>Student</th><th>Academic year</th><th>Department</th><th>Contact</th><th>Submitted</th><th>Status / action</th></tr></thead><tbody>{requests.map(request => <tr key={request.id}><td><b>{request.name}</b><small>Roll ID: {request.rollId}</small></td><td>{years[request.academicYearId] || request.academicYearId}</td><td>{request.department}</td><td>{request.email}<small>{request.phone || 'No phone provided'}</small></td><td>{formatDate(request.submittedAt)}</td><td>{request.status === 'PENDING' ? <div className="registration-actions"><button className="button button-primary button-small" disabled={busy === request.id} onClick={() => review(request, 'approve')}>Approve</button><div className="inline-reject"><input value={reasons[request.id] || ''} onChange={e => setReasons(v => ({ ...v, [request.id]: e.target.value }))} placeholder="Rejection reason" /><button className="button button-danger button-small" disabled={busy === request.id} onClick={() => review(request, 'reject')}>Reject</button></div></div> : <span className={`request-status request-${request.status.toLowerCase()}`}>{request.status}</span>}{request.rejectionReason && <small className="rejection-note">{request.rejectionReason}</small>}</td></tr>)}</tbody></table></div> : <div className="empty-state"><div className="empty-icon">♧</div><h3>No {status.toLowerCase() || ''} requests</h3><p>Student sign-up requests matching this status will appear here.</p></div>}</section>
  </div>
}
