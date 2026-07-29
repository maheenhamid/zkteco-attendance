package com.zkteco.attendance.dto.institute;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/** Common envelope shape used by every api.shebashikkha.com public endpoint. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalApiEnvelope<T> {
    private String message;
    private Integer messageType;
    private List<T> item = Collections.emptyList();
}
