package com.spring.social_network.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.spring.social_network.dto.request.CreateUserRequestDto;
import com.spring.social_network.dto.request.UpdateUserRequestDto;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toResponseDto(User user);

    List<UserResponseDto> toResponseDtoList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequestDto createUserRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequestDto(UpdateUserRequestDto updateUserRequestDto, @MappingTarget User user);
}
