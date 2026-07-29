package com.zkteco.attendance.dto.device;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DeviceRequest {

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Device name is required")
    private String name;

    @NotNull(message = "Institute is required")
    private Long instituteId;

    private String ipAddress;
    private Integer port;
    private String location;
}
