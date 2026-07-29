package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    Optional<Device> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);
    long countByInstituteId(Long instituteId);
    long countByStatus(DeviceStatus status);
    long countByInstituteIdAndStatus(Long instituteId, DeviceStatus status);
    List<Device> findByStatusAndLastHeartbeatBefore(DeviceStatus status, LocalDateTime threshold);
}
