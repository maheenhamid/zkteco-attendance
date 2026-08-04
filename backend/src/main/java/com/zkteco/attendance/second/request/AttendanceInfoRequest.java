package com.zkteco.attendance.second.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceInfoRequest {

	@JsonFormat(pattern="yyyy-MM-dd")
	private LocalDate attendanceDate;
	
	private List<String> deviceIds;

}
