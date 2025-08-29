package com.spring.social_network.service;

import org.springframework.stereotype.Service;

import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.repository.UserRepository;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.dto.request.UpdateProfileRequestDto;
import com.spring.social_network.dto.request.ChangePasswordRequestDto;
import com.spring.social_network.dto.request.SearchUserRequestDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.model.User;
import com.spring.social_network.service.FileUploadService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, FileUploadService fileUploadService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

        return userMapper.toResponseDto(user);
    }

    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

        return user;
    }

    public List<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public UserResponseDto uploadProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ cho phép file ảnh");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Kích thước file không được vượt quá 5MB");
        }

        User currentUser = getCurrentUserEntity();

        try {

            String fileUrl = fileUploadService.uploadFile(file);

            currentUser.setProfilePictureUrl(fileUrl);
            User updatedUser = userRepository.save(currentUser);

            return userMapper.toResponseDto(updatedUser);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Tải lên ảnh đại diện thất bại: " + e.getMessage());
        }
    }

    public UserResponseDto updateProfile(UpdateProfileRequestDto updateProfileRequest) {
        User currentUser = getCurrentUserEntity();

        if (updateProfileRequest.getFirstName() != null) {
            currentUser.setFirstName(updateProfileRequest.getFirstName());
        }
        if (updateProfileRequest.getLastName() != null) {
            currentUser.setLastName(updateProfileRequest.getLastName());
        }
        if (updateProfileRequest.getGender() != null) {
            currentUser.setGender(updateProfileRequest.getGender());
        }
        if (updateProfileRequest.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(updateProfileRequest.getDateOfBirth());
        }
        if (updateProfileRequest.getPhone() != null) {
            currentUser.setPhone(updateProfileRequest.getPhone());
        }
        if (updateProfileRequest.getAddress() != null) {
            currentUser.setAddress(updateProfileRequest.getAddress());
        }

        User updatedUser = userRepository.save(currentUser);

        return userMapper.toResponseDto(updatedUser);
    }

    public UserResponseDto removeProfilePicture() {
        User currentUser = getCurrentUserEntity();

        currentUser.setProfilePictureUrl(null);
        User updatedUser = userRepository.save(currentUser);

        return userMapper.toResponseDto(updatedUser);
    }

    public UserResponseDto changePassword(ChangePasswordRequestDto changePasswordRequest) {

        if (!changePasswordRequest.isPasswordMatching()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu mới và xác nhận mật khẩu không khớp nhau");
        }

        User currentUser = getCurrentUserEntity();

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), currentUser.getPassword())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu mới không được giống mật khẩu cũ");
        }

        String encodedNewPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        currentUser.setPassword(encodedNewPassword);

        User updatedUser = userRepository.save(currentUser);

        return userMapper.toResponseDto(updatedUser);
    }

    public Page<UserResponseDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponseDto);
    }

    public Page<UserResponseDto> searchUsers(SearchUserRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        if (searchRequest.getSearchTerm() == null || searchRequest.getSearchTerm().trim().isEmpty()) {

            Page<User> users = userRepository.findAll(pageable);
            return users.map(userMapper::toResponseDto);
        }

        Page<User> users = userRepository.findByFirstNameOrLastNameContainingIgnoreCase(
                searchRequest.getSearchTerm().trim(), pageable);

        return users.map(userMapper::toResponseDto);
    }

    public UserResponseDto getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

        return userMapper.toResponseDto(user);
    }
}