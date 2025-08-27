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
public class UserBlockRequestDto {

    @NotBlank(message = "Lý do block là bắt buộc")
    @Size(max = 500, message = "Lý do block không được vượt quá 500 ký tự")
    private String reason;
}
