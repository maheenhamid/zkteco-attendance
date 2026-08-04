package com.zkteco.attendance.second.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "iclock_transaction")
public class IclockTransaction implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "terminal_alias")
	private String area;

	@Column(name = "punch_time")
	private LocalDateTime checkDateTime;

	@Column(name = "upload_time")
	private LocalDateTime uploadTime;
	
	@Column(name = "terminal_sn")
	private String terminalSn;
	
	@Column(name = "emp_code")
	private String empCode;

}
