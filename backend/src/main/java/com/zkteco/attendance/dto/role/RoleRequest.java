package com.zkteco.attendance.dto.role;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    @NotEmpty(message = "At least one permission is required")
    private List<Long> permissionIds;
}
