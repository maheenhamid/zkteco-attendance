package com.zkteco.attendance.repository;

import com.zkteco.attendance.entity.DeviceUser;
import com.zkteco.attendance.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

public interface DeviceUserRepository extends JpaRepository<DeviceUser, Long>, JpaSpecificationExecutor<DeviceUser> {
    Optional<DeviceUser> findByDeviceIdAndEnrollNo(Long deviceId, String enrollNo);

    @Query("select du.enrollNo from DeviceUser du where du.device.id = :deviceId")
    List<String> findEnrollNosByDeviceId(@Param("deviceId") Long deviceId);

    boolean existsByInstituteIdAndCardNo(Long instituteId, String cardNo);

    long countByInstituteId(Long instituteId);

    long countByInstituteIdAndSyncStatusNot(Long instituteId, SyncStatus syncStatus);

    long countBySyncStatusNot(SyncStatus syncStatus);

    /** Convenience used by the Excel importer - avoids a spurious match on a blank/null cardNo. */
    default boolean cardNoTaken(Long instituteId, String cardNo) {
        return StringUtils.hasText(cardNo) && existsByInstituteIdAndCardNo(instituteId, cardNo);
    }
}
