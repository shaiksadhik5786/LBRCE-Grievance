package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.StudentRegistrationRequest;
import com.example.demo.entity.StudentRegistrationRequest.Status;

public interface StudentRegistrationRepository extends JpaRepository<StudentRegistrationRequest, Integer> {
    boolean existsByRollId(String rollId);
    boolean existsByEmail(String email);
    boolean existsByRollIdAndStatus(String rollId, Status status);
    boolean existsByEmailAndStatus(String email, Status status);
    List<StudentRegistrationRequest> findByStatus(Status status);
    long countByStatus(Status status);
    Optional<StudentRegistrationRequest> findFirstByRollIdAndStatusOrderBySubmittedAtDesc(String rollId, Status status);
}
