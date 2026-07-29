package com.zkteco.attendance.dto.role;

import com.zkteco.attendance.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private List<PermissionDTO> permissions;

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream().map(PermissionDTO::new).collect(Collectors.toList());
    }
}
