package com.spring.social_network.controller;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.request.SendFriendRequestDto;
import com.spring.social_network.dto.response.FriendResponseDto;
import com.spring.social_network.dto.response.FriendshipResponseDto;
import com.spring.social_network.dto.response.FriendshipStatusResponseDto;
import com.spring.social_network.service.FriendshipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các yêu cầu liên quan đến tình bạn
 * 
 * Cung cấp các endpoint để:
 * - Gửi lời mời kết bạn
 * - Xem danh sách lời mời đã gửi và nhận được
 * - Chấp nhận/từ chối lời mời kết bạn
 * - Hủy lời mời kết bạn
 * - Xem danh sách bạn bè
 * - Hủy kết bạn
 * - Kiểm tra trạng thái quan hệ giữa hai người dùng
 * 
 * Yêu cầu quyền: USER hoặc ADMIN
 * 
 * @author Spring Social Network Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/friendships")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    /**
     * Gửi lời mời kết bạn đến người dùng khác
     * 
     * @param request     Thông tin người dùng muốn kết bạn
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin lời mời kết bạn đã tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FriendshipResponseDto>> sendFriendRequest(
            @Valid @RequestBody SendFriendRequestDto request,
            HttpServletRequest httpRequest) {
        FriendshipResponseDto response = friendshipService.sendFriendRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gửi lời mời kết bạn thành công", httpRequest));
    }

    /**
     * Lấy danh sách lời mời kết bạn đã gửi (có phân trang)
     * 
     * @param page        Số trang (mặc định: 1)
     * @param limit       Số lượng item trên mỗi trang (mặc định: 20)
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách lời mời đã gửi
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<FriendshipResponseDto>>> getSentRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FriendshipResponseDto> response = friendshipService.getSentRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách lời mời đã gửi thành công", httpRequest));
    }

    /**
     * Lấy danh sách lời mời kết bạn đã nhận được (có phân trang)
     * 
     * @param page        Số trang (mặc định: 1)
     * @param limit       Số lượng item trên mỗi trang (mặc định: 20)
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách lời mời đã nhận
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<FriendshipResponseDto>>> getReceivedRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FriendshipResponseDto> response = friendshipService.getReceivedRequests(pageable);
        return ResponseEntity
                .ok(ApiResponse.success(response, "Lấy danh sách lời mời nhận được thành công", httpRequest));
    }

    /**
     * Hủy lời mời kết bạn đã gửi
     * 
     * @param id          ID của lời mời kết bạn cần hủy
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity xác nhận hủy lời mời thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelFriendRequest(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        friendshipService.cancelFriendRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy lời mời kết bạn thành công", httpRequest));
    }

    /**
     * Chấp nhận lời mời kết bạn
     * 
     * @param id          ID của lời mời kết bạn cần chấp nhận
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity xác nhận chấp nhận lời mời thành công
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<String>> acceptFriendRequest(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        friendshipService.acceptFriendRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận lời mời kết bạn thành công", httpRequest));
    }

    /**
     * Từ chối lời mời kết bạn
     * 
     * @param id          ID của lời mời kết bạn cần từ chối
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity xác nhận từ chối lời mời thành công
     */
    @PatchMapping("/{id}/decline")
    public ResponseEntity<ApiResponse<String>> declineFriendRequest(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        friendshipService.declineFriendRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Từ chối lời mời kết bạn thành công", httpRequest));
    }

    /**
     * Lấy danh sách bạn bè hiện tại (có phân trang)
     * 
     * @param page        Số trang (mặc định: 1)
     * @param limit       Số lượng item trên mỗi trang (mặc định: 20)
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa danh sách bạn bè
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FriendResponseDto>>> getFriends(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FriendResponseDto> response = friendshipService.getFriends(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách bạn bè thành công", httpRequest));
    }

    /**
     * Hủy kết bạn với người dùng khác
     * 
     * @param id          ID của mối quan hệ bạn bè cần hủy
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity xác nhận hủy kết bạn thành công
     */
    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        friendshipService.removeFriend(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy kết bạn thành công", httpRequest));
    }

    /**
     * Kiểm tra trạng thái quan hệ giữa người dùng hiện tại và người dùng khác
     * 
     * @param userId      ID của người dùng cần kiểm tra quan hệ
     * @param httpRequest HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin trạng thái quan hệ
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<FriendshipStatusResponseDto>> getFriendshipStatus(
            @RequestParam String userId,
            HttpServletRequest httpRequest) {
        FriendshipStatusResponseDto response = friendshipService.getFriendshipStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Kiểm tra trạng thái quan hệ thành công", httpRequest));
    }
}
