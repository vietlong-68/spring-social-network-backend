package com.spring.social_network.controller.post;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.response.HeartResponse;
import com.spring.social_network.dto.response.PostHeartsResponse;
import com.spring.social_network.service.post.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/{postId}/hearts")
    public ResponseEntity<ApiResponse<HeartResponse>> heartPost(@PathVariable(name = "postId") String postId) {
        HeartResponse heartResponse = reactionService.heartPost(postId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<HeartResponse>builder()
                        .success(true)
                        .message("Thả tim thành công")
                        .data(heartResponse)
                        .build());
    }

    @DeleteMapping("/{postId}/hearts")
    public ResponseEntity<ApiResponse<Void>> unheartPost(@PathVariable(name = "postId") String postId) {
        reactionService.unheartPost(postId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Bỏ tim thành công")
                .build());
    }

    @GetMapping("/{postId}/hearts")
    public ResponseEntity<ApiResponse<PostHeartsResponse>> getPostHearts(
            @PathVariable(name = "postId") String postId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {

        PostHeartsResponse response = reactionService.getPostHearts(postId, page, limit);
        return ResponseEntity.ok(ApiResponse.<PostHeartsResponse>builder()
                .success(true)
                .message("Lấy danh sách tim thành công")
                .data(response)
                .build());
    }
}
