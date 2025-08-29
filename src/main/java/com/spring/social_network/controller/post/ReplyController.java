package com.spring.social_network.controller.post;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.request.CreateReplyRequest;
import com.spring.social_network.dto.request.UpdateReplyRequest;
import com.spring.social_network.dto.response.ReplyResponse;
import com.spring.social_network.service.post.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            @PathVariable String commentId,
            @Valid @RequestBody CreateReplyRequest request) {

        ReplyResponse reply = replyService.createReply(commentId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reply, "Tạo reply thành công"));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<Page<ReplyResponse>>> getReplies(
            @PathVariable String commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ReplyResponse> replies = replyService.getRepliesByCommentId(commentId, page, size);

        return ResponseEntity.ok(ApiResponse.success(replies, "Lấy danh sách reply thành công"));
    }

    @PatchMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<ReplyResponse>> updateReply(
            @PathVariable String replyId,
            @Valid @RequestBody UpdateReplyRequest request) {

        ReplyResponse reply = replyService.updateReply(replyId, request);

        return ResponseEntity.ok(ApiResponse.success(reply, "Cập nhật reply thành công"));
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(@PathVariable String replyId) {

        replyService.deleteReply(replyId);

        return ResponseEntity.ok(ApiResponse.success("Xóa reply thành công"));
    }
}
