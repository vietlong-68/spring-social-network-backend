package com.spring.social_network.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import com.spring.social_network.service.UserService;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.dto.request.UpdateProfileRequestDto;
import com.spring.social_network.dto.request.ChangePasswordRequestDto;
import com.spring.social_network.dto.request.SearchUserRequestDto;
import com.spring.social_network.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;

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

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<UserResponseDto> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công", request));
    }

    @PostMapping("/profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> uploadProfilePicture(
            @RequestParam(name = "file") MultipartFile file,
            HttpServletRequest request) {
        UserResponseDto updatedUser = userService.uploadProfilePicture(file);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Upload ảnh đại diện thành công", request));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDto updateProfileRequest,
            HttpServletRequest request) {
        UserResponseDto updatedUser = userService.updateProfile(updateProfileRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật thông tin cá nhân thành công", request));
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequest,
            HttpServletRequest request) {
        UserResponseDto updatedUser = userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Đổi mật khẩu thành công", request));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> searchUsers(
            @Valid SearchUserRequestDto searchRequest,
            HttpServletRequest request) {
        Page<UserResponseDto> users = userService.searchUsers(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(users, "Tìm kiếm người dùng thành công", request));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @PathVariable(name = "userId") String userId,
            HttpServletRequest request) {
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công", request));
    }

    @DeleteMapping("/profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> removeProfilePicture(HttpServletRequest request) {
        UserResponseDto updatedUser = userService.removeProfilePicture();
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Xóa ảnh đại diện thành công", request));
    }
}
