import { useEffect, useRef, useState } from 'react'
import api, { messageFromError } from '../services/api'

export default function EmailOtpVerification({ email, purpose, onVerificationChange, disabled = false }) {
  const [otp, setOtp] = useState('')
  const [challengeId, setChallengeId] = useState('')
  const [verificationToken, setVerificationToken] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)
  const [verifying, setVerifying] = useState(false)
  const callbackRef = useRef(onVerificationChange)
  callbackRef.current = onVerificationChange

  useEffect(() => {
    setOtp(''); setChallengeId(''); setVerificationToken(''); setMessage(''); setError('')
    callbackRef.current?.('')
  }, [email, purpose])

  async function sendOtp() {
    setError(''); setMessage(''); setOtp(''); setChallengeId(''); setVerificationToken('')
    callbackRef.current?.('')
    if (!email?.trim()) return setError('Enter an email address before requesting an OTP.')
    setSending(true)
    try {
      const { data } = await api.post('/otp/send', { email, purpose })
      if (!data?.challengeId) throw new Error('OTP request could not be started. Please try again.')
      setChallengeId(data.challengeId)
      setMessage(data.message || 'OTP sent to your email.')
    } catch (requestError) {
      setError(messageFromError(requestError))
    } finally {
      setSending(false)
    }
  }

  async function verifyOtp() {
    setError(''); setMessage('')
    if (!/^[0-9]{6}$/.test(otp)) return setError('OTP must contain exactly 6 digits.')
    setVerifying(true)
    try {
      const { data } = await api.post('/otp/verify', { email, otp, purpose, challengeId })
      if (!data?.verificationToken) throw new Error('Email verification could not be completed. Please try again.')
      setVerificationToken(data.verificationToken)
      callbackRef.current?.(data.verificationToken)
      setMessage('✓ Email verified')
    } catch (requestError) {
      setVerificationToken(''); callbackRef.current?.('')
      setError(messageFromError(requestError))
    } finally {
      setVerifying(false)
    }
  }

  return <div className="span-two otp-verification-block">
    <button type="button" className="button button-secondary otp-action" onClick={sendOtp}
      disabled={disabled || sending || verifying}>{sending ? 'Sending…' : challengeId ? 'Resend OTP' : 'Send OTP'}</button>
    {challengeId && !verificationToken && <div className="field-label otp-verification">OTP<div className="otp-action-row">
      <input aria-label="6-digit OTP" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}"
        maxLength="6" value={otp} onChange={event => { setOtp(event.target.value.replace(/\D/g, '').slice(0, 6)); setError('') }}
        disabled={verifying} required />
      <button type="button" className="button button-secondary otp-action" onClick={verifyOtp} disabled={verifying || sending}>
        {verifying ? 'Verifying…' : 'Verify OTP'}
      </button>
    </div></div>}
    {(message || error) && <div className={`otp-status ${error ? 'otp-status-error' : 'otp-status-success'}`} role="status">{error || message}</div>}
  </div>
}
