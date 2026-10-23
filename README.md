# College Student Grievance Management System

Full-stack complaint workflow using Spring Boot 4 / Java 17, MySQL and a separate React/Vite client.

## Local setup

1. Install Java 17, Node.js 20.19+ or 22.12+, npm, and MySQL 8. This checkout includes a Gradle wrapper; a separate Maven installation is not present in the environment used here.
2. Create the database with `mysql -u root -p < database/schema.sql`, or let Hibernate create/update it on first startup. Configure `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` if your local MySQL differs from the defaults.
3. Start the backend from the project root with `gradlew.bat bootRun` on Windows or `./gradlew bootRun` on macOS/Linux. The first startup inserts the default grievance streams, the current academic year, and a local bootstrap admin when those records are absent.
4. Start the frontend in a second terminal: `cd frontend`, `npm install`, `npm run dev`.
5. Open `http://localhost:5173`.

For a non-default API address, copy `frontend/.env.example` to `frontend/.env.local` and set `VITE_API_BASE_URL`.

The local bootstrap admin is `admin` / `password123` (email: `admin@webgenz.local`). The password is stored using BCrypt hashing. Override `BOOTSTRAP_ADMIN_ROLL_ID`, `BOOTSTRAP_ADMIN_NAME`, `BOOTSTRAP_ADMIN_EMAIL`, and `BOOTSTRAP_ADMIN_PASSWORD` before first startup. Set `BOOTSTRAP_ADMIN_ENABLED=false` in an environment where an administrator is provisioned another way. Set a private `JWT_SECRET` of at least 32 bytes for persistent sessions; without it the app creates a random key on startup and existing sessions expire when the app restarts. Uploaded proof is stored in `./uploads` by default; set `UPLOAD_DIR` to a private persistent directory.

For an existing demo database whose account passwords were previously encoded, apply `database/migrate_plaintext_demo_passwords.sql` once before signing in, for example with `mysql -u root -p complaint < database/migrate_plaintext_demo_passwords.sql`. It updates only encoded password values to the demo password `password`, including pending registration requests; account records and foreign keys remain in place. The registration table retains its legacy `password_hash` column name, but that field now stores plain text.

## Main routes

- `POST /api/auth/login`, `GET /api/auth/me`
- `POST /api/student-registrations`; admin review at `/api/student-registrations/{id}/approve` or `/reject`
- `POST /api/complaints`; workflow actions `/review`, `/assign`, `/start`, `/solution`, `/verify`
- `GET /api/complaints` supports status, year, stream, department, type, priority, staff, roll, name and date filters; student and staff results are scoped by the authenticated account
- `GET /api/streams`, `GET /api/academic-years`; admin can create/update reference data and manage staff

Only approved student requests become accounts. Student, staff, and administrator passwords are stored and compared as plain text for this college demo. JWT authentication remains enabled, and the API enforces role, ownership, staff assignment, and workflow transitions; the React route guards are only a navigation aid.

## Configuration notes

The project keeps Hibernate `ddl-auto=update` to make the supplied source usable on an existing local database. `database/schema.sql` is the corresponding clean-install schema. For a production rollout, apply reviewed versioned migrations and set Hibernate to `validate` after adopting the schema.
