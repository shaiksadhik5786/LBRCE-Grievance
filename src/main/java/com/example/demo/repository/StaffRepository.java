package com.example.demo.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByRollId(String rollId);
    Optional<Staff> findByEmail(String email);

    boolean existsByRollId(String rollId);

    boolean existsByEmail(String email);

    List<Staff> findByStreamId(Integer streamId);
}
