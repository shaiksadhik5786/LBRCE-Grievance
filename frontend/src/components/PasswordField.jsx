import { useId, useState } from 'react'

export default function PasswordField({ label, id, className = '', inputClassName = '', ...inputProps }) {
  const generatedId = useId()
  const inputId = id || generatedId
  const [visible, setVisible] = useState(false)

  return <div className={`field-label ${className}`.trim()}>
    <label htmlFor={inputId}>{label}</label>
    <span className="password-input-wrap">
      <input {...inputProps} id={inputId} className={`password-field-input ${inputClassName}`.trim()} type={visible ? 'text' : 'password'} />
      <button
        className="password-toggle"
        type="button"
        aria-label={visible ? 'Hide password' : 'Show password'}
        aria-pressed={visible}
        aria-controls={inputId}
        onClick={() => setVisible(value => !value)}
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          {visible
            ? <><path d="M2.2 12s3.5-6.2 9.8-6.2 9.8 6.2 9.8 6.2-3.5 6.2-9.8 6.2S2.2 12 2.2 12Z" /><circle cx="12" cy="12" r="2.7" /></>
            : <><path d="M3 3l18 18" /><path d="M10.6 5.9A10.7 10.7 0 0 1 12 5.8c6.3 0 9.8 6.2 9.8 6.2a16 16 0 0 1-3.1 3.7M6.2 6.9C3.6 8.5 2.2 12 2.2 12s3.5 6.2 9.8 6.2c1.1 0 2.1-.2 3-.5" /><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" /></>}
        </svg>
      </button>
    </span>
  </div>
}
