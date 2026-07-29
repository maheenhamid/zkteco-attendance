package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long>, JpaSpecificationExecutor<AttendanceLog> {
    long countByPunchTimeBetween(LocalDateTime start, LocalDateTime end);
    long countByInstituteIdAndPunchTimeBetween(Long instituteId, LocalDateTime start, LocalDateTime end);
    List<AttendanceLog> findTop15ByOrderByPunchTimeDesc();
    List<AttendanceLog> findTop15ByInstituteIdOrderByPunchTimeDesc(Long instituteId);
}
