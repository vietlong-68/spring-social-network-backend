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

/**
 * Controller xử lý các yêu cầu liên quan đến người dùng
 * 
 * Cung cấp các endpoint để:
 * - Lấy thông tin người dùng hiện tại
 * - Lấy danh sách quyền của người dùng hiện tại
 * 
 * Yêu cầu quyền: USER hoặc ADMIN
 * 
 * @author Spring Social Network Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lấy thông tin chi tiết của người dùng hiện tại
     * 
     * Endpoint này trả về thông tin cá nhân, profile và các thông tin
     * khác của người dùng đang đăng nhập
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(HttpServletRequest request) {
        UserResponseDto user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin user hiện tại thành công", request));
    }

    /**
     * Lấy danh sách quyền và vai trò của người dùng hiện tại
     * 
     * Endpoint này trả về các quyền mà người dùng đang có,
     * bao gồm cả vai trò (USER, ADMIN) và các quyền cụ thể
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách quyền của người dùng
     */
    @GetMapping("/authorities")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserAuthorities(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("authorities", userService.getCurrentUserAuthorities());
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy quyền user hiện tại thành công", request));
    }
}
