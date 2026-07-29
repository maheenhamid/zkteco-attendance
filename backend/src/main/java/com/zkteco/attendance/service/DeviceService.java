package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.device.DeviceDTO;
import com.zkteco.attendance.dto.device.DeviceRequest;
import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceStatus;
import com.zkteco.attendance.exception.BadRequestException;
import com.zkteco.attendance.exception.ResourceNotFoundException;
import com.zkteco.attendance.repository.DeviceRepository;
import com.zkteco.attendance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceCommandService deviceCommandService;

    @Transactional(readOnly = true)
    public Page<Device> list(Long instituteId, DeviceStatus status, String search, Pageable pageable) {
        Long scopedInstituteId = SecurityUtils.resolveInstituteId(instituteId);

        Specification<Device> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (scopedInstituteId != null) {
                predicates.add(cb.equal(root.get("instituteId"), scopedInstituteId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("serialNumber")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return deviceRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Device getOwned(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));
        assertOwnership(device);
        return device;
    }

    @Transactional
    public Device create(DeviceRequest request) {
        Long instituteId = SecurityUtils.resolveInstituteId(request.getInstituteId());
        if (instituteId == null) {
            throw new BadRequestException("Institute is required");
        }
        if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new BadRequestException("A device with this serial number already exists");
        }

        Device device = new Device();
        device.setSerialNumber(request.getSerialNumber());
        device.setName(request.getName());
        device.setInstituteId(instituteId);
        device.setIpAddress(request.getIpAddress());
        device.setPort(request.getPort());
        device.setLocation(request.getLocation());
        device.setStatus(DeviceStatus.OFFLINE);

        return deviceRepository.save(device);
    }

    @Transactional
    public Device update(Long id, DeviceRequest request) {
        Device device = getOwned(id);

        if (!device.getSerialNumber().equals(request.getSerialNumber())
                && deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new BadRequestException("A device with this serial number already exists");
        }

        device.setSerialNumber(request.getSerialNumber());
        device.setName(request.getName());
        if (SecurityUtils.isSuperAdmin()) {
            device.setInstituteId(request.getInstituteId());
        }
        device.setIpAddress(request.getIpAddress());
        device.setPort(request.getPort());
        device.setLocation(request.getLocation());

        return deviceRepository.save(device);
    }

    @Transactional
    public void delete(Long id) {
        Device device = getOwned(id);
        deviceRepository.delete(device);
    }

    /**
     * Queues a request for the device to push its local USERINFO table on its
     * next check-in (ADMS is poll-based - this is not synchronous). The device's
     * reply lands via the existing POST /iclock/cdata?table=USERINFO push
     * channel, handled by IClockService.pullUsersFromDevice.
     */
    @Transactional
    public void pullUsers(Long id) {
        Device device = getOwned(id);
        deviceCommandService.enqueue(device, "DATA QUERY USERINFO", "DATA QUERY USERINFO");
    }

    public static DeviceDTO toDTO(Device device) {
        return new DeviceDTO(device);
    }

    private void assertOwnership(Device device) {
        if (!SecurityUtils.isSuperAdmin() && !device.getInstituteId().equals(SecurityUtils.currentInstituteId())) {
            throw new ResourceNotFoundException("Device not found: " + device.getId());
        }
    }
}
