package com.zkteco.attendance.dto.role;

import com.zkteco.attendance.entity.Permission;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionDTO {
    private Long id;
    private String code;
    private String description;
    private String module;

    public PermissionDTO(Permission p) {
        this.id = p.getId();
        this.code = p.getCode();
        this.description = p.getDescription();
        this.module = p.getModule();
    }
}
