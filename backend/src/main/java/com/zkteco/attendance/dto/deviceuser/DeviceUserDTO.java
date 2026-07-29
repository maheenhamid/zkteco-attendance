package com.zkteco.attendance.dto.deviceuser;

import com.zkteco.attendance.entity.DevicePrivilege;
import com.zkteco.attendance.entity.DeviceUser;
import com.zkteco.attendance.entity.OperatorStatus;
import com.zkteco.attendance.entity.Role;
import com.zkteco.attendance.entity.SyncStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class DeviceUserDTO {
    private Long id;
    private Long instituteId;
    private Long classId;
    private String className;
    private String fullName;
    private String enrollNo;
    private String cardNo;
    private DevicePrivilege devicePrivilege;
    private Long deviceId;
    private String deviceName;
    private SyncStatus syncStatus;
    private OperatorStatus status;
    private Set<String> roles;
    private Set<Long> roleIds;
    private LocalDateTime createdAt;

    public DeviceUserDTO(DeviceUser u) {
        this.id = u.getId();
        this.instituteId = u.getInstituteId();
        this.classId = u.getClassId();
        this.className = u.getClassName();
        this.fullName = u.getFullName();
        this.enrollNo = u.getEnrollNo();
        this.cardNo = u.getCardNo();
        this.devicePrivilege = u.getDevicePrivilege();
        this.deviceId = u.getDevice().getId();
        this.deviceName = u.getDevice().getName();
        this.syncStatus = u.getSyncStatus();
        this.status = u.getStatus();
        this.roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        this.roleIds = u.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        this.createdAt = u.getCreatedAt();
    }
}
