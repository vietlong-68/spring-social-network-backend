package com.spring.social_network.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.spring.social_network.model.RoleType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequestDto {

    @NotNull(message = "Loại role là bắt buộc")
    private RoleType roleType;

    @NotBlank(message = "Lý do thay đổi role là bắt buộc")
    private String reason;
}
