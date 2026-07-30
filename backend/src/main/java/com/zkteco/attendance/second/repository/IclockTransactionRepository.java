package com.zkteco.attendance.second.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.zkteco.attendance.second.entity.IclockTransaction;


public interface IclockTransactionRepository extends JpaRepository<IclockTransaction, Long>{

	
	@Query(value = "SELECT t from IclockTransaction t where DATE(t.checkDateTime)=?1 AND t.terminalSn IN ?2")
	public List<IclockTransaction> fetchDeviceData(Date date, List<String> deviceIds);
}