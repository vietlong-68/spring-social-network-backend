package com.spring.social_network.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.social_network.dto.request.CreateUserRequestDto;
import com.spring.social_network.dto.request.UpdateUserRequestDto;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.model.User;
import com.spring.social_network.model.Role;
import com.spring.social_network.model.RoleType;
import com.spring.social_network.repository.UserRepository;
import com.spring.social_network.repository.RoleRepository;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AdminUserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseDtoList(users);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(String id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserResponseDto userResponseDto = userMapper.toResponseDto(user);
            return userResponseDto;
        } else {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id);
        }
    }

    public UserResponseDto create(CreateUserRequestDto createUserRequestDto) {
        if (userRepository.existsByEmail(createUserRequestDto.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                    "Email đã tồn tại: " + createUserRequestDto.getEmail());
        }

        User user = userMapper.toEntity(createUserRequestDto);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Không tìm thấy role USER mặc định"));

        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    public UserResponseDto update(String id, UpdateUserRequestDto updateUserRequestDto) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            userMapper.updateEntityFromRequestDto(updateUserRequestDto, user);
            User updatedUser = userRepository.save(user);
            return userMapper.toResponseDto(updatedUser);
        } else {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id);
        }
    }

    public void delete(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id);
        }
    }
}
