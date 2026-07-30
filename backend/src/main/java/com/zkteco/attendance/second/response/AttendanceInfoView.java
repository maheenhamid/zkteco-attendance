package com.zkteco.attendance.second.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceInfoView {
	
	private Long id;

	private String area;

	private String checkDateTime;

	private String checkDate;

	private String checkTime;

	private String uploadTime;

	private String terminalSn;

	private String empCode;

}
