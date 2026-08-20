package com.zkteco.attendance.controller;

import com.zkteco.attendance.second.common.ItemResponse;
import com.zkteco.attendance.second.request.AttendanceInfoRequest;
import com.zkteco.attendance.service.PrimaryAttendanceInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public counterpart to {@link SecondAttendanceInfoController}: identical
 * request/response shape ({@link AttendanceInfoRequest} in, {@link ItemResponse}
 * of {@code AttendanceInfoView} out), but backed by this app's own primary
 * datasource instead of the external secondary one.
 */
@RestController
@RequiredArgsConstructor
public class PrimaryAttendanceInfoController {

    private final PrimaryAttendanceInfoService primaryAttendanceInfoService;

    @PostMapping(value = "/atd/list/primary")
    public ResponseEntity<ItemResponse> attendanceList(@RequestBody AttendanceInfoRequest request) {
        return new ResponseEntity<>(primaryAttendanceInfoService.attendanceInfoList(request), HttpStatus.OK);
    }
}
