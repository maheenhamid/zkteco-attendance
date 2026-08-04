package com.zkteco.attendance.second.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ep_eptransaction")
public class AttendanceInfo implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "area")
	private String area;

	@Column(name = "check_datetime")
	private LocalDateTime checkDateTime;

	@Column(name = "check_date")
	private LocalDate checkdate;

	@Column(name = "check_time")
	private LocalTime checkTime;

	@Column(name = "upload_time")
	private LocalDateTime uploadTime;
	
	@Column(name = "terminal_sn")
	private String terminalSn;
	
	@Column(name = "emp_code")
	private String empCode;

}
