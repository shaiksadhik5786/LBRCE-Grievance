import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import api, { getShared, messageFromError } from '../services/api'
import { ErrorNotice, Loading } from '../components/PageState'
import { useAuth } from '../context/AuthContext'

const empty = { subject: '', description: '' }

export default function ComplaintFormPage() {
  const { id } = useParams()
  const editing = Boolean(id)
  const navigate = useNavigate()
  const { invalidateComplaints } = useAuth()
  const [form, setForm] = useState(empty)
  const [file, setFile] = useState(null)
  const [existingFile, setExistingFile] = useState('')
  const [removeAttachment, setRemoveAttachment] = useState(false)
  const [loading, setLoading] = useState(editing)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const mounted = useRef(false)
  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])
  useEffect(() => {
    if (!editing) return
    let active = true
    setLoading(true)
    setError(null)
    getShared(`/complaints/${id}`).then(({ data }) => {
      if (!active) return
      if (data.status !== 'REGISTERED') { navigate(`/complaints/${id}`, { replace: true }); return }
      setForm({ subject: data.subject, description: data.description }); setExistingFile(data.attachmentName || '')
    }).catch(err => { if (active) setError(err) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [editing, id, navigate])
  function update(e) { setForm(f => ({ ...f, [e.target.name]: e.target.value })) }
  function selectFile(e) { const chosen = e.target.files?.[0]; setFile(chosen || null); setRemoveAttachment(false); if (chosen && chosen.size > 5 * 1024 * 1024) { setFile(null); e.target.value = ''; setError({ message: 'Proof files must be 5 MB or smaller.' }) } else setError(null) }
  async function submit(e) {
    e.preventDefault(); setBusy(true); setError(null)
    const body = new FormData()
    Object.entries(form).forEach(([key, value]) => body.append(key, value))
    if (file) body.append('file', file)
    if (removeAttachment) body.append('removeAttachment', 'true')
    try {
      if (editing) await api.put(`/complaints/${id}`, body)
      else await api.post('/complaints', body)
      invalidateComplaints()
      if (mounted.current) navigate(editing ? `/complaints/${id}` : '/complaints', { state: { notice: editing ? 'Complaint updated.' : 'Your complaint was submitted and registered.' } })
    } catch (err) { if (mounted.current) setError({ message: messageFromError(err) }) }
    finally { if (mounted.current) setBusy(false) }
  }
  if (loading) return <Loading />
  return <div className="form-page"><Link to={editing ? `/complaints/${id}` : '/complaints'} className="back-link">← {editing ? 'Back to complaint' : 'Back to my complaints'}</Link>
    <section className="form-hero"><span className="eyebrow">{editing ? 'UPDATE YOUR CONCERN' : 'STUDENT SUPPORT'}</span><h1>{editing ? 'Edit complaint' : 'Raise a concern'}</h1><p className="muted">{editing ? 'You can make changes until an administrator accepts your complaint.' : 'Share what happened. Your college team will review the details and follow up.'}</p></section>
    <ErrorNotice error={error} />
    <form className="section-card complaint-form" onSubmit={submit}>
      <div className="form-card-title"><span className="number-badge">01</span><div><b>Complaint details</b><small>Only the information below is needed to register your concern.</small></div></div>
      <label className="field-label">Subject<input name="subject" value={form.subject} onChange={update} maxLength="200" placeholder="A short summary of your concern" required /></label>
      <label className="field-label">Description<textarea name="description" value={form.description} onChange={update} maxLength="10000" rows="7" placeholder="Add the details that will help the college team understand your concern…" required /><small className="field-hint">{form.description.length} / 10,000 characters</small></label>
      <div className="field-label">Photo or proof <small>(optional · up to 5 MB)</small><label className="upload-box"><span className="upload-icon">↑</span><span><b>{file?.name || (existingFile && !removeAttachment ? existingFile : 'Choose a file or drag it here')}</b><small>JPG, PNG, WebP or PDF</small></span><input type="file" accept="image/jpeg,image/png,image/webp,application/pdf,.jpg,.jpeg,.png,.webp,.pdf" onChange={selectFile} />{existingFile && !file && <button type="button" className="text-button upload-clear" onClick={(e) => { e.preventDefault(); setRemoveAttachment(v => !v) }}>{removeAttachment ? 'Keep existing' : 'Remove'}</button>}</label></div>
      <div className="workflow-tip"><span>ⓘ</span><p>Your complaint starts as <b>Registered</b>. You can edit it while it awaits admin review. Once accepted, the details are locked.</p></div>
      <div className="form-actions"><Link className="button button-secondary" to={editing ? `/complaints/${id}` : '/complaints'}>Cancel</Link><button className="button button-primary" disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : 'Submit complaint'} <span>→</span></button></div>
    </form>
  </div>
}
