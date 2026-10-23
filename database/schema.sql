CREATE DATABASE IF NOT EXISTS complaint CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE complaint;

CREATE TABLE IF NOT EXISTS academic_year (
  academic_year_id INT AUTO_INCREMENT PRIMARY KEY,
  year_name VARCHAR(20) NOT NULL UNIQUE,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  is_active CHAR(1) NOT NULL DEFAULT 'N',
  CHECK (is_active IN ('Y','N'))
);

CREATE TABLE IF NOT EXISTS stream (
  stream_id INT AUTO_INCREMENT PRIMARY KEY,
  stream_name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS student (
  student_id INT AUTO_INCREMENT PRIMARY KEY,
  roll_id VARCHAR(20) NOT NULL UNIQUE,
  academic_year_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  department VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15),
  password VARCHAR(255) NOT NULL,
  INDEX idx_student_name (name),
  INDEX idx_student_department (department),
  INDEX idx_student_academic_year (academic_year_id),
  CONSTRAINT fk_student_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_year(academic_year_id)
);

CREATE TABLE IF NOT EXISTS staff (
  staff_id INT AUTO_INCREMENT PRIMARY KEY,
  roll_id VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15),
  designation VARCHAR(100) NOT NULL,
  department VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  stream_id INT NOT NULL,
  CONSTRAINT fk_staff_stream FOREIGN KEY (stream_id) REFERENCES stream(stream_id)
);

CREATE TABLE IF NOT EXISTS admin (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  roll_id VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15),
  designation VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS student_registration_request (
  id INT AUTO_INCREMENT PRIMARY KEY,
  roll_id VARCHAR(20) NOT NULL UNIQUE,
  academic_year_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  department VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15),
  -- Legacy column name retained for compatibility; stores the submitted password as a BCrypt hash.
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  rejection_reason VARCHAR(500),
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_registration_status (status),
  INDEX idx_registration_roll_id (roll_id),
  CONSTRAINT fk_registration_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_year(academic_year_id)
);

CREATE TABLE IF NOT EXISTS complaint (
  complaint_id INT AUTO_INCREMENT PRIMARY KEY,
  academic_year_id INT NOT NULL,
  student_id INT NOT NULL,
  complaint_type VARCHAR(100) NOT NULL,
  subject VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  image_name VARCHAR(255),
  image_path VARCHAR(500),
  image_type VARCHAR(50),
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_complaint_academic_year_created (academic_year_id, created_date),
  INDEX idx_complaint_student_created (student_id, created_date),
  INDEX idx_complaint_type (complaint_type),
  CONSTRAINT fk_complaint_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_year(academic_year_id),
  CONSTRAINT fk_complaint_student FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE IF NOT EXISTS registered_complaint (
  registered_complaint_id INT AUTO_INCREMENT PRIMARY KEY,
  complaint_id INT NOT NULL UNIQUE,
  student_id INT NOT NULL,
  admin_id INT NULL,
  stream_id INT,
  assigned_staff_id INT,
  registered_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  assigned_date DATETIME,
  priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  registration_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
  staff_remarks TEXT,
  resolution TEXT,
  rejection_reason VARCHAR(1000),
  solution_proof_name VARCHAR(255),
  solution_proof_path VARCHAR(500),
  resolved_date DATETIME,
  verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  verification_date DATETIME,
  INDEX idx_rc_status (registration_status),
  INDEX idx_rc_stream (stream_id),
  INDEX idx_rc_staff (assigned_staff_id),
  INDEX idx_rc_registered_date (registered_date),
  INDEX idx_rc_verification (verification_status),
  INDEX idx_rc_priority (priority),
  CONSTRAINT fk_rc_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(complaint_id),
  CONSTRAINT fk_rc_student FOREIGN KEY (student_id) REFERENCES student(student_id),
  CONSTRAINT fk_rc_admin FOREIGN KEY (admin_id) REFERENCES admin(admin_id),
  CONSTRAINT fk_rc_stream FOREIGN KEY (stream_id) REFERENCES stream(stream_id),
  CONSTRAINT fk_rc_staff FOREIGN KEY (assigned_staff_id) REFERENCES staff(staff_id)
);

CREATE TABLE IF NOT EXISTS complaint_history (
  history_id INT AUTO_INCREMENT PRIMARY KEY,
  registered_complaint_id INT NOT NULL,
  previous_status VARCHAR(30),
  new_status VARCHAR(30) NOT NULL,
  action VARCHAR(80) NOT NULL,
  performed_by_id INT NOT NULL,
  performed_by_name VARCHAR(120) NOT NULL,
  role VARCHAR(20) NOT NULL,
  remarks TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_history_complaint_time (registered_complaint_id, created_at),
  CONSTRAINT fk_history_complaint FOREIGN KEY (registered_complaint_id) REFERENCES registered_complaint(registered_complaint_id)
);
