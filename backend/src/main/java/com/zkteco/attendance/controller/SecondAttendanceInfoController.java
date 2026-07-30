package com.zkteco.attendance.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zkteco.attendance.second.common.ItemResponse;
import com.zkteco.attendance.second.request.AttendanceInfoRequest;
import com.zkteco.attendance.second.service.AttendanceInfoService;

@RestController
public class SecondAttendanceInfoController {
	
	@Autowired
	public AttendanceInfoService attendanceInfoService;
	
	@GetMapping(value = "/")
	public String applicationStart() {
		return "Application Started";
	}

	@PostMapping(value = "/atd/list")
	public ResponseEntity<ItemResponse> attendanceList(@RequestBody AttendanceInfoRequest request) {
		return new ResponseEntity<>(attendanceInfoService.attendanceInfoList(request), HttpStatus.OK);
	}

}
