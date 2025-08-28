package com.spring.social_network.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequestDto {
    
    @NotBlank(message = "Receiver ID is required")
    private String receiverId;
}
