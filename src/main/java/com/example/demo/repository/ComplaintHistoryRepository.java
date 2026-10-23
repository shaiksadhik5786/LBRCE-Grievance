package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.ComplaintHistory;

public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Integer> {
    List<ComplaintHistory> findByRegisteredComplaint_RegisteredComplaintIdOrderByCreatedAtAsc(Integer id);
}
