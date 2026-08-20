package com.zkteco.attendance.service;

import com.zkteco.attendance.entity.AttendanceLog;
import com.zkteco.attendance.repository.AttendanceLogRepository;
import com.zkteco.attendance.second.common.ItemResponse;
import com.zkteco.attendance.second.request.AttendanceInfoRequest;
import com.zkteco.attendance.second.response.AttendanceInfoView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Same request/response contract as {@code SecondAttendanceInfoController}'s
 * /atd/list (see {@link com.zkteco.attendance.second.service.AttendanceInfoService}),
 * but reads punches from this app's own primary datasource
 * ({@link AttendanceLog}, populated by the iclock ingestion in this app)
 * instead of the external secondary datasource.
 */
@Service
@RequiredArgsConstructor
public class PrimaryAttendanceInfoService {

    private final AttendanceLogRepository attendanceLogRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Transactional(readOnly = true)
    public ItemResponse attendanceInfoList(AttendanceInfoRequest request) {
        ItemResponse itemResponse = new ItemResponse();

        LocalDateTime startOfDay = request.getAttendanceDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<AttendanceLog> logs = attendanceLogRepository
                .findByPunchTimeBetweenAndDevice_SerialNumberIn(startOfDay, endOfDay, request.getDeviceIds());

        List<AttendanceInfoView> views = new ArrayList<>();
        for (AttendanceLog log : logs) {
            AttendanceInfoView view = new AttendanceInfoView();
            view.setId(log.getId());
            view.setArea(log.getDevice().getLocation());
            view.setCheckDate(DATE_FMT.format(log.getPunchTime()));
            view.setCheckDateTime(DATETIME_FMT.format(log.getPunchTime()));
            view.setCheckTime(TIME_FMT.format(log.getPunchTime()));
            view.setEmpCode(log.getEnrollNo());
            view.setTerminalSn(log.getDevice().getSerialNumber());
            view.setUploadTime(log.getCreatedAt() != null ? DATETIME_FMT.format(log.getCreatedAt()) : null);
            views.add(view);
        }

        itemResponse.setItem(views);
        itemResponse.setMessageType(1);
        itemResponse.setMessage("OK");
        return itemResponse;
    }
}
