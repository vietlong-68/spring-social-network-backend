package com.spring.social_network.service.post;

import com.spring.social_network.dto.request.CreateReplyRequest;
import com.spring.social_network.dto.request.UpdateReplyRequest;
import com.spring.social_network.dto.response.ReplyResponse;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.model.User;
import com.spring.social_network.model.post.Comment;
import com.spring.social_network.model.post.Reply;
import com.spring.social_network.repository.post.CommentRepository;
import com.spring.social_network.repository.post.ReplyRepository;
import com.spring.social_network.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    public ReplyResponse createReply(String commentId, CreateReplyRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, "Không tìm thấy comment"));

        Reply reply = Reply.builder()
                .content(request.getContent())
                .user(currentUser)
                .comment(comment)
                .build();

        Reply savedReply = replyRepository.save(reply);

        return mapToReplyResponse(savedReply);
    }

    public Page<ReplyResponse> getRepliesByCommentId(String commentId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Reply> replies = replyRepository.findByCommentIdOrderByCreatedAtAsc(commentId, pageable);

        return replies.map(this::mapToReplyResponse);
    }

    public ReplyResponse updateReply(String replyId, UpdateReplyRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new AppException(ErrorCode.REPLY_NOT_FOUND, "Không tìm thấy reply"));

        if (!reply.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.REPLY_FORBIDDEN, "Bạn chỉ có thể chỉnh sửa reply của mình");
        }

        reply.setContent(request.getContent());
        Reply updatedReply = replyRepository.save(reply);

        return mapToReplyResponse(updatedReply);
    }

    public void deleteReply(String replyId) {
        User currentUser = userService.getCurrentUserEntity();

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new AppException(ErrorCode.REPLY_NOT_FOUND, "Không tìm thấy reply"));

        if (!reply.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.REPLY_FORBIDDEN, "Bạn chỉ có thể xóa reply của mình");
        }

        replyRepository.delete(reply);
    }

    private ReplyResponse mapToReplyResponse(Reply reply) {
        UserResponseDto userResponse = userMapper.toResponseDto(reply.getUser());

        return ReplyResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .user(userResponse)
                .commentId(reply.getComment().getId())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}
