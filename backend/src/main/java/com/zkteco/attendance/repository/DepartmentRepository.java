package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByInstituteIdOrderByNameAsc(Long instituteId);
    boolean existsByInstituteIdAndNameIgnoreCase(Long instituteId, String name);
}
