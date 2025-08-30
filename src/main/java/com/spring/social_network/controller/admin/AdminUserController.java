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

/**
 * Controller dành cho quản trị viên để quản lý người dùng
 * 
 * Cung cấp các endpoint để:
 * - Xem danh sách tất cả người dùng trong hệ thống
 * - Xem thông tin chi tiết của một người dùng cụ thể
 * - Tạo mới người dùng
 * - Cập nhật thông tin người dùng
 * - Xóa người dùng khỏi hệ thống
 * - Block/Unblock người dùng
 * - Quản lý quyền hạn người dùng
 * - Xem thống kê hệ thống
 * 
 * Yêu cầu quyền: ADMIN (chỉ quản trị viên mới có thể truy cập)
 * 
 * @author Spring Social Network Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Lấy danh sách tất cả người dùng trong hệ thống
     * 
     * Endpoint này trả về danh sách đầy đủ tất cả người dùng
     * đã đăng ký trong hệ thống (không phân trang)
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách tất cả người dùng
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(HttpServletRequest request) {
        List<UserResponseDto> users = adminUserService.findAll();
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công", request));
    }

    /**
     * Lấy thông tin chi tiết của một người dùng theo ID
     * 
     * @param id      ID của người dùng cần lấy thông tin
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin chi tiết của người dùng
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable(name = "id") String id,
            HttpServletRequest request) {
        UserResponseDto user = adminUserService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công", request));
    }

    /**
     * Tạo mới một người dùng trong hệ thống
     * 
     * @param createUserRequestDto Thông tin cần thiết để tạo người dùng mới
     * @param request              HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã tạo thành công
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequestDto createUserRequestDto,
            HttpServletRequest request) {
        UserResponseDto createdUser = adminUserService.create(createUserRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "Tạo người dùng thành công", request));
    }

    /**
     * Cập nhật thông tin của một người dùng
     * 
     * @param id                   ID của người dùng cần cập nhật
     * @param updateUserRequestDto Thông tin mới cần cập nhật
     * @param request              HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UpdateUserRequestDto updateUserRequestDto,
            HttpServletRequest request) {
        UserResponseDto updatedUser = adminUserService.update(id, updateUserRequestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật người dùng thành công", request));
    }

    /**
     * Xóa một người dùng khỏi hệ thống
     * 
     * @param id      ID của người dùng cần xóa
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity xác nhận xóa người dùng thành công
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable(name = "id") String id,
            HttpServletRequest request) {
        adminUserService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", request));
    }

    /**
     * Block một người dùng
     * 
     * @param id           ID của người dùng cần block
     * @param blockRequest Thông tin lý do block
     * @param request      HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã bị block
     */
    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> blockUser(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UserBlockRequestDto blockRequest,
            HttpServletRequest request) {
        UserResponseDto blockedUser = adminUserService.blockUser(id, blockRequest);
        return ResponseEntity.ok(ApiResponse.success(blockedUser, "Block người dùng thành công", request));
    }

    /**
     * Unblock một người dùng
     * 
     * @param id      ID của người dùng cần unblock
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã được unblock
     */
    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> unblockUser(
            @PathVariable(name = "id") String id,
            HttpServletRequest request) {
        UserResponseDto unblockedUser = adminUserService.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.success(unblockedUser, "Unblock người dùng thành công", request));
    }

    /**
     * Lấy danh sách người dùng bị block
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách người dùng bị block
     */
    @GetMapping("/blocked")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getBlockedUsers(HttpServletRequest request) {
        List<UserResponseDto> blockedUsers = adminUserService.findBlockedUsers();
        return ResponseEntity
                .ok(ApiResponse.success(blockedUsers, "Lấy danh sách người dùng bị block thành công", request));
    }

    /**
     * Cập nhật role của người dùng
     * 
     * @param id          ID của người dùng cần cập nhật role
     * @param roleRequest Thông tin role mới
     * @param request     HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã cập nhật role
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UserRoleRequestDto roleRequest,
            HttpServletRequest request) {
        UserResponseDto updatedUser = adminUserService.updateUserRole(id, roleRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật role người dùng thành công", request));
    }

    /**
     * Lấy thống kê người dùng
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thống kê người dùng
     */
    @GetMapping("/statistics/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatisticsDto>> getUserStatistics(HttpServletRequest request) {
        UserStatisticsDto userStats = adminUserService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(userStats, "Lấy thống kê người dùng thành công", request));
    }

    /**
     * Lấy thống kê bài viết
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thống kê bài viết
     */
    @GetMapping("/statistics/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PostStatisticsDto>> getPostStatistics(HttpServletRequest request) {
        PostStatisticsDto postStats = adminUserService.getPostStatistics();
        return ResponseEntity.ok(ApiResponse.success(postStats, "Lấy thống kê bài viết thành công", request));
    }

    /**
     * Lấy thống kê tổng hợp cho admin
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thống kê tổng hợp
     */
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
