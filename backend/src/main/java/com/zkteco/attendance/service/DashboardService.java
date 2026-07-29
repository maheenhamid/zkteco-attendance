package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.attendance.AttendanceDTO;
import com.zkteco.attendance.dto.dashboard.DashboardStatsDTO;
import com.zkteco.attendance.entity.AttendanceLog;
import com.zkteco.attendance.entity.DeviceStatus;
import com.zkteco.attendance.entity.SyncStatus;
import com.zkteco.attendance.repository.AttendanceLogRepository;
import com.zkteco.attendance.repository.DeviceRepository;
import com.zkteco.attendance.repository.DeviceUserRepository;
import com.zkteco.attendance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final DeviceUserRepository deviceUserRepository;
    private final InstituteApiService instituteApiService;

    @Transactional(readOnly = true)
    public DashboardStatsDTO stats() {
        Long instituteId = SecurityUtils.isSuperAdmin() ? null : SecurityUtils.currentInstituteId();

        long totalDevices;
        long onlineDevices;
        long offlineDevices;
        long todayAttendance;
        long totalCardUsers;
        long unsyncedUsers;
        List<AttendanceLog> recent;

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        if (instituteId == null) {
            totalDevices = deviceRepository.count();
            onlineDevices = deviceRepository.countByStatus(DeviceStatus.ONLINE);
            offlineDevices = deviceRepository.countByStatus(DeviceStatus.OFFLINE);
            todayAttendance = attendanceLogRepository.countByPunchTimeBetween(startOfDay, endOfDay);
            totalCardUsers = deviceUserRepository.count();
            unsyncedUsers = deviceUserRepository.countBySyncStatusNot(SyncStatus.SYNCED);
            recent = attendanceLogRepository.findTop15ByOrderByPunchTimeDesc();
        } else {
            totalDevices = deviceRepository.countByInstituteId(instituteId);
            onlineDevices = deviceRepository.countByInstituteIdAndStatus(instituteId, DeviceStatus.ONLINE);
            offlineDevices = deviceRepository.countByInstituteIdAndStatus(instituteId, DeviceStatus.OFFLINE);
            todayAttendance = attendanceLogRepository.countByInstituteIdAndPunchTimeBetween(instituteId, startOfDay, endOfDay);
            totalCardUsers = deviceUserRepository.countByInstituteId(instituteId);
            unsyncedUsers = deviceUserRepository.countByInstituteIdAndSyncStatusNot(instituteId, SyncStatus.SYNCED);
            recent = attendanceLogRepository.findTop15ByInstituteIdOrderByPunchTimeDesc(instituteId);
        }

        long totalInstitutes = instituteId == null ? instituteApiService.listInstitutes().size() : 1;
        long syncedUsers = totalCardUsers - unsyncedUsers;
        List<AttendanceDTO> recentPunches = recent.stream().map(AttendanceDTO::new).collect(Collectors.toList());

        return new DashboardStatsDTO(totalDevices, onlineDevices, offlineDevices, todayAttendance, totalInstitutes,
                totalCardUsers, syncedUsers, unsyncedUsers, recentPunches);
    }
}
