package com.zkteco.attendance.util;

import com.zkteco.attendance.entity.DeviceUser;

/** Builds ZKTeco ADMS command strings queued via the /iclock/getrequest command channel. */
public final class ZKTecoCommandBuilder {

    private ZKTecoCommandBuilder() {
    }

    public static String userInfoUpdate(DeviceUser user) {
        StringBuilder sb = new StringBuilder("DATA UPDATE USERINFO");
        sb.append(" PIN=").append(user.getEnrollNo());
        sb.append("\tName=").append(user.getFullName());
        sb.append("\tPri=").append(user.getDevicePrivilege().getCode());
        sb.append("\tPasswd=");
        sb.append("\tCard=").append(user.getCardNo() == null ? "" : user.getCardNo());
        sb.append("\tGrp=1");
        sb.append("\tTZ=0000000000000000");
        return sb.toString();
    }

    public static String userDelete(String enrollNo) {
        return "DATA DELETE USERINFO PIN=" + enrollNo;
    }

    public static String rebootDevice() {
        return "REBOOT";
    }

    public static String clearAllData() {
        return "CLEAR DATA";
    }
}
