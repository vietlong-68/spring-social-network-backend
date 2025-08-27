package com.spring.social_network.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.spring.social_network.model.Gender;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
    private String firstName;

    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ chỉ được chứa chữ cái và khoảng trắng")
    private String lastName;

    private Gender gender;

    @Past(message = "Ngày sinh phải trong quá khứ")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,15}$", message = "Số điện thoại phải hợp lệ")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;
}
