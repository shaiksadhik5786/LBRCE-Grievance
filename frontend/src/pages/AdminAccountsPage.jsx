import { useCallback, useEffect, useState } from 'react'
import { getShared, messageFromError } from '../services/api'
import { ErrorNotice, Loading } from '../components/PageState'

export default function AdminAccountsPage() {
  const [admins, setAdmins] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await getShared('/admins')
      setAdmins(data)
    } catch (requestError) {
      setError({ message: messageFromError(requestError) })
    } finally {
      setLoading(false)
    }
  }, [])
  useEffect(() => { void load() }, [load])

  const query = search.trim().toLowerCase()
  const visibleAdmins = admins.filter(admin => [admin.name, admin.rollId, admin.email, admin.designation]
    .filter(Boolean).join(' ').toLowerCase().includes(query))

  return <div>
    <section className="welcome-row page-title-row"><div><span className="eyebrow">ADMINISTRATION</span><h1>Administrators</h1><p className="muted">View administrator accounts. New administrator accounts are provisioned by the college system administrator.</p></div><span className="count-chip">{admins.length} administrators</span></section>
    <ErrorNotice error={error} />
    <section className="section-card list-card">
      {loading ? <Loading /> : admins.length ? <>
        <div className="directory-filters directory-filters-single"><label className="field-label">Search administrators<input type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Name, roll ID, email or designation" /></label></div>
        <div className="list-summary"><span>Showing {visibleAdmins.length} of {admins.length} administrators</span></div>
        {visibleAdmins.length ? <div className="table-scroll"><table><thead><tr><th>Administrator</th><th>Email</th><th>Phone</th></tr></thead><tbody>{visibleAdmins.map(admin => <tr key={admin.adminId}><td><b>{admin.name}</b><small>{admin.rollId} · {admin.designation}</small></td><td>{admin.email}</td><td>{admin.phone || '—'}</td></tr>)}</tbody></table></div>
          : <div className="empty-state"><h3>No matching administrators</h3><p>Try another search.</p></div>}
      </> : <div className="empty-state"><h3>No administrators found</h3></div>}
    </section>
  </div>
}
