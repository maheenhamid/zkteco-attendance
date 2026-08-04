package com.zkteco.attendance.second.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zkteco.attendance.second.entity.AttendanceInfo;

public interface AttendanceInfoRepository extends JpaRepository<AttendanceInfo, Long> {

	List<AttendanceInfo> findByCheckdateAndTerminalSnIn(LocalDate attendanceDate, List<String> deviceIds);

	List<AttendanceInfo> findByCheckdate(LocalDate checkdate);

}
