package com.zkteco.attendance.dto.operator;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class OperatorRequest {

    @NotBlank(message = "Username is required")
    private String username;

    /** Optional on update - leave blank to keep the current password. */
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    private Long instituteId;

    @NotEmpty(message = "At least one role is required")
    private List<Long> roleIds;
}
