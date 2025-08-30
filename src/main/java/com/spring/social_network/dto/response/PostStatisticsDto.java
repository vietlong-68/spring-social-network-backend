package com.spring.social_network.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatisticsDto {

    private Long totalPosts;
    private Long postsToday;
    private Long postsThisWeek;
    private Long postsThisMonth;
    private Long publicPosts;
    private Long privatePosts;
    private Long postsWithImages;
    private Long postsWithVideos;
    private Long postsWithHashtags;
    private Map<String, Long> topHashtags;
    private Double averageLikesPerPost;
    private Double averageCommentsPerPost;
}
