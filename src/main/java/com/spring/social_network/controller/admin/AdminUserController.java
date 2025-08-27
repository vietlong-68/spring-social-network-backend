package com.spring.social_network.controller.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.request.CreateUserRequestDto;
import com.spring.social_network.dto.request.UpdateUserRequestDto;
import com.spring.social_network.dto.request.UserBlockRequestDto;
import com.spring.social_network.dto.request.UserRoleRequestDto;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.dto.response.UserStatisticsDto;
import com.spring.social_network.dto.response.PostStatisticsDto;
import com.spring.social_network.dto.response.AdminStatisticsResponseDto;
import com.spring.social_network.service.AdminUserService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(HttpServletRequest request) {
        List<UserResponseDto> users = adminUserService.findAll();
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công", request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable(name = "id") String id,
            HttpServletRequest request) {
        UserResponseDto user = adminUserService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công", request));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequestDto createUserRequestDto,
            HttpServletRequest request) {
        UserResponseDto createdUser = adminUserService.create(createUserRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "Tạo người dùng thành công", request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UpdateUserRequestDto updateUserRequestDto,
            HttpServletRequest request) {
        UserResponseDto updatedUser = adminUserService.update(id, updateUserRequestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật người dùng thành công", request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable(name = "id") String id,
            HttpServletRequest request) {
        adminUserService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", request));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> blockUser(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UserBlockRequestDto blockRequest,
            HttpServletRequest request) {
        UserResponseDto blockedUser = adminUserService.blockUser(id, blockRequest);
        return ResponseEntity.ok(ApiResponse.success(blockedUser, "Block người dùng thành công", request));
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> unblockUser(
            @PathVariable(name = "id") String id,
            HttpServletRequest request) {
        UserResponseDto unblockedUser = adminUserService.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.success(unblockedUser, "Unblock người dùng thành công", request));
    }

    @GetMapping("/blocked")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getBlockedUsers(HttpServletRequest request) {
        List<UserResponseDto> blockedUsers = adminUserService.findBlockedUsers();
        return ResponseEntity
                .ok(ApiResponse.success(blockedUsers, "Lấy danh sách người dùng bị block thành công", request));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UserRoleRequestDto roleRequest,
            HttpServletRequest request) {
        UserResponseDto updatedUser = adminUserService.updateUserRole(id, roleRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật role người dùng thành công", request));
    }

    @GetMapping("/statistics/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatisticsDto>> getUserStatistics(HttpServletRequest request) {
        UserStatisticsDto userStats = adminUserService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(userStats, "Lấy thống kê người dùng thành công", request));
    }

    @GetMapping("/statistics/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PostStatisticsDto>> getPostStatistics(HttpServletRequest request) {
        PostStatisticsDto postStats = adminUserService.getPostStatistics();
        return ResponseEntity.ok(ApiResponse.success(postStats, "Lấy thống kê bài viết thành công", request));
    }

    @GetMapping("/statistics/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStatisticsResponseDto>> getAdminOverview(HttpServletRequest request) {
        UserStatisticsDto userStats = adminUserService.getUserStatistics();
        PostStatisticsDto postStats = adminUserService.getPostStatistics();

        AdminStatisticsResponseDto overview = AdminStatisticsResponseDto.builder()
                .userStatistics(userStats)
                .postStatistics(postStats)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        return ResponseEntity.ok(ApiResponse.success(overview, "Lấy thống kê tổng hợp thành công", request));
    }
}
