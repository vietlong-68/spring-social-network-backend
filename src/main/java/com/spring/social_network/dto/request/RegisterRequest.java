package com.spring.social_network.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.spring.social_network.model.Gender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 20, message = "Mật khẩu phải có độ dài từ 8 đến 20 ký tự")
    private String password;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 50, message = "Tên phải có độ dài từ 2 đến 50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(min = 2, max = 50, message = "Họ phải có độ dài từ 2 đến 50 ký tự")
    private String lastName;

    private Gender gender;

    private LocalDate dateOfBirth;

    @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
    private String phone;

    @Size(max = 200, message = "Địa chỉ không được quá 200 ký tự")
    private String address;
}
