package com.spring.social_network.controller;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.request.LoginRequest;
import com.spring.social_network.dto.request.LogoutRequest;
import com.spring.social_network.dto.request.IntrospectRequest;
import com.spring.social_network.dto.request.RefreshTokenRequest;
import com.spring.social_network.dto.request.RegisterRequest;
import com.spring.social_network.dto.response.LoginResponse;
import com.spring.social_network.dto.response.IntrospectResponse;
import com.spring.social_network.dto.response.RefreshTokenResponse;
import com.spring.social_network.dto.response.RegisterResponse;
import com.spring.social_network.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        RegisterResponse registerResponse = authService.handleRegister(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(registerResponse, "Đăng ký tài khoản thành công", request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        LoginResponse loginResponse = authService.handleLogin(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Đăng nhập thành công", request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest logoutRequest,
            HttpServletRequest request) throws JOSEException, ParseException {
        authService.handleLogout(logoutRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest introspectRequest,
            HttpServletRequest request) throws JOSEException, ParseException {
        IntrospectResponse introspectResponse = authService.handleIntrospect(introspectRequest);
        return ResponseEntity.ok(ApiResponse.success(introspectResponse, "Kiểm tra token thành công", request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) throws JOSEException, ParseException {
        RefreshTokenResponse refreshTokenResponse = authService.handleRefreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(refreshTokenResponse, "Làm mới token thành công", request));
    }
}
