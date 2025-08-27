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

@RestController
@RequestMapping("/friendships")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FriendshipResponseDto>> sendFriendRequest(
            @Valid @RequestBody SendFriendRequestDto request,
            HttpServletRequest httpRequest) {
        FriendshipResponseDto response = friendshipService.sendFriendRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gửi lời mời kết bạn thành công", httpRequest));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<FriendshipResponseDto>>> getSentRequests(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FriendshipResponseDto> response = friendshipService.getSentRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách lời mời đã gửi thành công", httpRequest));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<FriendshipResponseDto>>> getReceivedRequests(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FriendshipResponseDto> response = friendshipService.getReceivedRequests(pageable);
        return ResponseEntity
                .ok(ApiResponse.success(response, "Lấy danh sách lời mời nhận được thành công", httpRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelFriendRequest(
            @PathVariable(name = "id") String id,
            HttpServletRequest httpRequest) {
        friendshipService.cancelFriendRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy lời mời kết bạn thành công", httpRequest));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<String>> acceptFriendRequest(
            @PathVariable(name = "id") String id,
            HttpServletRequest httpRequest) {
        friendshipService.acceptFriendRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận lời mời kết bạn thành công", httpRequest));
    }

    @PatchMapping("/{id}/decline")
    public ResponseEntity<ApiResponse<String>> declineFriendRequest(
            @PathVariable(name = "id") String id,
            HttpServletRequest httpRequest) {
        friendshipService.declineFriendRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Từ chối lời mời kết bạn thành công", httpRequest));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FriendResponseDto>>> getFriends(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<FriendResponseDto> response = friendshipService.getFriends(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách bạn bè thành công", httpRequest));
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @PathVariable(name = "id") String id,
            HttpServletRequest httpRequest) {
        friendshipService.removeFriend(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy kết bạn thành công", httpRequest));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<FriendshipStatusResponseDto>> getFriendshipStatus(
            @RequestParam(name = "userId") String userId,
            HttpServletRequest httpRequest) {
        FriendshipStatusResponseDto response = friendshipService.getFriendshipStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Kiểm tra trạng thái quan hệ thành công", httpRequest));
    }
}
