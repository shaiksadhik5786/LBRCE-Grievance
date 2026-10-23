import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { dashboardPathForRole, useAuth } from '../context/AuthContext'
import api, { messageFromError } from '../services/api'
import PasswordField from '../components/PasswordField'
import lbrceLogo from '../assets/lbrce-logo.jpg'

export default function LoginPage() {
  const { user, role, isAuthenticated, initialized, notice, clearNotice, signIn, completeOtpSignIn, signOut } = useAuth()
  const navigate = useNavigate()
  const [rollId, setRollId] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [loginOtp, setLoginOtp] = useState('')
  const [otpStep, setOtpStep] = useState(null)
  const mounted = useRef(true)
  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])

  if (!initialized) return <div className="screen-loader"><span className="spinner" /> Loading your workspace…</div>
  async function submit(e) {
    e.preventDefault(); setBusy(true); setError(''); clearNotice()
    let navigated = false
    try {
      const result = await signIn({ rollId: rollId.trim(), password })
      if (!mounted.current) return
      if (result.otpRequired) {
        setOtpStep(result); setBusy(false); return
      }
      setPassword('')
      navigated = true
      navigate(result.destination)
    } catch (err) { if (mounted.current) setError(messageFromError(err)) }
    finally { if (mounted.current && !navigated) setBusy(false) }
  }
  async function resendLoginOtp() {
    setBusy(true); setError(''); setLoginOtp('')
    try {
      const result = await signIn({ rollId: rollId.trim(), password })
      if (!result.otpRequired) throw new Error('OTP verification is not required for this account.')
      if (mounted.current) setOtpStep(result)
    } catch (err) { if (mounted.current) setError(messageFromError(err)) }
    finally { if (mounted.current) setBusy(false) }
  }
  async function verifyLoginOtp(e) {
    e.preventDefault(); setError('')
    if (!/^[0-9]{6}$/.test(loginOtp)) return setError('OTP must contain exactly 6 digits.')
    setBusy(true)
    try {
      const destination = await completeOtpSignIn({ challengeId: otpStep.challengeId, otp: loginOtp, purpose: otpStep.purpose })
      if (!mounted.current) return
      navigate(destination)
    } catch (err) { if (mounted.current) setError(messageFromError(err)) }
    finally { if (mounted.current) setBusy(false) }
  }

  if (isAuthenticated && user && !busy) {
    const destination = dashboardPathForRole(role)
    return <div className="auth-page register-page"><section className="register-card">
      <div className="auth-mobile-brand"><img src={lbrceLogo} alt="" /><span>LBRCE</span></div>
      <span className="eyebrow">SESSION ACTIVE</span><h1>You’re already signed in</h1>
      <p className="muted">Signed in as {user.name || user.rollId}. You can return to your workspace or sign out to use another account.</p>
      <div className="form-actions">
        <button type="button" className="button button-secondary" onClick={() => navigate(destination, { replace: true })}>Return to workspace</button>
        <button type="button" className="button button-primary" onClick={() => { signOut(); setPassword(''); setError('') }}>Sign out to login</button>
      </div>
    </section></div>
  }

  return <div className="auth-page">
    <div className="auth-art"><div className="auth-art-inner"><img className="auth-logo-image" src={lbrceLogo} alt="Lakireddy Bali Reddy College of Engineering logo" /><span className="eyebrow light">LAKIREDDY BALI REDDY COLLEGE OF ENGINEERING</span><h1>A better campus<br /><em>starts with listening.</em></h1><p>Raise a concern, track its progress, and help make your college community better for everyone.</p><div className="art-bottom"><div className="art-stats"><b>One portal</b><span>for every concern</span></div><div className="art-divider" /><div className="art-stats"><b>Clear updates</b><span>at every step</span></div></div><div className="art-orb orb-one" /><div className="art-orb orb-two" /></div></div>
    <div className="auth-panel"><div className="auth-panel-inner"><div className="auth-mobile-brand"><img src={lbrceLogo} alt="" /><span>LBRCE</span></div><span className="eyebrow">WELCOME BACK</span><h2>Sign in to your account</h2><p className="muted auth-intro">Use your college roll ID to continue to your workspace.</p>
      {(error || notice) && <div role="alert" className="notice notice-error"><span>!</span><div>{error || notice}</div></div>}
      {!otpStep ? <form onSubmit={submit} className="form-stack">
        <label className="field-label">Roll ID<input autoComplete="username" value={rollId} onChange={e => setRollId(e.target.value)} required /></label>
        <PasswordField label="Password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button className="button button-primary button-full" disabled={busy}>{busy ? <><span className="spinner spinner-light" /> Signing in…</> : <>Sign in <span>→</span></>}</button>
      </form> : <form onSubmit={verifyLoginOtp} className="form-stack">
        <p className="muted">An OTP was sent to {otpStep.maskedEmail}. Enter it to finish signing in.</p>
        <label className="field-label">OTP<input aria-label="6-digit login OTP" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength="6" value={loginOtp} onChange={e => setLoginOtp(e.target.value.replace(/\D/g, '').slice(0, 6))} required /></label>
        <button className="button button-primary button-full" disabled={busy}>{busy ? <><span className="spinner spinner-light" /> Verifying…</> : <>Verify OTP <span>→</span></>}</button>
        <button type="button" className="text-button" disabled={busy} onClick={resendLoginOtp}>Resend OTP</button>
        <button type="button" className="button button-secondary button-full" disabled={busy} onClick={() => { setOtpStep(null); setLoginOtp(''); setError('') }}>Use different credentials</button>
      </form>}
      <div className="auth-divider"><span /> <small>NEW TO CAMPUSCARE?</small> <span /></div>
      <Link to="/student/register" className="button button-secondary button-full">Create student login <span>↗</span></Link>
      <div className="auth-footnote">Your account role is verified securely by the college system.</div>
    </div></div>
  </div>
}
