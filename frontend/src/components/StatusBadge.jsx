export default function StatusBadge({ status }) {
  const value = status || 'UNKNOWN'
  return <span className={`status-pill status-${value.toLowerCase().replaceAll('_', '-')}`}>{value.replaceAll('_', ' ')}</span>
}
