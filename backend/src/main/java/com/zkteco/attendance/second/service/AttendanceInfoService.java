package com.zkteco.attendance.second.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zkteco.attendance.second.common.ItemResponse;
import com.zkteco.attendance.second.entity.AttendanceInfo;
import com.zkteco.attendance.second.entity.IclockTransaction;
import com.zkteco.attendance.second.repository.AttendanceInfoRepository;
import com.zkteco.attendance.second.repository.IclockTransactionRepository;
import com.zkteco.attendance.second.request.AttendanceInfoRequest;
import com.zkteco.attendance.second.response.AttendanceInfoView;

@Service
public class AttendanceInfoService {

	@Autowired
	public AttendanceInfoRepository attendanceInfoRepository;
	
	@Autowired
	public IclockTransactionRepository iclockTransactionRepository;

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ItemResponse attendanceInfoList() {
		
		ItemResponse itemResponse = new ItemResponse();
		
		List<AttendanceInfo> attendanceInfos = attendanceInfoRepository.findAll();
		
		itemResponse.setItem(attendanceInfos);
		
		itemResponse.setMessageType(1);
		
		itemResponse.setMessage("OK");
		
		return itemResponse;
		
	}
	
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ItemResponse attendanceInfoList(AttendanceInfoRequest request) {
		
		ItemResponse itemResponse = new ItemResponse();

		List<AttendanceInfoView> views = new ArrayList<>();
		
		List<AttendanceInfo> attendanceInfos1 = attendanceInfoRepository.findByCheckdateAndTerminalSnIn(request.getAttendanceDate(),request.getDeviceIds());
		
		for(AttendanceInfo info : attendanceInfos1) {
			
			AttendanceInfoView view = new AttendanceInfoView();
			
			view.setArea(info.getArea());
			view.setCheckDate(DATE_FMT.format(info.getCheckdate()));
			view.setCheckDateTime(DATETIME_FMT.format(info.getCheckDateTime()));
			view.setCheckTime(TIME_FMT.format(info.getCheckTime()));
			view.setEmpCode(info.getEmpCode());
			view.setId(info.getId());
			view.setTerminalSn(info.getTerminalSn());
			view.setUploadTime(DATETIME_FMT.format(info.getUploadTime()));

			views.add(view);

		}

		LocalDateTime startOfDay = request.getAttendanceDate().atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		List<IclockTransaction> attendanceInfos2 = iclockTransactionRepository.fetchDeviceData(startOfDay, endOfDay, request.getDeviceIds());

		for(IclockTransaction info : attendanceInfos2) {

			AttendanceInfoView view = new AttendanceInfoView();

			view.setArea(info.getArea());
			view.setCheckDate(DATE_FMT.format(info.getCheckDateTime()));
			view.setCheckDateTime(DATETIME_FMT.format(info.getCheckDateTime()));
			view.setCheckTime(TIME_FMT.format(info.getCheckDateTime()));
			view.setEmpCode(info.getEmpCode());
			view.setId(info.getId());
			view.setTerminalSn(info.getTerminalSn());
			view.setUploadTime(DATETIME_FMT.format(info.getUploadTime()));
			
			views.add(view);

		}
		
		itemResponse.setItem(views);
		
		itemResponse.setMessageType(1);
		
		itemResponse.setMessage("OK");
		
		return itemResponse;
		
	}

}
