package com.example.demo.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.entity.RegisteredComplaint;

public interface RegisteredComplaintRepository extends JpaRepository<RegisteredComplaint, Integer>, JpaSpecificationExecutor<RegisteredComplaint> {
    Optional<RegisteredComplaint> findByComplaint_ComplaintId(Integer complaintId);
    long countByRegistrationStatus(String registrationStatus);
    long countByStudent_StudentIdAndRegistrationStatus(Integer studentId, String registrationStatus);
    long countByAssignedStaff_StaffIdAndRegistrationStatus(Integer staffId, String registrationStatus);

    @Query(value = "select rc.registered_complaint_id from registered_complaint rc "
            + "inner join complaint c on c.complaint_id = rc.complaint_id "
            + "inner join student rs on rs.student_id = rc.student_id "
            + "inner join student cs on cs.student_id = c.student_id "
            + "left join admin a on a.admin_id = rc.admin_id "
            + "left join stream st on st.stream_id = rc.stream_id "
            + "left join staff sf on sf.staff_id = rc.assigned_staff_id "
            + "where rs.student_id = cs.student_id "
            + "and (rc.admin_id is null or a.admin_id is not null) "
            + "and (rc.stream_id is null or st.stream_id is not null) "
            + "and (rc.assigned_staff_id is null or sf.staff_id is not null)", nativeQuery = true)
    List<Integer> findActiveWorkflowIds();

    @Query(value = "select count(*) from registered_complaint rc "
            + "inner join complaint c on c.complaint_id = rc.complaint_id "
            + "inner join student rs on rs.student_id = rc.student_id "
            + "inner join student cs on cs.student_id = c.student_id "
            + "left join admin a on a.admin_id = rc.admin_id "
            + "left join stream st on st.stream_id = rc.stream_id "
            + "left join staff sf on sf.staff_id = rc.assigned_staff_id "
            + "where rc.registration_status = :status and rs.student_id = cs.student_id "
            + "and (rc.admin_id is null or a.admin_id is not null) "
            + "and (rc.stream_id is null or st.stream_id is not null) "
            + "and (rc.assigned_staff_id is null or sf.staff_id is not null)", nativeQuery = true)
    long countActiveByRegistrationStatus(@Param("status") String status);

    @Query(value = "select count(*) from registered_complaint rc "
            + "inner join complaint c on c.complaint_id = rc.complaint_id "
            + "inner join student rs on rs.student_id = rc.student_id "
            + "inner join student cs on cs.student_id = c.student_id "
            + "left join admin a on a.admin_id = rc.admin_id "
            + "left join stream st on st.stream_id = rc.stream_id "
            + "left join staff sf on sf.staff_id = rc.assigned_staff_id "
            + "where rc.registration_status = :status and rc.student_id = :studentId and rs.student_id = cs.student_id "
            + "and (rc.admin_id is null or a.admin_id is not null) "
            + "and (rc.stream_id is null or st.stream_id is not null) "
            + "and (rc.assigned_staff_id is null or sf.staff_id is not null)", nativeQuery = true)
    long countActiveByStudentAndStatus(@Param("studentId") Integer studentId, @Param("status") String status);

    @Query(value = "select count(*) from registered_complaint rc "
            + "inner join complaint c on c.complaint_id = rc.complaint_id "
            + "inner join student rs on rs.student_id = rc.student_id "
            + "inner join student cs on cs.student_id = c.student_id "
            + "inner join staff sf on sf.staff_id = rc.assigned_staff_id "
            + "left join admin a on a.admin_id = rc.admin_id "
            + "left join stream st on st.stream_id = rc.stream_id "
            + "where rc.registration_status = :status and rc.assigned_staff_id = :staffId and rs.student_id = cs.student_id "
            + "and (rc.admin_id is null or a.admin_id is not null) "
            + "and (rc.stream_id is null or st.stream_id is not null)", nativeQuery = true)
    long countActiveByAssignedStaffAndStatus(@Param("staffId") Integer staffId, @Param("status") String status);
}
