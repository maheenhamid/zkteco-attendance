package com.zkteco.attendance.dto.operator;

import com.zkteco.attendance.entity.Operator;
import com.zkteco.attendance.entity.OperatorStatus;
import com.zkteco.attendance.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OperatorDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Long instituteId;
    private OperatorStatus status;
    private Set<String> roles;

    public OperatorDTO(Operator operator) {
        this.id = operator.getId();
        this.username = operator.getUsername();
        this.fullName = operator.getFullName();
        this.email = operator.getEmail();
        this.instituteId = operator.getInstituteId();
        this.status = operator.getStatus();
        this.roles = operator.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }
}
