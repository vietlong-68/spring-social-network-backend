package com.spring.social_network.service.post;

import com.spring.social_network.dto.response.HeartResponse;
import com.spring.social_network.dto.response.PostHeartsResponse;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.model.User;
import com.spring.social_network.model.post.Post;
import com.spring.social_network.model.post.Reaction;
import com.spring.social_network.model.post.ReactionType;
import com.spring.social_network.repository.post.PostRepository;
import com.spring.social_network.repository.post.ReactionRepository;
import com.spring.social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    private static final ReactionType DEFAULT_REACTION_TYPE = ReactionType.LOVE;

    @Transactional
    public HeartResponse heartPost(String postId) {
        User currentUser = userService.getCurrentUserEntity();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết"));

        if (reactionRepository.existsByPostIdAndUserIdAndType(postId, currentUser.getId(), DEFAULT_REACTION_TYPE)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Bạn đã thả tim bài viết này rồi");
        }

        Reaction reaction = Reaction.builder()
                .type(DEFAULT_REACTION_TYPE)
                .user(currentUser)
                .post(post)
                .build();

        Reaction savedReaction = reactionRepository.save(reaction);
        return mapToHeartResponse(savedReaction);
    }

    @Transactional
    public void unheartPost(String postId) {
        User currentUser = userService.getCurrentUserEntity();

        Reaction reaction = reactionRepository
                .findByPostIdAndUserIdAndType(postId, currentUser.getId(), DEFAULT_REACTION_TYPE)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Bạn chưa thả tim bài viết này"));

        reactionRepository.delete(reaction);
    }

    public PostHeartsResponse getPostHearts(String postId, int page, int limit) {
        User currentUser = userService.getCurrentUserEntity();

        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết");
        }

        boolean currentUserHearted = reactionRepository.existsByPostIdAndUserIdAndType(
                postId, currentUser.getId(), DEFAULT_REACTION_TYPE);

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Reaction> reactionPage = reactionRepository.findByPostIdAndType(postId, DEFAULT_REACTION_TYPE, pageable);

        long totalHearts = reactionRepository.countByPostIdAndType(postId, DEFAULT_REACTION_TYPE);

        List<HeartResponse> hearts = reactionPage.getContent().stream()
                .map(this::mapToHeartResponse)
                .collect(Collectors.toList());

        return PostHeartsResponse.builder()
                .hearts(hearts)
                .currentUserHearted(currentUserHearted)
                .totalHearts(totalHearts)
                .currentPage(page)
                .totalPages(reactionPage.getTotalPages())
                .build();
    }

    private HeartResponse mapToHeartResponse(Reaction reaction) {
        return HeartResponse.builder()
                .id(reaction.getId())
                .userId(reaction.getUser().getId())
                .userFirstName(reaction.getUser().getFirstName())
                .userLastName(reaction.getUser().getLastName())
                .userProfilePicture(reaction.getUser().getProfilePictureUrl())
                .postId(reaction.getPost().getId())
                .type(reaction.getType().name())
                .createdAt(reaction.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
