package com.example.demo.securityConfig;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.*;
import com.example.demo.repository.*;

@Service
public class CurrentUserService {
    private final StudentRepository students;
    private final StaffRepository staff;
    private final AdminRepository admins;
    private final AcademicYearRepository years;
    private final StreamRepository streams;

    public CurrentUserService(StudentRepository students, StaffRepository staff, AdminRepository admins,
            AcademicYearRepository years, StreamRepository streams) {
        this.students = students; this.staff = staff; this.admins = admins; this.years = years; this.streams = streams;
    }

    public String role(Authentication auth) {
        return auth.getAuthorities().stream().map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user role"));
    }
    public Student student(Authentication auth) {
        return students.findByRollId(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    public Staff staff(Authentication auth) {
        return staff.findByRollId(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    public Admin admin(Authentication auth) {
        return admins.findByRollId(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    public Integer id(Authentication auth) {
        return switch (role(auth)) {
            case "ROLE_STUDENT" -> student(auth).getStudentId();
            case "ROLE_STAFF" -> staff(auth).getStaffId();
            case "ROLE_ADMIN" -> admin(auth).getAdminId();
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        };
    }
    public String name(Authentication auth) {
        return switch (role(auth)) {
            case "ROLE_STUDENT" -> student(auth).getName();
            case "ROLE_STAFF" -> staff(auth).getName();
            case "ROLE_ADMIN" -> admin(auth).getName();
            default -> auth.getName();
        };
    }
    public ApiProfile profile(Authentication auth) {
        return switch (role(auth)) {
            case "ROLE_STUDENT" -> {
                Student s = student(auth); AcademicYear y = years.findById(s.getAcademicYearId()).orElse(null);
                yield new ApiProfile(role(auth), s.getStudentId(), s.getRollId(), s.getName(), s.getEmail(), s.getPhone(),
                        s.getDepartment(), null, s.getAcademicYearId(), y == null ? null : y.getYearName(), null, null);
            }
            case "ROLE_STAFF" -> {
                Staff s = staff(auth); Stream st = streams.findById(s.getStreamId()).orElse(null);
                yield new ApiProfile(role(auth), s.getStaffId(), s.getRollId(), s.getName(), s.getEmail(), s.getPhone(),
                        s.getDepartment(), s.getDesignation(), null, null, s.getStreamId(), st == null ? null : st.getStreamName());
            }
            case "ROLE_ADMIN" -> {
                Admin a = admin(auth);
                yield new ApiProfile(role(auth), a.getAdminId(), a.getRollId(), a.getName(), a.getEmail(), a.getPhone(),
                        null, a.getDesignation(), null, null, null, null);
            }
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        };
    }
    public record ApiProfile(String role, Integer userId, String rollId, String name, String email, String phone,
            String department, String designation, Integer academicYearId, String academicYear,
            Integer streamId, String streamName) {}
}
