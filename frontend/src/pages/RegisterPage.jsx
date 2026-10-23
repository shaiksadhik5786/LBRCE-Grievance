import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import api, { getShared, messageFromError } from '../services/api'
import { ErrorNotice, SuccessNotice } from '../components/PageState'
import PasswordField from '../components/PasswordField'
import lbrceLogo from '../assets/lbrce-logo.jpg'
import { DEPARTMENTS } from '../constants/departments'
import EmailOtpVerification from '../components/EmailOtpVerification'

const initial = { rollId: '', name: '', academicYearId: '', department: '', email: '', phone: '', password: '', confirmPassword: '' }
const PHONE_ERROR = 'Phone number must contain exactly 10 digits.'

export default function RegisterPage() {
  const [form, setForm] = useState(initial)
  const [years, setYears] = useState([])
  const [yearsLoading, setYearsLoading] = useState(true)
  const [yearsError, setYearsError] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [busy, setBusy] = useState(false)
  const [verificationToken, setVerificationToken] = useState('')
  const navigate = useNavigate()
  const redirectTimer = useRef(null)
  const mounted = useRef(false)
  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])
  useEffect(() => {
    let active = true
    getShared('/academic-years').then(({ data }) => { if (active) setYears(data.filter(year => year.isActive === 'Y')) })
      .catch(err => { if (active) setYearsError({ message: messageFromError(err) }) })
      .finally(() => { if (active) setYearsLoading(false) })
    return () => { active = false }
  }, [])
  useEffect(() => () => window.clearTimeout(redirectTimer.current), [])
  function update(e) {
    if (e.target.name === 'phone') e.target.setCustomValidity('')
    if (e.target.name === 'email') {
      setVerificationToken('')
    }
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  }
  async function submit(e) {
    e.preventDefault(); setError(''); setSuccess(false)
    if (form.password !== form.confirmPassword) return setError('Your passwords do not match.')
    if (!verificationToken) return setError('Please verify your email with OTP before completing registration.')
    setBusy(true)
    try {
      const { data } = await api.post('/student-registrations', { ...form, emailVerificationToken: verificationToken })
      if (mounted.current) { setSuccess(`Request submitted with Roll ID ${data.rollId}. Save this Roll ID for sign-in after administrator approval.`); redirectTimer.current = window.setTimeout(() => navigate('/login'), 6000) }
    } catch (err) { if (mounted.current) setError(messageFromError(err)) }
    finally { if (mounted.current) setBusy(false) }
  }
  return <div className="auth-page register-page"><div className="register-card">
    <Link className="back-link" to="/login">← Back to sign in</Link>
    <div className="auth-mobile-brand"><img src={lbrceLogo} alt="" /><span>LBRCE</span></div>
    <span className="eyebrow">STUDENT ACCOUNT</span><h1>Create your login</h1><p className="muted">Your details will be reviewed by a college administrator before your account is activated.</p>
    <ErrorNotice error={yearsError} />
    <ErrorNotice error={error} /><SuccessNotice>{success}</SuccessNotice>
    <form onSubmit={submit} className="form-grid register-form">
      <label className="field-label span-two">Roll ID<input name="rollId" value={form.rollId} onChange={update} maxLength="20" required /></label>
      <label className="field-label span-two">Full name<input name="name" value={form.name} onChange={update} maxLength="100" required /></label>
      <label className="field-label">Academic year<select name="academicYearId" value={form.academicYearId} onChange={update} required disabled={yearsLoading}><option value="">{yearsLoading ? 'Loading academic years…' : 'Choose active year'}</option>{years.map(y => <option key={y.academicYearId} value={y.academicYearId}>{y.yearName}</option>)}</select>{!yearsLoading && years.length === 0 && <small className="field-hint">No active academic year is available. Contact the college office.</small>}</label>
      <label className="field-label">Department<select name="department" value={form.department} onChange={update} required><option value="">Choose department</option>{DEPARTMENTS.map(department => <option key={department} value={department}>{department}</option>)}</select></label>
      <label className="field-label">College email<input name="email" type="email" value={form.email} onChange={update} maxLength="100" required /></label>
      <label className="field-label">Phone Number<input name="phone" type="tel" inputMode="numeric" pattern="[0-9]{10}" maxLength="10" title={PHONE_ERROR} onInvalid={e => e.currentTarget.setCustomValidity(PHONE_ERROR)} onInput={e => e.currentTarget.setCustomValidity('')} value={form.phone} onChange={update} required /></label>
      <EmailOtpVerification email={form.email} purpose="STUDENT_REGISTRATION" onVerificationChange={setVerificationToken} disabled={busy} />
      <PasswordField label="Password" name="password" value={form.password} onChange={update} minLength="8" maxLength="72" autoComplete="new-password" required />
      <PasswordField label="Confirm password" name="confirmPassword" value={form.confirmPassword} onChange={update} autoComplete="new-password" required />
      {!verificationToken && <p className="field-hint span-two">Verify your college email before submitting the registration request.</p>}
      <button disabled={busy || yearsLoading || years.length === 0 || !verificationToken} className="button button-primary span-two">{busy ? 'Registering…' : 'Submit registration request'} <span>→</span></button>
    </form>
    <p className="small-print">By submitting, you confirm these are your correct college records. Never share your password with anyone.</p>
  </div></div>
}
