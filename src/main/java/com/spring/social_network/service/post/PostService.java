package com.spring.social_network.service.post;

import com.spring.social_network.dto.request.CreatePostRequest;
import com.spring.social_network.dto.request.UpdatePostRequest;
import com.spring.social_network.dto.response.FriendResponseDto;
import com.spring.social_network.dto.response.PostFeedResponse;
import com.spring.social_network.dto.response.PostResponse;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.model.User;
import com.spring.social_network.model.post.Post;
import com.spring.social_network.model.post.PostPrivacy;
import com.spring.social_network.repository.post.PostRepository;
import com.spring.social_network.service.FileUploadService;
import com.spring.social_network.service.FriendshipService;
import com.spring.social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;

    public PostResponse createPost(CreatePostRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Nội dung không được để trống");
        }

        if (request.getContent().length() > 1000) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Nội dung quá dài");
        }

        List<String> imageUrls = new java.util.ArrayList<>();
        List<String> videoUrls = new java.util.ArrayList<>();

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile file : request.getFiles()) {
                try {
                    String fileUrl = fileUploadService.uploadFile(file);

                    String contentType = file.getContentType();
                    if (contentType != null && contentType.startsWith("image/")) {
                        imageUrls.add(fileUrl);
                    } else if (contentType != null && contentType.startsWith("video/")) {
                        videoUrls.add(fileUrl);
                    }
                } catch (Exception e) {
                    throw new AppException(ErrorCode.FILE_UPLOAD_FAILED,
                            "Tải lên file thất bại: " + file.getOriginalFilename());
                }
            }
        }

        Post post = Post.builder()
                .content(request.getContent())
                .user(currentUser)
                .imageUrl(imageUrls)
                .videoUrl(videoUrls)
                .privacy(request.getPrivacy() != null ? request.getPrivacy() : PostPrivacy.PUBLIC)
                .hashtags(request.getHashtags() != null ? new java.util.HashSet<>(request.getHashtags()) : null)
                .build();

        Post savedPost = postRepository.save(post);
        return mapToPostResponse(savedPost, currentUser.getId());
    }

    public PostResponse updatePost(String postId, UpdatePostRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.POST_FORBIDDEN, "Bạn chỉ có thể chỉnh sửa bài viết của mình");
        }

        if (request.getContent() != null && request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Nội dung không được để trống");
        }

        if (request.getContent() != null && request.getContent().length() > 1000) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Nội dung quá dài");
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            List<String> imageUrls = new java.util.ArrayList<>();
            List<String> videoUrls = new java.util.ArrayList<>();

            for (MultipartFile file : request.getFiles()) {
                try {
                    String fileUrl = fileUploadService.uploadFile(file);

                    String contentType = file.getContentType();
                    if (contentType != null && contentType.startsWith("image/")) {
                        imageUrls.add(fileUrl);
                    } else if (contentType != null && contentType.startsWith("video/")) {
                        videoUrls.add(fileUrl);
                    }
                } catch (Exception e) {
                    throw new AppException(ErrorCode.FILE_UPLOAD_FAILED,
                            "Tải lên file thất bại: " + file.getOriginalFilename());
                }
            }

            if (!imageUrls.isEmpty()) {
                post.setImageUrl(imageUrls);
            }
            if (!videoUrls.isEmpty()) {
                post.setVideoUrl(videoUrls);
            }
        }
        if (request.getPrivacy() != null) {
            post.setPrivacy(request.getPrivacy());
        }
        if (request.getHashtags() != null) {
            post.setHashtags(new java.util.HashSet<>(request.getHashtags()));
        }

        Post updatedPost = postRepository.save(post);
        return mapToPostResponse(updatedPost, currentUser.getId());
    }

    public void deletePost(String postId) {
        User currentUser = userService.getCurrentUserEntity();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.POST_FORBIDDEN, "Bạn chỉ có thể xóa bài viết của mình");
        }

        postRepository.delete(post);
    }

    public PostFeedResponse getFeed(int page, int limit) {
        User currentUser = userService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<FriendResponseDto> friendsPage = friendshipService.getFriends(PageRequest.of(0, 1000));
        List<String> friendIds = friendsPage.getContent().stream()
                .map(friend -> friend.getFriend().getId())
                .collect(Collectors.toList());

        Page<Post> postsPage = postRepository.findFeedPosts(
                PostPrivacy.PUBLIC,
                PostPrivacy.FRIENDS,
                friendIds,
                currentUser.getId(),
                pageable);

        List<PostResponse> posts = postsPage.getContent().stream()
                .map(post -> mapToPostResponse(post, currentUser.getId()))
                .collect(Collectors.toList());

        return PostFeedResponse.builder()
                .posts(posts)
                .currentPage(page)
                .totalPages(postsPage.getTotalPages())
                .totalElements(postsPage.getTotalElements())
                .pageSize(limit)
                .build();
    }

    public PostResponse getPostById(String postId) {
        User currentUser = userService.getCurrentUserEntity();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, "Không tìm thấy bài viết"));

        if (!canViewPost(post, currentUser)) {
            throw new AppException(ErrorCode.POST_FORBIDDEN, "Bạn không có quyền xem bài viết này");
        }

        return mapToPostResponse(post, currentUser.getId());
    }

    public PostFeedResponse getUserPosts(String userId, int page, int limit) {
        User currentUser = userService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page - 1, limit);

        if (userId.equals(currentUser.getId())) {
            Page<Post> postsPage = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            return buildPostFeedResponse(postsPage, currentUser.getId(), page, limit);
        }

        boolean isFriend = friendshipService.getFriendshipStatus(userId).getStatus().equals("friends");

        if (isFriend) {

            Page<Post> publicPosts = postRepository.findByUserIdAndPrivacyOrderByCreatedAtDesc(userId,
                    PostPrivacy.PUBLIC, pageable);
            Page<Post> friendsPosts = postRepository.findByUserIdAndPrivacyOrderByCreatedAtDesc(userId,
                    PostPrivacy.FRIENDS, pageable);

            List<Post> allPosts = publicPosts.getContent();
            allPosts.addAll(friendsPosts.getContent());
            allPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

            int start = (page - 1) * limit;
            int end = Math.min(start + limit, allPosts.size());
            List<Post> paginatedPosts = allPosts.subList(start, end);

            List<PostResponse> posts = paginatedPosts.stream()
                    .map(post -> mapToPostResponse(post, currentUser.getId()))
                    .collect(Collectors.toList());

            return PostFeedResponse.builder()
                    .posts(posts)
                    .currentPage(page)
                    .totalPages((int) Math.ceil((double) allPosts.size() / limit))
                    .totalElements(allPosts.size())
                    .pageSize(limit)
                    .build();
        } else {

            Page<Post> postsPage = postRepository.findByUserIdAndPrivacyOrderByCreatedAtDesc(userId, PostPrivacy.PUBLIC,
                    pageable);
            return buildPostFeedResponse(postsPage, currentUser.getId(), page, limit);
        }
    }

    private boolean canViewPost(Post post, User currentUser) {

        if (post.getUser().getId().equals(currentUser.getId())) {
            return true;
        }

        if (post.getPrivacy() == PostPrivacy.PUBLIC) {
            return true;
        }

        if (post.getPrivacy() == PostPrivacy.FRIENDS) {
            return friendshipService.getFriendshipStatus(post.getUser().getId()).getStatus().equals("friends");
        }

        return false;
    }

    private PostResponse mapToPostResponse(Post post, String currentUserId) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setUser(userMapper.toResponseDto(post.getUser()));
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setImageUrls(post.getImageUrl());
        response.setVideoUrls(post.getVideoUrl());
        response.setPrivacy(post.getPrivacy());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setHashtags(post.getHashtags() != null ? new java.util.ArrayList<>(post.getHashtags()) : null);
        response.setIsLiked(false);

        return response;
    }

    private PostFeedResponse buildPostFeedResponse(Page<Post> postsPage, String currentUserId, int page, int limit) {
        List<PostResponse> posts = postsPage.getContent().stream()
                .map(post -> mapToPostResponse(post, currentUserId))
                .collect(Collectors.toList());

        return PostFeedResponse.builder()
                .posts(posts)
                .currentPage(page)
                .totalPages(postsPage.getTotalPages())
                .totalElements(postsPage.getTotalElements())
                .pageSize(limit)
                .build();
    }
}
