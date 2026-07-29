package com.zkteco.attendance.dto.deviceuser;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BulkDeleteRequest {

    @NotEmpty(message = "At least one id is required")
    private List<Long> ids;
}
