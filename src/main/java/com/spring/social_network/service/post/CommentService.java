package com.spring.social_network.service.post;

import com.spring.social_network.dto.request.CreateCommentRequest;
import com.spring.social_network.dto.request.UpdateCommentRequest;
import com.spring.social_network.dto.response.CommentResponse;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.model.User;
import com.spring.social_network.model.post.Comment;
import com.spring.social_network.model.post.Post;
import com.spring.social_network.repository.post.CommentRepository;
import com.spring.social_network.repository.post.PostRepository;
import com.spring.social_network.repository.post.ReplyRepository;
import com.spring.social_network.service.FriendshipService;
import com.spring.social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final FriendshipService friendshipService;

    public CommentResponse createComment(String postId, CreateCommentRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết"));

        if (!canUserViewPost(currentUser, post)) {
            throw new AppException(ErrorCode.POST_FORBIDDEN, "Bạn không có quyền xem bài viết này");
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);

        updatePostCommentCount(postId);

        return mapToCommentResponse(savedComment);
    }

    public Page<CommentResponse> getCommentsByPostId(String postId, int page, int size) {
        User currentUser = userService.getCurrentUserEntity();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết"));

        if (!canUserViewPost(currentUser, post)) {
            throw new AppException(ErrorCode.POST_FORBIDDEN, "Bạn không có quyền xem bài viết này");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

        return comments.map(this::mapToCommentResponse);
    }

    public CommentResponse updateComment(String commentId, UpdateCommentRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, "Không tìm thấy comment"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.COMMENT_FORBIDDEN, "Bạn chỉ có thể chỉnh sửa comment của mình");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        return mapToCommentResponse(updatedComment);
    }

    public void deleteComment(String commentId) {
        User currentUser = userService.getCurrentUserEntity();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, "Không tìm thấy comment"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.COMMENT_FORBIDDEN, "Bạn chỉ có thể xóa comment của mình");
        }

        String postId = comment.getPost().getId();

        commentRepository.delete(comment);

        updatePostCommentCount(postId);
    }

    private boolean canUserViewPost(User user, Post post) {

        if (post.getUser().getId().equals(user.getId())) {
            return true;
        }

        if (post.getPrivacy() == com.spring.social_network.model.post.PostPrivacy.PUBLIC) {
            return true;
        }

        if (post.getPrivacy() == com.spring.social_network.model.post.PostPrivacy.FRIENDS) {
            return friendshipService.getFriendshipStatus(post.getUser().getId()).getStatus().equals("friends");
        }

        return false;
    }

    private void updatePostCommentCount(String postId) {
        long commentCount = commentRepository.countByPostId(postId);
        postRepository.updateCommentCount(postId, (int) commentCount);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        UserResponseDto userResponse = userMapper.toResponseDto(comment.getUser());

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userResponse)
                .postId(comment.getPost().getId())
                .replyCount((int) replyRepository.countByCommentId(comment.getId()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
