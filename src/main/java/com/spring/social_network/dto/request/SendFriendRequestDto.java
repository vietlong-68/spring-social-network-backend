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

    @NotBlank(message = "ID người nhận là bắt buộc")
    private String receiverId;
}
