import { useAuth } from '../context/AuthContext'
import { Loading } from '../components/PageState'

export default function ProfilePage() {
  const { user: profile, loading } = useAuth()
  if (loading || !profile) return <Loading label="Loading profile…" />

  const roleName = profile.role.replace('ROLE_', '').toLowerCase()
  const idLabel = profile.role === 'ROLE_STUDENT' ? 'Student ID' : profile.role === 'ROLE_STAFF' ? 'Staff ID' : 'Admin ID'
  const fields = [
    [idLabel, profile.userId], ['Roll ID', profile.rollId], ['Full name', profile.name], ['Email', profile.email], ['Phone', profile.phone || 'Not provided'],
    ...(profile.role === 'ROLE_STUDENT' ? [['Department', profile.department], ['Academic year', profile.academicYear]] : []),
    ...(profile.role === 'ROLE_STAFF' ? [['Department', profile.department], ['Designation', profile.designation], ['Assigned stream', profile.streamName]] : []),
    ...(profile.role === 'ROLE_ADMIN' ? [['Designation', profile.designation]] : []),
  ]
  return <div><section className="welcome-row page-title-row"><div><span className="eyebrow">ACCOUNT</span><h1>Profile</h1><p className="muted">Your account information comes from your authenticated college account.</p></div></section><section className="section-card profile-card"><div className="profile-banner"><div className="profile-avatar">{profile.name?.slice(0, 1)?.toUpperCase() || 'U'}</div><div><h2>{profile.name}</h2><p>{roleName} account · {profile.rollId}</p></div></div><div className="profile-grid">{fields.map(([label, value]) => <div className="profile-field" key={label}><small>{label}</small><b>{value || '—'}</b></div>)}</div><div className="privacy-note"><span>⌑</span><p>Private credentials and tokens are never shown in your profile.</p></div></section></div>
}
