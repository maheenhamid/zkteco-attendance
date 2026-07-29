package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.CommandStatus;
import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long>, JpaSpecificationExecutor<DeviceCommand> {
    Optional<DeviceCommand> findFirstByDeviceAndStatusOrderByCreatedAtAsc(Device device, CommandStatus status);
    List<DeviceCommand> findByStatusInAndCreatedAtBefore(List<CommandStatus> statuses, LocalDateTime threshold);
}
