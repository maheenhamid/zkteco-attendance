package com.zkteco.attendance.dto.command;

import com.zkteco.attendance.entity.CommandStatus;
import com.zkteco.attendance.entity.DeviceCommand;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DeviceCommandDTO {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String commandType;
    private String commandText;
    private CommandStatus status;
    private String responseText;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime executedAt;

    public DeviceCommandDTO(DeviceCommand c) {
        this.id = c.getId();
        this.deviceId = c.getDevice().getId();
        this.deviceName = c.getDevice().getName();
        this.commandType = c.getCommandType();
        this.commandText = c.getCommandText();
        this.status = c.getStatus();
        this.responseText = c.getResponseText();
        this.createdAt = c.getCreatedAt();
        this.sentAt = c.getSentAt();
        this.executedAt = c.getExecutedAt();
    }
}
