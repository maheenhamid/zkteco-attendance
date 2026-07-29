package com.zkteco.attendance.dto.deviceuser;

import com.zkteco.attendance.entity.DevicePrivilege;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DeviceUserRequest {

    @NotNull(message = "Institute is required")
    private Long instituteId;

    private Long classId;
    private String className;

    @NotBlank(message = "Full name is required")
    private String fullName;

    /** Optional - auto-assigned as the next available PIN on the device when left blank. */
    private String enrollNo;

    private String cardNo;

    @NotNull(message = "Device privilege is required")
    private DevicePrivilege devicePrivilege;

    @NotNull(message = "Device is required")
    private Long deviceId;

    /** Optional system Roles tagged onto this card user (separate from devicePrivilege). */
    private List<Long> roleIds;
}
