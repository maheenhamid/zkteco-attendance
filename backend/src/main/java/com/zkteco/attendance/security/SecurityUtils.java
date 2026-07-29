package com.zkteco.attendance.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        throw new IllegalStateException("No authenticated operator in the security context");
    }

    public static boolean isSuperAdmin() {
        return currentUser().isSuperAdmin();
    }

    public static Long currentInstituteId() {
        return currentUser().getInstituteId();
    }

    /**
     * Resolves the institute id that a query should actually run against: for
     * non-super-admins the operator's own institute always wins, regardless of
     * what the client requested, enforcing tenant isolation server-side.
     */
    public static Long resolveInstituteId(Long requestedInstituteId) {
        CustomUserDetails user = currentUser();
        if (user.isSuperAdmin()) {
            return requestedInstituteId;
        }
        return user.getInstituteId();
    }
}
