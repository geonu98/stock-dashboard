package com.stock.dashboard.backend.controller;

import com.stock.dashboard.backend.model.payload.request.LoginRequest;
import com.stock.dashboard.backend.model.payload.response.JwtAuthenticationResponse;
import com.stock.dashboard.backend.security.model.CustomUserDetails;
import com.stock.dashboard.backend.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        log.info("Login request received: email={}, deviceId={}",
                loginRequest.getEmail(),
                loginRequest.getDeviceInfo() != null ? loginRequest.getDeviceInfo().getDeviceId() : null);

        Authentication authentication = authService.authenticateUser(loginRequest)
                .orElseThrow(() -> new RuntimeException("로그인 실패"));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = authService.generateToken(userDetails);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }
}
