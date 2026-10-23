package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Stream;

public interface StreamRepository extends JpaRepository<Stream, Integer> {

}