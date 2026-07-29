package com.zkteco.attendance.dto.attendance;

import com.zkteco.attendance.entity.AttendanceLog;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AttendanceDTO {
    private Long id;
    private String enrollNo;
    private String userFullName;
    private Long deviceUserId;
    private Long deviceId;
    private String deviceName;
    private Long instituteId;
    private LocalDateTime punchTime;
    private String punchType;
    private String verifyMode;

    public AttendanceDTO(AttendanceLog log) {
        this.id = log.getId();
        this.enrollNo = log.getEnrollNo();
        this.userFullName = log.getDeviceUser() != null ? log.getDeviceUser().getFullName() : null;
        this.deviceUserId = log.getDeviceUser() != null ? log.getDeviceUser().getId() : null;
        this.deviceId = log.getDevice().getId();
        this.deviceName = log.getDevice().getName();
        this.instituteId = log.getInstituteId();
        this.punchTime = log.getPunchTime();
        this.punchType = log.getPunchType();
        this.verifyMode = log.getVerifyMode();
    }
}
