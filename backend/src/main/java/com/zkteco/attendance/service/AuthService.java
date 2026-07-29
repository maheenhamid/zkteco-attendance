package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.auth.LoginRequest;
import com.zkteco.attendance.dto.auth.LoginResponse;
import com.zkteco.attendance.entity.Operator;
import com.zkteco.attendance.entity.Permission;
import com.zkteco.attendance.repository.OperatorRepository;
import com.zkteco.attendance.security.CustomUserDetails;
import com.zkteco.attendance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final OperatorRepository operatorRepository;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);

        Operator operator = operatorRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated operator vanished"));

        return new LoginResponse(token, toProfile(operator));
    }

    @Transactional(readOnly = true)
    public LoginResponse.OperatorProfile currentProfile(String username) {
        Operator operator = operatorRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated operator vanished"));
        return toProfile(operator);
    }

    private LoginResponse.OperatorProfile toProfile(Operator operator) {
        Set<String> roleNames = operator.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        Set<String> permissionCodes = operator.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return new LoginResponse.OperatorProfile(
                operator.getId(),
                operator.getUsername(),
                operator.getFullName(),
                operator.getEmail(),
                operator.getInstituteId(),
                operator.isSuperAdmin(),
                roleNames,
                permissionCodes
        );
    }
}
