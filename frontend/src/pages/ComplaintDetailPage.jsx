import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router'
import api, { getShared, messageFromError } from '../services/api'
import { useAuth } from '../context/AuthContext'
import StatusBadge from '../components/StatusBadge'
import { AttachmentButton, ErrorNotice, formatDate, Loading } from '../components/PageState'

export default function ComplaintDetailPage() {
  const { id } = useParams()
  const { user, complaintRevision, invalidateComplaints } = useAuth()
  const [complaint, setComplaint] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [streams, setStreams] = useState([])
  const [staff, setStaff] = useState([])
  const [assignment, setAssignment] = useState({ streamId: '', staffId: '', priority: 'NORMAL' })
  const [rejectReason, setRejectReason] = useState('')
  const [remarks, setRemarks] = useState('')
  const [solution, setSolution] = useState('')
  const [proof, setProof] = useState(null)
  const mounted = useRef(false)
  const admin = user?.role === 'ROLE_ADMIN'
  const isStaff = user?.role === 'ROLE_STAFF'

  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])

  useEffect(() => {
    let active = true
    setLoading(true)
    setComplaint(null)
    setError(null)
    getShared(`/complaints/${id}`).then(({ data }) => {
      if (!active) return
      setComplaint(data)
      setAssignment({ streamId: String(data.streamId || ''), staffId: String(data.assignedStaffId || ''), priority: data.priority || 'NORMAL' })
    }).catch(err => { if (active) setError(err) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, complaintRevision])

  useEffect(() => {
    if (!admin) { setStreams([]); return }
    let active = true
    getShared('/streams').then(({ data }) => {
      if (!active) return
      setStreams(data)
      if (data[0]) setAssignment(value => ({ ...value, streamId: value.streamId || String(data[0].streamId) }))
    }).catch(err => { if (active) setError(err) })
    return () => { active = false }
  }, [admin])
  useEffect(() => {
    if (!admin || !assignment.streamId) { setStaff([]); return }
    let active = true
    setStaff([])
    getShared(`/staff/by-stream/${assignment.streamId}`).then(({ data }) => {
      if (!active) return
      setStaff(data)
      setAssignment(value => ({ ...value, staffId: data.some(person => String(person.staffId) === value.staffId) ? value.staffId : (data[0] ? String(data[0].staffId) : '') }))
    }).catch(err => { if (active) setError(err) })
    return () => { active = false }
  }, [admin, assignment.streamId])

  async function mutate(action, body) {
    setBusy(true); setError(null)
    try {
      await action(body)
      invalidateComplaints()
      if (mounted.current) { setLoading(true); setComplaint(null) }
    } catch (err) { if (mounted.current) setError({ message: messageFromError(err) }) }
    finally { if (mounted.current) setBusy(false) }
  }
  if (loading) return <Loading label="Loading complaint details…" />
  if (!complaint) return <div><ErrorNotice error={error || { message: 'Complaint not found.' }} /><Link to="/complaints" className="button button-secondary">← Back to complaints</Link></div>

  return <div className="detail-page">
    <Link to="/complaints" className="back-link">← Back to complaints</Link>
    <ErrorNotice error={error} />
    <section className="detail-hero"><div><span className="eyebrow">COMPLAINT #{complaint.complaintId} · {complaint.academicYear}</span><h1>{complaint.subject}</h1><div className="detail-meta"><StatusBadge status={complaint.status} /><span>Submitted {formatDate(complaint.createdDate)}</span><span>·</span><span>{complaint.complaintType}</span></div></div>{user?.role === 'ROLE_STUDENT' && complaint.status === 'REGISTERED' && <Link to={`/complaints/${id}/edit`} className="button button-secondary">✎ Edit complaint</Link>}</section>
    <div className="detail-layout">
      <div className="detail-main">
        <section className="section-card detail-card"><div className="section-heading compact"><div><span className="eyebrow">THE CONCERN</span><h2>Complaint details</h2></div></div><div className="detail-copy"><p>{complaint.description}</p></div>{complaint.attachmentName && <div className="attachment-row"><span className="attachment-icon">↥</span><div><b>Student proof</b><small>{complaint.attachmentName}</small></div><AttachmentButton complaintId={id} kind="student" name={complaint.attachmentName} /></div>}
          {complaint.rejectionReason && <div className="reason-card"><b>Admin response</b><p>{complaint.rejectionReason}</p></div>}
        </section>

        {admin && complaint.status === 'REGISTERED' && <section className="section-card workflow-card"><span className="eyebrow">ADMIN REVIEW</span><h2>Review this complaint</h2><p className="muted">Accept it to classify and assign the concern, or reject it with a clear reason for the student.</p><div className="workflow-actions"><button disabled={busy} className="button button-primary" onClick={() => mutate(body => api.put(`/complaints/${id}/review`, body), { action: 'ACCEPT' })}>✓ Accept complaint</button><div className="inline-reject"><input value={rejectReason} onChange={e => setRejectReason(e.target.value)} placeholder="Reason for rejection" /><button disabled={busy || !rejectReason.trim()} className="button button-danger" onClick={() => mutate(body => api.put(`/complaints/${id}/review`, body), { action: 'REJECT', reason: rejectReason })}>Reject</button></div></div></section>}

        {admin && ['ACCEPTED', 'ASSIGNED'].includes(complaint.status) && <section className="section-card workflow-card"><span className="eyebrow">CLASSIFICATION & ASSIGNMENT</span><h2>{complaint.status === 'ASSIGNED' ? 'Update assignment' : 'Choose a stream and staff member'}</h2><p className="muted">The staff list is limited to people assigned to the selected stream.</p><form className="assignment-grid" onSubmit={e => { e.preventDefault(); mutate(body => api.put(`/complaints/${id}/assign`, body), { ...assignment, streamId: Number(assignment.streamId), staffId: Number(assignment.staffId) }) }}><label className="field-label">Stream<select value={assignment.streamId} onChange={e => setAssignment(v => ({ ...v, streamId: e.target.value, staffId: '' }))} required><option value="">Choose stream</option>{streams.map(s => <option key={s.streamId} value={s.streamId}>{s.streamName}</option>)}</select></label><label className="field-label">Assigned staff<select value={assignment.staffId} onChange={e => setAssignment(v => ({ ...v, staffId: e.target.value }))} required><option value="">Choose staff</option>{staff.map(s => <option key={s.staffId} value={s.staffId}>{s.name} · {s.designation}</option>)}</select>{staff.length === 0 && assignment.streamId && <small className="field-hint">No staff are registered for this stream yet.</small>}</label><label className="field-label">Priority<select value={assignment.priority} onChange={e => setAssignment(v => ({ ...v, priority: e.target.value }))}>{['NORMAL', 'LOW', 'MEDIUM', 'HIGH', 'URGENT'].map(p => <option key={p}>{p}</option>)}</select></label><button className="button button-primary" disabled={busy || !assignment.staffId}>{busy ? 'Saving…' : 'Save assignment'} <span>→</span></button></form></section>}

        {isStaff && ['ASSIGNED', 'REOPENED'].includes(complaint.status) && <section className="section-card workflow-card"><span className="eyebrow">YOUR NEXT STEP</span><h2>Start working on this complaint</h2><p className="muted">Starting moves it to In progress and lets the student know it is being handled.</p><label className="field-label">Optional note<input value={remarks} onChange={e => setRemarks(e.target.value)} maxLength="2000" placeholder="Add a short update for the timeline" /></label><button disabled={busy} className="button button-primary" onClick={() => mutate(body => api.put(`/complaints/${id}/start`, body), { remarks })}>Start work <span>→</span></button></section>}

        {isStaff && complaint.status === 'IN_PROGRESS' && <section className="section-card workflow-card"><span className="eyebrow">SOLUTION</span><h2>Submit your solution</h2><p className="muted">Explain what was done. An administrator will review the solution before closing the case.</p><form className="form-stack" onSubmit={e => { e.preventDefault(); const body = new FormData(); body.append('solution', solution); body.append('remarks', remarks); if (proof) body.append('file', proof); mutate(payload => api.put(`/complaints/${id}/solution`, payload), body) }}><label className="field-label">Solution<textarea rows="5" value={solution} onChange={e => setSolution(e.target.value)} maxLength="10000" required placeholder="Describe the resolution or action taken…" /></label><label className="field-label">Update for the student <small>(optional)</small><input value={remarks} onChange={e => setRemarks(e.target.value)} maxLength="2000" placeholder="A short progress note" /></label><label className="field-label">Proof <small>(optional · up to 5 MB)</small><input type="file" accept="image/jpeg,image/png,image/webp,application/pdf,.jpg,.jpeg,.png,.webp,.pdf" onChange={e => setProof(e.target.files?.[0] || null)} /></label><button className="button button-primary" disabled={busy || !solution.trim()}>{busy ? 'Submitting…' : 'Submit solution'} <span>→</span></button></form></section>}

        {admin && complaint.status === 'SOLUTION_SUBMITTED' && <section className="section-card workflow-card"><span className="eyebrow">ADMIN VERIFICATION</span><h2>Review the submitted solution</h2><div className="solution-box"><b>Staff solution</b><p>{complaint.resolution}</p>{complaint.solutionProofName && <AttachmentButton complaintId={id} kind="solution" name={complaint.solutionProofName} />}</div><label className="field-label">Optional note for the history<input value={remarks} onChange={e => setRemarks(e.target.value)} maxLength="2000" placeholder="Add verification note" /></label><div className="workflow-actions"><button disabled={busy} className="button button-primary" onClick={() => mutate(body => api.put(`/complaints/${id}/verify`, body), { action: 'SOLVED', remarks })}>✓ Verify and solve</button><button disabled={busy} className="button button-secondary" onClick={() => mutate(body => api.put(`/complaints/${id}/verify`, body), { action: 'REOPENED', remarks })}>↺ Reopen for more work</button></div></section>}

        <section className="section-card history-card"><div className="section-heading compact"><div><span className="eyebrow">AUDIT TRAIL</span><h2>Complaint history</h2></div></div>{complaint.history?.length ? <div className="timeline">{complaint.history.map((event, index) => <article className="timeline-item" key={event.id}><div className={`timeline-marker ${index === complaint.history.length - 1 ? 'latest' : ''}`} /><div className="timeline-copy"><div className="timeline-title"><b>{event.action}</b><span>{formatDate(event.createdAt)}</span></div><p>{event.performedByName} <span className="role-tag">{event.role}</span>{event.newStatus && <> · <StatusBadge status={event.newStatus} /></>}</p>{event.remarks && <small>{event.remarks}</small>}</div></article>)}</div> : <p className="muted">No history is available yet.</p>}</section>
      </div>
      <aside className="detail-side">
        <section className="section-card info-card"><span className="eyebrow">CASE SUMMARY</span><h3>Complaint information</h3><dl><div><dt>Student</dt><dd>{user?.role === 'ROLE_STUDENT' ? 'You' : complaint.studentName}</dd></div><div><dt>Roll ID</dt><dd>{user?.role === 'ROLE_STUDENT' ? user.rollId : complaint.studentRollId}</dd></div><div><dt>Department</dt><dd>{user?.role === 'ROLE_STUDENT' ? 'Your department' : complaint.department}</dd></div><div><dt>Academic year</dt><dd>{complaint.academicYear}</dd></div><div><dt>Stream</dt><dd>{complaint.streamName || 'Pending classification'}</dd></div><div><dt>Assigned staff</dt><dd>{complaint.assignedStaffName || 'Not assigned'}</dd></div><div><dt>Priority</dt><dd><span className={`priority priority-${(complaint.priority || '').toLowerCase()}`}>{complaint.priority}</span></dd></div><div><dt>Last update</dt><dd>{formatDate(complaint.history?.at(-1)?.createdAt || complaint.createdDate)}</dd></div></dl></section>
        {complaint.resolution && complaint.status !== 'SOLUTION_SUBMITTED' && <section className="section-card resolution-card"><span className="eyebrow">STAFF RESPONSE</span><h3>Solution</h3><p>{complaint.resolution}</p>{complaint.solutionProofName && <AttachmentButton complaintId={id} kind="solution" name={complaint.solutionProofName} />}</section>}
        <div className="privacy-note"><span>⌑</span><p>Your complaint details are visible only to the college team assigned to handle them.</p></div>
      </aside>
    </div>
  </div>
}
