package com.zkteco.attendance.second.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.zkteco.attendance.second.entity.IclockTransaction;


public interface IclockTransactionRepository extends JpaRepository<IclockTransaction, Long>{

	// Day-range comparison instead of JPQL DATE(t.checkDateTime)=?1: Hibernate 5.6's
	// built-in DATE() function has a fixed java.util.Date return-type binding, so it
	// rejects a LocalDate parameter at validation time even though the underlying
	// column is fine. Comparing checkDateTime directly against LocalDateTime bounds
	// sidesteps that entirely (and lets the DB use an index on checkDateTime).
	@Query(value = "SELECT t from IclockTransaction t where t.checkDateTime >= ?1 AND t.checkDateTime < ?2 AND t.terminalSn IN ?3")
	public List<IclockTransaction> fetchDeviceData(LocalDateTime startOfDay, LocalDateTime endOfDay, List<String> deviceIds);
}