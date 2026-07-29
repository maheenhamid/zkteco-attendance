package com.zkteco.attendance.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private OperatorProfile operator;

    public LoginResponse(String token, OperatorProfile operator) {
        this.token = token;
        this.operator = operator;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorProfile {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private Long instituteId;
        private boolean superAdmin;
        private Set<String> roles;
        private Set<String> permissions;
    }
}
