package com.spring.social_network.dto.request;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.spring.social_network.model.Gender;
import com.spring.social_network.model.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Mật khẩu phải có độ dài từ 8 đến 20 ký tự")
    private String password;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private Set<Role> roles;
}
