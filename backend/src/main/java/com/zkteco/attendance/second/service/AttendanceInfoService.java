package com.zkteco.attendance.second.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		DateFormat df3 = new SimpleDateFormat("HH:mm:ss");
		
		List<AttendanceInfoView> views = new ArrayList<>();
		
		List<AttendanceInfo> attendanceInfos1 = attendanceInfoRepository.findByCheckdateAndTerminalSnIn(request.getAttendanceDate(),request.getDeviceIds());
		
		for(AttendanceInfo info : attendanceInfos1) {
			
			AttendanceInfoView view = new AttendanceInfoView();
			
			view.setArea(info.getArea());
			view.setCheckDate(df1.format(info.getCheckdate()));
			view.setCheckDateTime(df2.format(info.getCheckDateTime()));
			view.setCheckTime(df3.format(info.getCheckTime()));
			view.setEmpCode(info.getEmpCode());
			view.setId(info.getId());
			view.setTerminalSn(info.getTerminalSn());
			view.setUploadTime(df2.format(info.getUploadTime()));
						
			views.add(view);

		}
		
		List<IclockTransaction> attendanceInfos2 = iclockTransactionRepository.fetchDeviceData(request.getAttendanceDate(), request.getDeviceIds());
				
		for(IclockTransaction info : attendanceInfos2) {
			
			AttendanceInfoView view = new AttendanceInfoView();
						
			view.setArea(info.getArea());
			view.setCheckDate(df1.format(info.getCheckDateTime()));
			view.setCheckDateTime(df2.format(info.getCheckDateTime()));
			view.setCheckTime(df3.format(info.getCheckDateTime()));
			view.setEmpCode(info.getEmpCode());
			view.setId(info.getId());
			view.setTerminalSn(info.getTerminalSn());
			view.setUploadTime(df2.format(info.getUploadTime()));
			
			views.add(view);

		}
		
		itemResponse.setItem(views);
		
		itemResponse.setMessageType(1);
		
		itemResponse.setMessage("OK");
		
		return itemResponse;
		
	}

}
