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

/**
 * Controller xử lý các yêu cầu liên quan đến người dùng
 * 
 * Cung cấp các endpoint để:
 * - Lấy thông tin người dùng hiện tại
 * - Lấy danh sách quyền của người dùng hiện tại
 * - Lấy danh sách tất cả người dùng (có phân trang)
 * - Upload ảnh đại diện
 * - Xóa ảnh đại diện
 * - Cập nhật thông tin cá nhân
 * - Đổi mật khẩu
 * - Tìm kiếm người dùng
 * - Xem thông tin người dùng khác
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

    /**
     * Lấy danh sách tất cả người dùng có phân trang
     * 
     * Endpoint này cho phép người dùng xem danh sách tất cả người dùng trong hệ
     * thống
     * với phân trang để tối ưu hiệu suất
     * 
     * @param page    Số trang (mặc định: 0)
     * @param size    Kích thước mỗi trang (mặc định: 20)
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách người dùng có phân trang
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Page<UserResponseDto> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công", request));
    }

    /**
     * Upload ảnh đại diện cho người dùng hiện tại
     * 
     * Endpoint này cho phép người dùng tải lên ảnh đại diện mới
     * Ảnh sẽ được lưu vào thư mục uploads và URL được cập nhật vào
     * profilePictureUrl
     * 
     * @param file    MultipartFile chứa ảnh đại diện
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã cập nhật
     */
    @PostMapping("/profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        UserResponseDto updatedUser = userService.uploadProfilePicture(file);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Upload ảnh đại diện thành công", request));
    }

    /**
     * Cập nhật thông tin cá nhân của người dùng hiện tại
     * 
     * Endpoint này cho phép người dùng cập nhật thông tin cá nhân của bản thân
     * như tên, họ, giới tính, ngày sinh, số điện thoại, địa chỉ
     * 
     * @param updateProfileRequest DTO chứa thông tin cập nhật
     * @param request              HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã cập nhật
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDto updateProfileRequest,
            HttpServletRequest request) {
        UserResponseDto updatedUser = userService.updateProfile(updateProfileRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật thông tin cá nhân thành công", request));
    }

    /**
     * Đổi mật khẩu của người dùng hiện tại
     * 
     * Endpoint này cho phép người dùng đổi mật khẩu của bản thân
     * Yêu cầu cung cấp mật khẩu hiện tại để xác thực
     * 
     * @param changePasswordRequest DTO chứa thông tin đổi mật khẩu
     * @param request               HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng
     */
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequest,
            HttpServletRequest request) {
        UserResponseDto updatedUser = userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Đổi mật khẩu thành công", request));
    }

    /**
     * Tìm kiếm người dùng theo tên hoặc họ
     * 
     * Endpoint này cho phép người dùng tìm kiếm người dùng khác trong hệ thống
     * theo tên hoặc họ, có phân trang
     * 
     * @param searchRequest DTO chứa thông tin tìm kiếm
     * @param request       HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách người dùng tìm được
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> searchUsers(
            @Valid SearchUserRequestDto searchRequest,
            HttpServletRequest request) {
        Page<UserResponseDto> users = userService.searchUsers(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(users, "Tìm kiếm người dùng thành công", request));
    }

    /**
     * Lấy thông tin người dùng theo ID
     * 
     * Endpoint này cho phép người dùng xem thông tin công khai của người dùng khác
     * 
     * @param userId  ID của người dùng cần xem
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @PathVariable String userId,
            HttpServletRequest request) {
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công", request));
    }

    /**
     * Xóa ảnh đại diện của người dùng hiện tại
     * 
     * Endpoint này cho phép người dùng xóa ảnh đại diện của bản thân
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin người dùng đã xóa ảnh đại diện
     */
    @DeleteMapping("/profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> removeProfilePicture(HttpServletRequest request) {
        UserResponseDto updatedUser = userService.removeProfilePicture();
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Xóa ảnh đại diện thành công", request));
    }
}
