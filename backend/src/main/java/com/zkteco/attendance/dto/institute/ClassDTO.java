package com.zkteco.attendance.dto.institute;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassDTO {
    private Long id;
    private String name;
    private String code;
    private Integer status;
}
