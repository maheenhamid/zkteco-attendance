package com.zkteco.attendance.second.common;

public class BaseResponse {
	
	protected String message;
	protected int messageType;
	//protected int responseCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

}
