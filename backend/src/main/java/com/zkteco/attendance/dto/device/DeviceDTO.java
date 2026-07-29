package com.zkteco.attendance.dto.device;

import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DeviceDTO {
    private Long id;
    private String serialNumber;
    private String name;
    private Long instituteId;
    private String ipAddress;
    private Integer port;
    private String location;
    private DeviceStatus status;
    private LocalDateTime lastHeartbeat;
    private String firmwareVersion;
    private LocalDateTime createdAt;

    public DeviceDTO(Device d) {
        this.id = d.getId();
        this.serialNumber = d.getSerialNumber();
        this.name = d.getName();
        this.instituteId = d.getInstituteId();
        this.ipAddress = d.getIpAddress();
        this.port = d.getPort();
        this.location = d.getLocation();
        this.status = d.getStatus();
        this.lastHeartbeat = d.getLastHeartbeat();
        this.firmwareVersion = d.getFirmwareVersion();
        this.createdAt = d.getCreatedAt();
    }
}
