package com.spring.social_network.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDto {
    @NotBlank(message = "Mật khẩu hiện tại là bắt buộc")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới là bắt buộc")
    @Size(min = 8, max = 100, message = "Mật khẩu mới phải có từ 8 đến 100 ký tự")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới là bắt buộc")
    private String confirmNewPassword;

    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }
}
