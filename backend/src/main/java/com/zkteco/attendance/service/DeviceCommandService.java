package com.zkteco.attendance.service;

import com.zkteco.attendance.entity.CommandStatus;
import com.zkteco.attendance.entity.Device;
import com.zkteco.attendance.entity.DeviceCommand;
import com.zkteco.attendance.repository.DeviceCommandRepository;
import com.zkteco.attendance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;

    @Transactional
    public DeviceCommand enqueue(Device device, String commandType, String commandText) {
        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(commandType);
        command.setCommandText(commandText);
        command.setStatus(CommandStatus.PENDING);
        return deviceCommandRepository.save(command);
    }

    @Transactional(readOnly = true)
    public Page<DeviceCommand> list(Long deviceId, CommandStatus status, Pageable pageable) {
        Long scopedInstituteId = SecurityUtils.isSuperAdmin() ? null : SecurityUtils.currentInstituteId();

        Specification<DeviceCommand> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (deviceId != null) {
                predicates.add(cb.equal(root.get("device").get("id"), deviceId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (scopedInstituteId != null) {
                predicates.add(cb.equal(root.get("device").get("instituteId"), scopedInstituteId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return deviceCommandRepository.findAll(spec, pageable);
    }
}
