package com.zkteco.attendance.service;

import com.zkteco.attendance.entity.AttendanceLog;
import com.zkteco.attendance.entity.DeviceUser;
import com.zkteco.attendance.repository.AttendanceLogRepository;
import com.zkteco.attendance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;

    @Transactional(readOnly = true)
    public Page<AttendanceLog> search(Long instituteId, Long classId, Long deviceId, String enrollNo,
                                       LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return attendanceLogRepository.findAll(buildSpec(instituteId, classId, deviceId, enrollNo, fromDate, toDate), pageable);
    }

    @Transactional(readOnly = true)
    public List<AttendanceLog> searchAll(Long instituteId, Long classId, Long deviceId, String enrollNo,
                                          LocalDate fromDate, LocalDate toDate) {
        return attendanceLogRepository.findAll(buildSpec(instituteId, classId, deviceId, enrollNo, fromDate, toDate));
    }

    private Specification<AttendanceLog> buildSpec(Long instituteId, Long classId, Long deviceId, String enrollNo,
                                                     LocalDate fromDate, LocalDate toDate) {
        Long scopedInstituteId = SecurityUtils.resolveInstituteId(instituteId);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (scopedInstituteId != null) {
                predicates.add(cb.equal(root.get("instituteId"), scopedInstituteId));
            }
            if (deviceId != null) {
                predicates.add(cb.equal(root.get("device").get("id"), deviceId));
            }
            if (StringUtils.hasText(enrollNo)) {
                predicates.add(cb.equal(root.get("enrollNo"), enrollNo));
            }
            if (classId != null) {
                Join<AttendanceLog, DeviceUser> deviceUserJoin = root.join("deviceUser");
                predicates.add(cb.equal(deviceUserJoin.get("classId"), classId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("punchTime"), LocalDateTime.of(fromDate, LocalTime.MIN)));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("punchTime"), LocalDateTime.of(toDate, LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
