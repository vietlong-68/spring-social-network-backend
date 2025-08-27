package com.spring.social_network.controller;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.request.ForgotPasswordRequest;
import com.spring.social_network.dto.request.ResetPasswordRequest;
import com.spring.social_network.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Email chứa mã đặt lại mật khẩu đã được gửi"));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được đặt lại thành công"));
    }
}
