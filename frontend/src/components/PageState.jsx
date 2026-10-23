import { getShared, messageFromError } from '../services/api'
import { useEffect, useRef, useState } from 'react'

export function ErrorNotice({ error }) { return error ? <div role="alert" className="notice notice-error"><span>!</span><div>{messageFromError(error)}</div></div> : null }
export function SuccessNotice({ children }) { return children ? <div role="status" className="notice notice-success"><span>✓</span><div>{children}</div></div> : null }
export function Loading({ label = 'Loading…' }) { return <div className="loading-state"><span className="spinner" /> {label}</div> }
export function EmptyState({ icon = '✦', title, children, action }) { return <div className="empty-state"><div className="empty-icon">{icon}</div><h3>{title}</h3><p>{children}</p>{action}</div> }
export function formatDate(value) { return value ? new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—' }
export function formatDay(value) { return value ? new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(new Date(value)) : '—' }

function ImageViewer({ src, name, onClose }) {
  useEffect(() => {
    const previousOverflow = document.body.style.overflow
    const closeOnEscape = event => { if (event.key === 'Escape') onClose() }
    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', closeOnEscape)
    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', closeOnEscape)
    }
  }, [onClose])

  function downloadImage() {
    const link = document.createElement('a')
    link.href = src
    link.download = name || 'complaint-image'
    document.body.appendChild(link)
    link.click()
    link.remove()
  }

  return <div className="attachment-viewer-backdrop" role="presentation" onClick={onClose}>
    <section className="attachment-viewer" role="dialog" aria-modal="true" aria-label="Complaint image viewer" onClick={event => event.stopPropagation()}>
      <div className="attachment-viewer-heading"><b>{name || 'Complaint image'}</b></div>
      <div className="attachment-viewer-square"><img src={src} alt={name || 'Uploaded complaint image'} /></div>
      <div className="attachment-viewer-actions"><button type="button" className="button button-primary" onClick={downloadImage}>Download</button><button type="button" className="button button-secondary" onClick={onClose}>Close</button></div>
    </section>
  </div>
}

export function AttachmentButton({ complaintId, kind, name }) {
  const [busy, setBusy] = useState(false)
  const [preview, setPreview] = useState(null)
  const mounted = useRef(true)
  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])
  useEffect(() => { if (preview) return () => URL.revokeObjectURL(preview.url) }, [preview])
  async function openFile() {
    setBusy(true)
    try {
      const { data } = await getShared(`/complaints/${complaintId}/attachments/${kind}`, { responseType: 'blob' })
      const url = URL.createObjectURL(data)
      const isImage = data.type?.toLowerCase().startsWith('image/') || /\.(png|jpe?g|webp|gif|bmp|svg)$/i.test(name || '')
      if (isImage) {
        if (mounted.current) setPreview({ url, name })
        else URL.revokeObjectURL(url)
      } else {
        const a = document.createElement('a'); a.href = url; a.download = name || 'proof'; a.click()
        setTimeout(() => URL.revokeObjectURL(url), 60000)
      }
    } catch (error) { window.alert(messageFromError(error)) } finally { if (mounted.current) setBusy(false) }
  }
  return <>
    <button type="button" className="file-link" onClick={openFile} disabled={busy}>↗ {busy ? 'Opening…' : name || 'Open proof'}</button>
    {preview && <ImageViewer src={preview.url} name={preview.name} onClose={() => setPreview(null)} />}
  </>
}
