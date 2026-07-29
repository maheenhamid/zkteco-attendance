package com.zkteco.attendance.entity;

/**
 * Mirrors the ZKTeco on-device privilege levels (the "Pri=" field of the
 * USERINFO command), not the panel's own RBAC roles.
 */
public enum DevicePrivilege {
    COMMON(0),
    ENROLLER(2),
    ADMIN(14);

    private final int code;

    DevicePrivilege(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /** Reverse lookup used when parsing a "Pri=" value pulled back from the device. Defaults to COMMON for unknown codes. */
    public static DevicePrivilege fromCode(int code) {
        for (DevicePrivilege privilege : values()) {
            if (privilege.code == code) {
                return privilege;
            }
        }
        return COMMON;
    }
}
