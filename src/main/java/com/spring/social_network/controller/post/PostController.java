package com.spring.social_network.controller.post;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.dto.request.CreatePostRequest;
import com.spring.social_network.dto.request.UpdatePostRequest;
import com.spring.social_network.dto.response.PostFeedResponse;
import com.spring.social_network.dto.response.PostResponse;
import com.spring.social_network.model.post.PostPrivacy;
import com.spring.social_network.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

        private final PostService postService;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @PostMapping
        public ResponseEntity<ApiResponse<PostResponse>> createPost(
                        @RequestParam(name = "content") String content,
                        @RequestParam(name = "files", required = false) List<MultipartFile> files,
                        @RequestParam(name = "privacy", required = false) String privacy,
                        @RequestParam(name = "hashtags", required = false) String hashtags) {

                List<String> parsedHashtags = new ArrayList<>();
                if (hashtags != null && !hashtags.trim().isEmpty()) {
                        try {
                                parsedHashtags = objectMapper.readValue(hashtags, new TypeReference<List<String>>() {
                                });
                        } catch (Exception e) {
                                // Fallback: tạo list với một hashtag
                                parsedHashtags = new ArrayList<>();
                                parsedHashtags.add(hashtags.trim());
                        }
                }

                CreatePostRequest request = CreatePostRequest.builder()
                                .content(content)
                                .files(files)
                                .privacy(privacy != null ? PostPrivacy.valueOf(privacy) : PostPrivacy.PUBLIC)
                                .hashtags(parsedHashtags)
                                .build();

                PostResponse post = postService.createPost(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<PostResponse>builder()
                                                .success(true)
                                                .message("Tạo bài viết thành công")
                                                .data(post)
                                                .build());
        }

        @PatchMapping("/{id}")
        public ResponseEntity<ApiResponse<PostResponse>> updatePost(
                        @PathVariable(name = "id") String id,
                        @RequestParam(name = "content", required = false) String content,
                        @RequestParam(name = "files", required = false) List<MultipartFile> files,
                        @RequestParam(name = "privacy", required = false) String privacy,
                        @RequestParam(name = "hashtags", required = false) String hashtags) {

                List<String> parsedHashtags = new ArrayList<>();
                if (hashtags != null && !hashtags.trim().isEmpty()) {
                        try {
                                parsedHashtags = objectMapper.readValue(hashtags, new TypeReference<List<String>>() {
                                });
                        } catch (Exception e) {
                                // Fallback: tạo list với một hashtag
                                parsedHashtags = new ArrayList<>();
                                parsedHashtags.add(hashtags.trim());
                        }
                }

                UpdatePostRequest request = UpdatePostRequest.builder()
                                .content(content)
                                .files(files)
                                .privacy(privacy != null ? PostPrivacy.valueOf(privacy) : null)
                                .hashtags(parsedHashtags)
                                .build();

                PostResponse post = postService.updatePost(id, request);
                return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                                .success(true)
                                .message("Cập nhật bài viết thành công")
                                .data(post)
                                .build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable(name = "id") String id) {
                postService.deletePost(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Xóa bài viết thành công")
                                .build());
        }

        @GetMapping("/feed")
        public ResponseEntity<ApiResponse<PostFeedResponse>> getFeed(
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "limit", defaultValue = "20") int limit) {
                PostFeedResponse feed = postService.getFeed(page, limit);
                return ResponseEntity.ok(ApiResponse.<PostFeedResponse>builder()
                                .success(true)
                                .message("Lấy feed thành công")
                                .data(feed)
                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable(name = "id") String id) {
                PostResponse post = postService.getPostById(id);
                return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                                .success(true)
                                .message("Lấy bài viết thành công")
                                .data(post)
                                .build());
        }

        @GetMapping("/users/{userId}/posts")
        public ResponseEntity<ApiResponse<PostFeedResponse>> getUserPosts(
                        @PathVariable(name = "userId") String userId,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "limit", defaultValue = "20") int limit) {
                PostFeedResponse posts = postService.getUserPosts(userId, page, limit);
                return ResponseEntity.ok(ApiResponse.<PostFeedResponse>builder()
                                .success(true)
                                .message("Lấy bài viết của người dùng thành công")
                                .data(posts)
                                .build());
        }
}
