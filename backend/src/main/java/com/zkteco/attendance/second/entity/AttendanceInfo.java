package com.zkteco.attendance.second.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
	private Date checkDateTime;
	
	@Column(name = "check_date")
	@Temporal(TemporalType.DATE)
	private Date checkdate;
	
	@Column(name = "check_time")
	private Date checkTime;
	
	@Column(name = "upload_time")
	private Date uploadTime;
	
	@Column(name = "terminal_sn")
	private String terminalSn;
	
	@Column(name = "emp_code")
	private String empCode;

}
