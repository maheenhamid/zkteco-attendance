package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByInstituteId(Long instituteId);
}
