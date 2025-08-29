package com.spring.social_network.dto.response;

import com.spring.social_network.model.post.PostPrivacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private String id;
    private String content;
    private UserResponseDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private PostPrivacy privacy;
    private Integer likeCount;
    private Integer commentCount;
    private List<String> hashtags;
    private Boolean isLiked;
}
