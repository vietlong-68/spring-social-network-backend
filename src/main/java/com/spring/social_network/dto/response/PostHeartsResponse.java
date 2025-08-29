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
public class PostHeartsResponse {
    private List<HeartResponse> hearts;
    private boolean currentUserHearted;
    private long totalHearts;
    private int currentPage;
    private int totalPages;
}
