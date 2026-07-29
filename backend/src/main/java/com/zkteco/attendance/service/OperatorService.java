package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.operator.AssignRoleRequest;
import com.zkteco.attendance.dto.operator.OperatorRequest;
import com.zkteco.attendance.entity.Operator;
import com.zkteco.attendance.entity.OperatorStatus;
import com.zkteco.attendance.entity.Role;
import com.zkteco.attendance.exception.BadRequestException;
import com.zkteco.attendance.exception.ResourceNotFoundException;
import com.zkteco.attendance.repository.OperatorRepository;
import com.zkteco.attendance.repository.RoleRepository;
import com.zkteco.attendance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorRepository operatorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Operator> list() {
        if (SecurityUtils.isSuperAdmin()) {
            return operatorRepository.findAll();
        }
        Long instituteId = SecurityUtils.currentInstituteId();
        return operatorRepository.findAll().stream()
                .filter(o -> instituteId.equals(o.getInstituteId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Operator create(OperatorRequest request) {
        if (operatorRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Password is required for a new operator");
        }

        Operator operator = new Operator();
        operator.setUsername(request.getUsername());
        operator.setPassword(passwordEncoder.encode(request.getPassword()));
        operator.setFullName(request.getFullName());
        operator.setEmail(request.getEmail());
        operator.setInstituteId(SecurityUtils.resolveInstituteId(request.getInstituteId()));
        operator.setStatus(OperatorStatus.ACTIVE);
        operator.setRoles(resolveRoles(request.getRoleIds()));

        return operatorRepository.save(operator);
    }

    @Transactional
    public Operator update(Long id, OperatorRequest request) {
        Operator operator = getOwned(id);

        if (!operator.getUsername().equals(request.getUsername()) && operatorRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }

        operator.setUsername(request.getUsername());
        if (StringUtils.hasText(request.getPassword())) {
            operator.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        operator.setFullName(request.getFullName());
        operator.setEmail(request.getEmail());
        if (SecurityUtils.isSuperAdmin()) {
            operator.setInstituteId(request.getInstituteId());
        }
        operator.setRoles(resolveRoles(request.getRoleIds()));

        return operatorRepository.save(operator);
    }

    @Transactional
    public Operator assignRoles(Long id, AssignRoleRequest request) {
        Operator operator = getOwned(id);
        operator.setRoles(resolveRoles(request.getRoleIds()));
        return operatorRepository.save(operator);
    }

    @Transactional
    public void delete(Long id) {
        Operator operator = getOwned(id);
        operatorRepository.delete(operator);
    }

    private Operator getOwned(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + id));
        if (!SecurityUtils.isSuperAdmin() && !SecurityUtils.currentInstituteId().equals(operator.getInstituteId())) {
            throw new ResourceNotFoundException("Operator not found: " + id);
        }
        return operator;
    }

    private Set<Role> resolveRoles(List<Long> ids) {
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(ids));
        if (roles.size() != ids.size()) {
            throw new BadRequestException("One or more role ids are invalid");
        }
        return roles;
    }
}
