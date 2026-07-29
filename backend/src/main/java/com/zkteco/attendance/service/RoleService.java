package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.role.RoleRequest;
import com.zkteco.attendance.entity.Permission;
import com.zkteco.attendance.entity.Role;
import com.zkteco.attendance.exception.BadRequestException;
import com.zkteco.attendance.exception.ResourceNotFoundException;
import com.zkteco.attendance.repository.PermissionRepository;
import com.zkteco.attendance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<Role> list() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Permission> listPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional
    public Role create(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new BadRequestException("A role with this name already exists");
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));

        if ("SUPER_ADMIN".equals(role.getName())) {
            throw new BadRequestException("The SUPER_ADMIN role cannot be modified");
        }
        if (!role.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            throw new BadRequestException("A role with this name already exists");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
        if ("SUPER_ADMIN".equals(role.getName())) {
            throw new BadRequestException("The SUPER_ADMIN role cannot be deleted");
        }
        roleRepository.delete(role);
    }

    private Set<Permission> resolvePermissions(List<Long> ids) {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(ids));
        if (permissions.size() != ids.size()) {
            throw new BadRequestException("One or more permission ids are invalid");
        }
        return permissions;
    }
}
