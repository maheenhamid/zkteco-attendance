package com.zkteco.attendance.service;

import com.zkteco.attendance.entity.CommandStatus;
import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceCommand;
import com.zkteco.attendance.entity.DeviceStatus;
import com.zkteco.attendance.repository.DeviceCommandRepository;
import com.zkteco.attendance.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceMonitorScheduler {

    private final DeviceRepository deviceRepository;
    private final DeviceCommandRepository deviceCommandRepository;

    @Value("${app.device.offline-threshold-seconds}")
    private long offlineThresholdSeconds;

    @Value("${app.device.command-timeout-seconds}")
    private long commandTimeoutSeconds;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void markStaleDevicesOffline() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineThresholdSeconds);
        List<Device> staleDevices = deviceRepository.findByStatusAndLastHeartbeatBefore(DeviceStatus.ONLINE, threshold);

        for (Device device : staleDevices) {
            device.setStatus(DeviceStatus.OFFLINE);
            deviceRepository.save(device);
        }

        if (!staleDevices.isEmpty()) {
            log.info("Marked {} device(s) OFFLINE after {}s without a heartbeat", staleDevices.size(), offlineThresholdSeconds);
        }
    }

    @Scheduled(fixedRate = 120000)
    @Transactional
    public void failStuckCommands() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(commandTimeoutSeconds);
        List<DeviceCommand> stuck = deviceCommandRepository.findByStatusInAndCreatedAtBefore(
                Arrays.asList(CommandStatus.PENDING, CommandStatus.SENT), threshold);

        for (DeviceCommand command : stuck) {
            command.setStatus(CommandStatus.FAILED);
            command.setResponseText("Timed out waiting for device response");
            deviceCommandRepository.save(command);
        }

        if (!stuck.isEmpty()) {
            log.info("Marked {} stuck command(s) FAILED after {}s", stuck.size(), commandTimeoutSeconds);
        }
    }
}
