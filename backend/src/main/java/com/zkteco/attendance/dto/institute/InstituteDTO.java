package com.zkteco.attendance.dto.institute;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Doubles as both the deserialization target for the external Shebashikkha
 * institute list response AND the response body this API returns to the
 * frontend - so the external field names are accepted only as @JsonAlias
 * (input), while the wire format we actually expose stays the clean id/name.
 * Note: the upstream API's field is literally spelled "instiltuteId" (typo).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstituteDTO {

    @JsonAlias("instiltuteId")
    private Long id;

    @JsonAlias("instituteName")
    private String name;

    private String address;
}
