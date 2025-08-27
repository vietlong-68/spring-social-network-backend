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
public class LoginRequest {
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Mật khẩu phải có độ dài từ 8 đến 20 ký tự")
    private String password;
}
