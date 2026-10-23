package com.example.demo.config;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.AcademicYear;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Stream;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.StreamRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.StudentRegistrationRepository;

@Configuration
public class DevelopmentDataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DevelopmentDataInitializer.class);
    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private final StreamRepository streams;
    private final AcademicYearRepository years;
    private final AdminRepository admins;
    private final StudentRepository students;
    private final StaffRepository staff;
    private final StudentRegistrationRepository registrations;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.bootstrap.admin-enabled:true}") private boolean bootstrapAdmin;
    @Value("${app.bootstrap.admin-only:false}") private boolean adminOnly;
    @Value("${app.bootstrap.admin-roll-id}") private String adminRollId;
    @Value("${app.bootstrap.admin-name}") private String adminName;
    @Value("${app.bootstrap.admin-email}") private String adminEmail;
    @Value("${app.bootstrap.admin-password:}") private String adminPassword;

    public DevelopmentDataInitializer(StreamRepository streams, AcademicYearRepository years, AdminRepository admins,
            StudentRepository students, StaffRepository staff, StudentRegistrationRepository registrations,
            PasswordEncoder passwordEncoder) {
        this.streams = streams; this.years = years; this.admins = admins; this.students = students; this.staff = staff;
        this.registrations = registrations; this.passwordEncoder = passwordEncoder;
    }

    @Override public void run(String... args) {
        if (!adminOnly) {
            migrateLegacyPasswords();
            if (streams.count() == 0) {
                for (String name : List.of("Academic", "Faculty & Teaching", "Fees & Finance", "Infrastructure", "Ragging & Anti-Ragging", "Student Welfare & Discipline", "Others")) {
                    Stream stream = new Stream(); stream.setStreamName(name); stream.setDescription("Complaints related to " + name.toLowerCase()); streams.save(stream);
                }
            }
            if (years.count() == 0) {
                LocalDate today = LocalDate.now(); int startYear = today.getMonthValue() >= 6 ? today.getYear() : today.getYear() - 1;
                AcademicYear year = new AcademicYear(); year.setYearName(String.format("%02d-%02d", startYear % 100, (startYear + 1) % 100));
                year.setStartDate(LocalDate.of(startYear, 6, 1)); year.setEndDate(LocalDate.of(startYear + 1, 5, 31)); year.setIsActive("Y"); years.save(year);
            }
        }
        initializeBootstrapAdmin();
    }

    private void initializeBootstrapAdmin() {
        if (!bootstrapAdmin) return;
        if (students.existsByRollId(adminRollId) || staff.existsByRollId(adminRollId))
            throw new IllegalStateException("Bootstrap admin Roll ID is already used by a student or staff account.");
        if (students.existsByEmail(adminEmail) || staff.existsByEmail(adminEmail))
            throw new IllegalStateException("Bootstrap admin email is already used by a student or staff account.");

        Admin admin = admins.findByRollId(adminRollId).orElse(null);
        Admin emailOwner = admins.findByEmail(adminEmail).orElse(null);
        if (emailOwner != null && (admin == null || !Objects.equals(emailOwner.getAdminId(), admin.getAdminId())))
            throw new IllegalStateException("Bootstrap admin email is already assigned to another admin account.");
        if (admin == null && (adminPassword == null || adminPassword.isBlank()))
            throw new IllegalStateException("Set BOOTSTRAP_ADMIN_PASSWORD to initialize the development admin account.");

        boolean changed = false;
        if (admin == null) {
            admin = new Admin();
            admin.setRollId(adminRollId);
            admin.setDesignation("Administrator");
            changed = true;
        }
        if (!Objects.equals(admin.getName(), adminName)) { admin.setName(adminName); changed = true; }
        if (!Objects.equals(admin.getEmail(), adminEmail)) { admin.setEmail(adminEmail); changed = true; }
        if (adminPassword != null && !adminPassword.isBlank()
                && (needsMigration(admin.getPassword()) || !passwordEncoder.matches(adminPassword, admin.getPassword()))) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            changed = true;
        }
        if (changed) admins.save(admin);

        Admin persisted = admins.findByRollId(adminRollId)
                .orElseThrow(() -> new IllegalStateException("Bootstrap admin was not persisted."));
        if (adminPassword != null && !adminPassword.isBlank()
                && (!BCRYPT_HASH.matcher(persisted.getPassword()).matches()
                        || !passwordEncoder.matches(adminPassword, persisted.getPassword())))
            throw new IllegalStateException("Bootstrap admin password was not persisted as a matching BCrypt hash.");
        logger.info("Bootstrap admin initialized for Roll ID {}.", adminRollId);
    }

    private void migrateLegacyPasswords() {
        var migratedAdmins = admins.findAll().stream().filter(account -> needsMigration(account.getPassword())).toList();
        migratedAdmins.forEach(account -> account.setPassword(passwordEncoder.encode(account.getPassword())));
        if (!migratedAdmins.isEmpty()) admins.saveAll(migratedAdmins);

        var migratedStaff = staff.findAll().stream().filter(account -> needsMigration(account.getPassword())).toList();
        migratedStaff.forEach(account -> account.setPassword(passwordEncoder.encode(account.getPassword())));
        if (!migratedStaff.isEmpty()) staff.saveAll(migratedStaff);

        var migratedStudents = students.findAll().stream().filter(account -> needsMigration(account.getPassword())).toList();
        migratedStudents.forEach(account -> account.setPassword(passwordEncoder.encode(account.getPassword())));
        if (!migratedStudents.isEmpty()) students.saveAll(migratedStudents);

        var migratedRegistrations = registrations.findAll().stream()
                .filter(request -> needsMigration(request.getPassword())).toList();
        migratedRegistrations.forEach(request -> request.setPassword(passwordEncoder.encode(request.getPassword())));
        if (!migratedRegistrations.isEmpty()) registrations.saveAll(migratedRegistrations);
    }

    private boolean needsMigration(String storedPassword) {
        return storedPassword != null && !BCRYPT_HASH.matcher(storedPassword).matches();
    }
}
