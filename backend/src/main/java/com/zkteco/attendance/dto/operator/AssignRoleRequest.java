package com.zkteco.attendance.dto.operator;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class AssignRoleRequest {

    @NotEmpty(message = "At least one role is required")
    private List<Long> roleIds;
}
