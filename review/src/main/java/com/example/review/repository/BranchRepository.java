package com.example.review.repository;

import com.example.review.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByCode(String code);

    Optional<Branch> findByCodeAndActiveTrue(String code);

    List<Branch> findAllByOrderByIdDesc();

    List<Branch> findByActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}