package com.spring.social_network.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyRequest {
    @NotBlank(message = "Nội dung reply là bắt buộc")
    @Size(max = 1000, message = "Nội dung reply không được vượt quá 1000 ký tự")
    private String content;
}
