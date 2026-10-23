-- Narrow, data-preserving migration: Roll ID remains unchanged.
USE complaint;

ALTER TABLE student DROP INDEX IF EXISTS idx_student_roll_number;
ALTER TABLE student DROP INDEX IF EXISTS uq_student_roll_year;
ALTER TABLE student DROP COLUMN IF EXISTS roll_number;
ALTER TABLE student_registration_request DROP COLUMN IF EXISTS roll_number;
