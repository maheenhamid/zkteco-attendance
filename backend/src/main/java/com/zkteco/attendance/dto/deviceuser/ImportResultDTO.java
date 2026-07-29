package com.zkteco.attendance.dto.deviceuser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {
    private int successCount;
    private int errorCount;
    private List<String> errors = new ArrayList<>();
}
