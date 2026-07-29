package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.attendance.AttendanceDTO;
import com.zkteco.attendance.dto.common.PageResponse;
import com.zkteco.attendance.entity.AttendanceLog;
import com.zkteco.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW')")
    public PageResponse<AttendanceDTO> search(@RequestParam(required = false) Long instituteId,
                                               @RequestParam(required = false) Long classId,
                                               @RequestParam(required = false) Long deviceId,
                                               @RequestParam(required = false) String enrollNo,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                               @PageableDefault(size = 20, sort = "punchTime") Pageable pageable) {
        return PageResponse.of(
                attendanceService.search(instituteId, classId, deviceId, enrollNo, fromDate, toDate, pageable),
                AttendanceDTO::new);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ATTENDANCE_EXPORT')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long instituteId,
                                          @RequestParam(required = false) Long classId,
                                          @RequestParam(required = false) Long deviceId,
                                          @RequestParam(required = false) String enrollNo,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AttendanceLog> logs = attendanceService.searchAll(instituteId, classId, deviceId, enrollNo, fromDate, toDate);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)) {
            writer.println("Enroll No,Full Name,Device,Institute Id,Punch Time,Punch Type,Verify Mode");
            for (AttendanceLog log : logs) {
                String name = log.getDeviceUser() != null ? log.getDeviceUser().getFullName() : "";
                writer.println(String.join(",",
                        csv(log.getEnrollNo()), csv(name), csv(log.getDevice().getName()),
                        csv(String.valueOf(log.getInstituteId())), csv(log.getPunchTime().format(TS_FORMAT)),
                        csv(log.getPunchType()), csv(log.getVerifyMode())));
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "attendance-export.csv");

        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
