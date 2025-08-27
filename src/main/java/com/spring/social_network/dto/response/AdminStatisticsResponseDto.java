package com.spring.social_network.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsResponseDto {

    private UserStatisticsDto userStatistics;
    private PostStatisticsDto postStatistics;
    private String generatedAt;
}
