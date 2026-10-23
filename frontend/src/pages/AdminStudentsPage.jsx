import { useEffect, useState } from 'react'
import { getShared, messageFromError } from '../services/api'
import { ErrorNotice, Loading } from '../components/PageState'

export default function AdminStudentsPage() {
  const [students, setStudents] = useState([])
  const [years, setYears] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [departmentFilter, setDepartmentFilter] = useState('')
  const [yearFilter, setYearFilter] = useState('')

  useEffect(() => {
    let active = true
    Promise.all([getShared('/students'), getShared('/academic-years')])
      .then(([studentResponse, yearResponse]) => {
        if (active) {
          setStudents(studentResponse.data)
          setYears(Object.fromEntries(yearResponse.data.map(item => [item.academicYearId, item.yearName])))
        }
      })
      .catch(err => { if (active) setError({ message: messageFromError(err) }) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  const departments = [...new Set(students.map(student => student.department).filter(Boolean))].sort((a, b) => a.localeCompare(b))
  const academicYears = [...new Map(students.map(student => [String(student.academicYearId), {
    id: String(student.academicYearId),
    name: years[student.academicYearId] || String(student.academicYearId),
  }])).values()].sort((a, b) => a.name.localeCompare(b.name))
  const query = search.trim().toLowerCase()
  const visibleStudents = students.filter(student => {
    const searchable = [student.name, student.rollId, student.department, student.email, student.phone, years[student.academicYearId]]
      .filter(Boolean).join(' ').toLowerCase()
    return (!query || searchable.includes(query))
      && (!departmentFilter || student.department === departmentFilter)
      && (!yearFilter || String(student.academicYearId) === yearFilter)
  })

  return <div>
    <section className="welcome-row page-title-row"><div><span className="eyebrow">COLLEGE DIRECTORY</span><h1>Students</h1><p className="muted">Active student accounts approved by an administrator.</p></div><span className="count-chip">{students.length} students</span></section>
    <ErrorNotice error={error} />
    <section className="section-card list-card">
      {loading ? <Loading /> : students.length ? <>
        <div className="directory-filters"><label className="field-label">Search students<input type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Name, roll ID, email or phone" /></label>
          <label className="field-label">Department<select value={departmentFilter} onChange={e => setDepartmentFilter(e.target.value)}><option value="">All departments</option>{departments.map(department => <option key={department} value={department}>{department}</option>)}</select></label>
          <label className="field-label">Academic year<select value={yearFilter} onChange={e => setYearFilter(e.target.value)}><option value="">All years</option>{academicYears.map(year => <option key={year.id} value={year.id}>{year.name}</option>)}</select></label>
        </div>
        <div className="list-summary"><span>Showing {visibleStudents.length} of {students.length} students</span><button type="button" className="text-button" onClick={() => { setSearch(''); setDepartmentFilter(''); setYearFilter('') }}>Clear filters</button></div>
        {visibleStudents.length ? <div className="table-scroll"><table><thead><tr><th>Student</th><th>Roll ID</th><th>Department</th><th>Academic year</th><th>Contact</th></tr></thead><tbody>{visibleStudents.map(student => <tr key={student.studentId}><td><b>{student.name}</b></td><td>{student.rollId}</td><td>{student.department}</td><td>{years[student.academicYearId] || student.academicYearId}</td><td>{student.email}<small>{student.phone || '—'}</small></td></tr>)}</tbody></table></div>
          : <div className="empty-state"><h3>No matching students</h3><p>Try changing the search or filters.</p></div>}
      </> : <div className="empty-state"><h3>No students yet</h3><p>Approved student logins will be listed here.</p></div>}
    </section>
  </div>
}
