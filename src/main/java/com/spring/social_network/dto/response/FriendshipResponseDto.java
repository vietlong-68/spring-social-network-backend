package com.spring.social_network.dto.response;

import com.spring.social_network.model.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponseDto {
    private String id;
    private UserResponseDto sender;
    private UserResponseDto receiver;
    private FriendshipStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
}
