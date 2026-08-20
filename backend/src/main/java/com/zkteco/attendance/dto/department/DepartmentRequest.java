package com.zkteco.attendance.dto.department;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DepartmentRequest {

    @NotNull(message = "Institute is required")
    private Long instituteId;

    @NotBlank(message = "Department name is required")
    private String name;
}
