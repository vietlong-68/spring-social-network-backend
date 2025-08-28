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
import com.spring.social_network.dto.response.LoginResponse;
import com.spring.social_network.dto.response.IntrospectResponse;
import com.spring.social_network.dto.response.RefreshTokenResponse;
import com.spring.social_network.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controller xử lý các yêu cầu xác thực và ủy quyền
 * 
 * Cung cấp các endpoint để:
 * - Đăng nhập người dùng
 * - Đăng xuất người dùng
 * - Kiểm tra tính hợp lệ của token
 * - Làm mới token khi hết hạn
 * 
 * @author Spring Social Network Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Xử lý yêu cầu đăng nhập của người dùng
     * 
     * @param loginRequest Thông tin đăng nhập (email/username và mật khẩu)
     * @param request      HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin đăng nhập thành công và JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        LoginResponse loginResponse = authService.handleLogin(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Đăng nhập thành công", request));
    }

    /**
     * Xử lý yêu cầu đăng xuất của người dùng
     * 
     * @param logoutRequest Thông tin token cần đăng xuất
     * @param request       HttpServletRequest để lấy thông tin request
     * @return ResponseEntity xác nhận đăng xuất thành công
     * @throws JOSEException  Khi có lỗi xử lý JWT
     * @throws ParseException Khi có lỗi parse dữ liệu
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest logoutRequest,
            HttpServletRequest request) throws JOSEException, ParseException {
        authService.handleLogout(logoutRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", request));
    }

    /**
     * Kiểm tra tính hợp lệ và thông tin của JWT token
     * 
     * @param introspectRequest Token cần kiểm tra
     * @param request           HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin chi tiết về token
     * @throws JOSEException  Khi có lỗi xử lý JWT
     * @throws ParseException Khi có lỗi parse dữ liệu
     */
    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest introspectRequest,
            HttpServletRequest request) throws JOSEException, ParseException {
        IntrospectResponse introspectResponse = authService.handleIntrospect(introspectRequest);
        return ResponseEntity.ok(ApiResponse.success(introspectResponse, "Kiểm tra token thành công", request));
    }

    /**
     * Làm mới JWT token khi token cũ sắp hết hạn
     * 
     * @param refreshTokenRequest Refresh token để tạo token mới
     * @param request             HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa token mới
     * @throws JOSEException  Khi có lỗi xử lý JWT
     * @throws ParseException Khi có lỗi parse dữ liệu
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) throws JOSEException, ParseException {
        RefreshTokenResponse refreshTokenResponse = authService.handleRefreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(refreshTokenResponse, "Làm mới token thành công", request));
    }
}
