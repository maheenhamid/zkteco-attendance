package com.zkteco.attendance.controller;

import com.zkteco.attendance.dto.auth.LoginRequest;
import com.zkteco.attendance.dto.auth.LoginResponse;
import com.zkteco.attendance.security.SecurityUtils;
import com.zkteco.attendance.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse.OperatorProfile> me() {
        String username = SecurityUtils.currentUser().getUsername();
        return ResponseEntity.ok(authService.currentProfile(username));
    }
}
