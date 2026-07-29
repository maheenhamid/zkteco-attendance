package com.zkteco.attendance.dto.deviceuser;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BulkDeleteByFilterRequest {

    /** Required - filter-based bulk delete always needs an explicit institute scope. */
    @NotNull(message = "Institute is required")
    private Long instituteId;

    private Long classId;
}
