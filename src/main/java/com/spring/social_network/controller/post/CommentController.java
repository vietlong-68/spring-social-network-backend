package com.spring.social_network.controller.post;

import com.spring.social_network.dto.request.CreateCommentRequest;
import com.spring.social_network.dto.request.UpdateCommentRequest;
import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.response.CommentResponse;
import com.spring.social_network.service.post.CommentService;
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
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable(name = "postId") String postId,
            @Valid @RequestBody CreateCommentRequest request) {

        CommentResponse comment = commentService.createComment(postId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(comment, "Tạo comment thành công"));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable(name = "postId") String postId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, page, size);

        return ResponseEntity.ok(ApiResponse.success(comments, "Lấy danh sách comment thành công"));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable(name = "commentId") String commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        CommentResponse comment = commentService.updateComment(commentId, request);

        return ResponseEntity.ok(ApiResponse.success(comment, "Cập nhật comment thành công"));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable(name = "commentId") String commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.ok(ApiResponse.success("Xóa comment thành công"));
    }
}
