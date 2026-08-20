package com.zkteco.attendance.dto.department;

import com.zkteco.attendance.entity.Department;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DepartmentDTO {
    private Long id;
    private Long instituteId;
    private String name;

    public DepartmentDTO(Department department) {
        this.id = department.getId();
        this.instituteId = department.getInstituteId();
        this.name = department.getName();
    }
}
