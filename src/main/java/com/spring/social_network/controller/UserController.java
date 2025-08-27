package com.spring.social_network.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.social_network.service.UserService;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(HttpServletRequest request) {
        UserResponseDto user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin user hiện tại thành công", request));
    }

    @GetMapping("/authorities")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserAuthorities(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("authorities", userService.getCurrentUserAuthorities());
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy quyền user hiện tại thành công", request));
    }
}
