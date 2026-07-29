package com.zkteco.attendance.dto.dashboard;

import com.zkteco.attendance.dto.attendance.AttendanceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
    private long todayAttendance;
    private long totalInstitutes;
    private long totalCardUsers;
    private long syncedUsers;
    private long unsyncedUsers;
    private List<AttendanceDTO> recentPunches;
}
