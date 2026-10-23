-- Read-only audit. Legacy plaintext passwords are migrated automatically by
-- DevelopmentDataInitializer using the application's BCryptPasswordEncoder.
-- It encodes only values that are not already complete BCrypt hashes and
-- preserves the credentials users enter. Do not replace this with a SQL
-- UPDATE: BCrypt must be calculated by the application, not stored as a fixed
-- or shared SQL value.

SELECT 'admin' AS account_type,
       SUM(password NOT REGEXP '^\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}$') AS legacy_password_count
FROM admin
UNION ALL
SELECT 'staff', SUM(password NOT REGEXP '^\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}$') FROM staff
UNION ALL
SELECT 'student', SUM(password NOT REGEXP '^\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}$') FROM student
UNION ALL
SELECT 'registration_request', SUM(password_hash NOT REGEXP '^\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}$')
FROM student_registration_request;
