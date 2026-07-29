package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.role.PermissionDTO;
import com.zkteco.attendance.dto.role.RoleDTO;
import com.zkteco.attendance.dto.role.RoleRequest;
import com.zkteco.attendance.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public List<RoleDTO> list() {
        return roleService.list().stream().map(RoleDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public List<PermissionDTO> permissions() {
        return roleService.listPermissions().stream().map(PermissionDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleDTO create(@Valid @RequestBody RoleRequest request) {
        return new RoleDTO(roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleDTO update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return new RoleDTO(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
