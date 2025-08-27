package com.spring.social_network.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyResponse {
    private String id;
    private String content;
    private UserResponseDto user;
    private String commentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
