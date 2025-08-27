package com.spring.social_network.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFeedResponse {
    private List<PostResponse> posts;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
