package com.zkteco.attendance.service;

import com.zkteco.attendance.entity.*;
import com.zkteco.attendance.repository.AttendanceLogRepository;
import com.zkteco.attendance.repository.DeviceCommandRepository;
import com.zkteco.attendance.repository.DeviceRepository;
import com.zkteco.attendance.repository.DeviceUserRepository;
import com.zkteco.attendance.util.IClockParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implements the ZKTeco ADMS/iClock push protocol used by the F18 device.
 * Devices authenticate purely by serial number (SN) - there is no JWT here,
 * this is a separate, permitAll channel from the operator-facing REST API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IClockService {

    private final DeviceRepository deviceRepository;
    private final DeviceUserRepository deviceUserRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final DeviceCommandRepository deviceCommandRepository;

    @Transactional
    public String handshake(String serialNumber) {
        Device device = findOrRegisterDevice(serialNumber);
        touchHeartbeat(device);

        StringBuilder options = new StringBuilder();
        options.append("GET OPTION FROM: ").append(serialNumber).append("\n");
        options.append("Stamp=9999\n");
        options.append("OpStamp=9999\n");
        options.append("ErrorDelay=60\n");
        options.append("Delay=30\n");
        options.append("TransTimes=00:00;14:00\n");
        options.append("TransInterval=1\n");
        options.append("TransFlag=1111000000\n");
        options.append("Realtime=1\n");
        options.append("Encrypt=0\n");
        return options.toString();
    }

    @Transactional
    public String receiveData(String serialNumber, String table, String body) {
        Device device = findOrRegisterDevice(serialNumber);
        touchHeartbeat(device);

        if (table == null) {
            return "OK";
        }

        switch (table.toUpperCase()) {
            case "ATTLOG":
                return saveAttendance(device, body);
            case "USERINFO":
                return pullUsersFromDevice(device, IClockParser.parseUserInfo(body));
            case "OPERLOG":
                // This firmware doesn't push a dedicated USERINFO table at all - user
                // records (and fingerprint templates, which are ignored) arrive as
                // OPERLOG lines instead. See IClockParser.parseOperLogUsers for why.
                return pullUsersFromDevice(device, IClockParser.parseOperLogUsers(body));
            case "FINGERTMP":
            default:
                log.debug("Received table={} from SN={} ({} bytes) - acknowledged, not processed", table, serialNumber,
                        body == null ? 0 : body.length());
                return "OK";
        }
    }

    @Transactional
    public String getPendingCommand(String serialNumber) {
        Device device = deviceRepository.findBySerialNumber(serialNumber).orElse(null);
        if (device == null) {
            return "OK";
        }
        touchHeartbeat(device);

        Optional<DeviceCommand> next = deviceCommandRepository
                .findFirstByDeviceAndStatusOrderByCreatedAtAsc(device, CommandStatus.PENDING);

        if (!next.isPresent()) {
            return "OK";
        }

        DeviceCommand command = next.get();
        command.setStatus(CommandStatus.SENT);
        command.setSentAt(LocalDateTime.now());
        deviceCommandRepository.save(command);

        return "C:" + command.getId() + ":" + command.getCommandText();
    }

    @Transactional
    public String recordCommandResult(String serialNumber, Long commandId, Integer returnCode, String cmd) {
        if (commandId == null) {
            return "OK";
        }
        deviceCommandRepository.findById(commandId).ifPresent(command -> {
            boolean success = returnCode != null && returnCode == 0;
            command.setStatus(success ? CommandStatus.EXECUTED : CommandStatus.FAILED);
            command.setExecutedAt(LocalDateTime.now());
            command.setResponseText("Return=" + returnCode + (cmd != null ? " CMD=" + cmd : ""));
            deviceCommandRepository.save(command);

            if (success) {
                syncLinkedDeviceUser(command);
            }
        });
        return "OK";
    }

    private void syncLinkedDeviceUser(DeviceCommand command) {
        if (!"DATA UPDATE USERINFO".equals(command.getCommandType())) {
            return;
        }
        // Best-effort: mark any PENDING device users on this device as SYNCED once a
        // USERINFO push to that device succeeds.
        deviceUserRepository.findAll().stream()
                .filter(u -> u.getDevice().getId().equals(command.getDevice().getId()))
                .filter(u -> u.getSyncStatus() == SyncStatus.PENDING)
                .forEach(u -> {
                    u.setSyncStatus(SyncStatus.SYNCED);
                    deviceUserRepository.save(u);
                });
    }

    /**
     * "Pulling" users from the device is not a synchronous request/response in
     * ADMS - the server queues a DATA QUERY USERINFO command (see
     * DeviceService.pullUsers), and the device replies to that on its own
     * schedule via this same push channel (as a USERINFO table push, or as
     * OPERLOG lines - see the two callers). Users already known for this
     * device (by PIN) are skipped so re-pulls don't duplicate them.
     */
    private String pullUsersFromDevice(Device device, List<IClockParser.UserInfoEntry> entries) {
        if (device.getInstituteId() == null) {
            log.warn("Ignoring user push from SN={} - device has no institute assigned yet", device.getSerialNumber());
            return "OK:0";
        }

        int created = 0;

        for (IClockParser.UserInfoEntry entry : entries) {
            if (deviceUserRepository.findByDeviceIdAndEnrollNo(device.getId(), entry.getEnrollNo()).isPresent()) {
                continue;
            }

            DeviceUser user = new DeviceUser();
            user.setInstituteId(device.getInstituteId());
            user.setFullName(entry.getName());
            user.setEnrollNo(entry.getEnrollNo());
            user.setCardNo(entry.getCardNo());
            user.setDevicePrivilege(DevicePrivilege.fromCode(entry.getPrivilegeCode()));
            user.setDevice(device);
            user.setSyncStatus(SyncStatus.SYNCED);
            user.setStatus(OperatorStatus.ACTIVE);

            deviceUserRepository.save(user);
            created++;
        }

        log.info("Pulled {} new user(s) from device SN={} ({} rows in push)", created, device.getSerialNumber(), entries.size());
        return "OK:" + created;
    }

    private String saveAttendance(Device device, String body) {
        List<IClockParser.AttLogEntry> entries = IClockParser.parseAttLog(body);

        for (IClockParser.AttLogEntry entry : entries) {
            AttendanceLog attendanceLog = new AttendanceLog();
            attendanceLog.setEnrollNo(entry.getEnrollNo());
            attendanceLog.setDevice(device);
            attendanceLog.setInstituteId(device.getInstituteId());
            attendanceLog.setPunchTime(entry.getPunchTime());
            attendanceLog.setPunchType(entry.getPunchType());
            attendanceLog.setVerifyMode(entry.getVerifyMode());
            attendanceLog.setRawLine(entry.getRawLine());

            deviceUserRepository.findByDeviceIdAndEnrollNo(device.getId(), entry.getEnrollNo())
                    .ifPresent(attendanceLog::setDeviceUser);

            attendanceLogRepository.save(attendanceLog);
        }

        return "OK:" + entries.size();
    }

    private Device findOrRegisterDevice(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber).orElseGet(() -> {
            Device device = new Device();
            device.setSerialNumber(serialNumber);
            device.setName("Unassigned Device " + serialNumber);
            device.setInstituteId(null);
            device.setLocation("Auto-registered - pending institute assignment");
            device.setStatus(DeviceStatus.ONLINE);
            log.info("Auto-registered previously unknown device SN={}", serialNumber);
            return deviceRepository.save(device);
        });
    }

    private void touchHeartbeat(Device device) {
        device.setLastHeartbeat(LocalDateTime.now());
        device.setStatus(DeviceStatus.ONLINE);
        deviceRepository.save(device);
    }
}
