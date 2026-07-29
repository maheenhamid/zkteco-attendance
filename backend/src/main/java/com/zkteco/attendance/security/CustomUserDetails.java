package com.zkteco.attendance.security;

import com.zkteco.attendance.entity.Operator;
import com.zkteco.attendance.entity.OperatorStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long operatorId;
    private final String username;
    private final String password;
    private final Long instituteId;
    private final boolean superAdmin;
    private final boolean enabled;
    private final Collection<GrantedAuthority> authorities;

    public CustomUserDetails(Operator operator) {
        this.operatorId = operator.getId();
        this.username = operator.getUsername();
        this.password = operator.getPassword();
        this.instituteId = operator.getInstituteId();
        this.superAdmin = operator.isSuperAdmin();
        this.enabled = operator.getStatus() == OperatorStatus.ACTIVE;
        this.authorities = operator.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getCode()))
                .collect(Collectors.toSet());
    }

    /** Reconstructs a lightweight principal straight from validated JWT claims (no DB hit per request). */
    public CustomUserDetails(Long operatorId, String username, Long instituteId, boolean superAdmin, Set<String> authorityCodes) {
        this.operatorId = operatorId;
        this.username = username;
        this.password = null;
        this.instituteId = instituteId;
        this.superAdmin = superAdmin;
        this.enabled = true;
        this.authorities = authorityCodes.stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority(code))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
