package com.spring.social_network.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.social_network.dto.request.CreateUserRequestDto;
import com.spring.social_network.dto.request.UpdateUserRequestDto;
import com.spring.social_network.dto.request.UserBlockRequestDto;
import com.spring.social_network.dto.request.UserRoleRequestDto;
import com.spring.social_network.dto.response.UserResponseDto;
import com.spring.social_network.dto.response.UserStatisticsDto;
import com.spring.social_network.dto.response.PostStatisticsDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.model.User;
import com.spring.social_network.model.Role;
import com.spring.social_network.model.RoleType;
import com.spring.social_network.model.post.PostPrivacy;
import com.spring.social_network.repository.UserRepository;
import com.spring.social_network.repository.RoleRepository;
import com.spring.social_network.repository.post.PostRepository;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;

    public AdminUserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.postRepository = postRepository;
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
                .orElseThrow(
                        () -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Không tìm thấy role USER mặc định"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto update(String id, UpdateUserRequestDto updateUserRequestDto) {
        try {

            
            Optional<User> existingUser = userRepository.findById(id);
            if (!existingUser.isPresent()) {
                throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id);
            }
            
            User user = existingUser.get();
            
            if (updateUserRequestDto.getEmail() != null && 
                !updateUserRequestDto.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(updateUserRequestDto.getEmail())) {
                    throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                            "Email đã tồn tại: " + updateUserRequestDto.getEmail());
                }
            }
            
            validateUpdateData(updateUserRequestDto);
            
            validateUserEntity(user);
            
            Set<Role> currentRoles = user.getRoles();
            
            userMapper.updateEntityFromRequestDto(updateUserRequestDto, user);
            
            if (user.getRoles() == null) {
                user.setRoles(currentRoles);
            }
            
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Tên không được để trống");
            }
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Họ không được để trống");
            }
            
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                Role defaultRole = roleRepository.findByName(RoleType.USER)
                    .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR, 
                        "Không tìm thấy role USER mặc định"));
                user.setRoles(Set.of(defaultRole));
            }
            
            User updatedUser = userRepository.save(user);
            return userMapper.toResponseDto(updatedUser);
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Không thể cập nhật người dùng: " + e.getMessage());
        }
    }
    
    private void validateUpdateData(UpdateUserRequestDto updateUserRequestDto) {
        if (updateUserRequestDto.getFirstName() != null) {
            if (updateUserRequestDto.getFirstName().trim().isEmpty()) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Tên không được để trống");
            }
            if (updateUserRequestDto.getFirstName().length() > 50) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Tên không được vượt quá 50 ký tự");
            }
        }
        
        if (updateUserRequestDto.getLastName() != null) {
            if (updateUserRequestDto.getLastName().trim().isEmpty()) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Họ không được để trống");
            }
            if (updateUserRequestDto.getLastName().length() > 50) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Họ không được vượt quá 50 ký tự");
            }
        }
        
        if (updateUserRequestDto.getEmail() != null) {
            if (updateUserRequestDto.getEmail().trim().isEmpty()) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Email không được để trống");
            }
            if (!updateUserRequestDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Email không đúng định dạng");
            }
        }
        
        if (updateUserRequestDto.getPhone() != null && !updateUserRequestDto.getPhone().trim().isEmpty()) {
            if (updateUserRequestDto.getPhone().length() > 20) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Số điện thoại không được vượt quá 20 ký tự");
            }
        }
        
        if (updateUserRequestDto.getAddress() != null && !updateUserRequestDto.getAddress().trim().isEmpty()) {
            if (updateUserRequestDto.getAddress().length() > 255) {
                throw new AppException(ErrorCode.USER_INVALID_INPUT, "Địa chỉ không được vượt quá 255 ký tự");
            }
        }
    }
    
    private void validateUserEntity(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Tên không được để trống");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Họ không được để trống");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Email không được để trống");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Mật khẩu không được để trống");
        }
        
        if (user.getFirstName().length() > 50) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Tên không được vượt quá 50 ký tự");
        }
        if (user.getLastName().length() > 50) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Họ không được vượt quá 50 ký tự");
        }
        if (user.getEmail().length() > 100) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Email không được vượt quá 100 ký tự");
        }
        if (user.getPhone() != null && user.getPhone().length() > 20) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Số điện thoại không được vượt quá 20 ký tự");
        }
        if (user.getAddress() != null && user.getAddress().length() > 255) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Địa chỉ không được vượt quá 255 ký tự");
        }
        if (user.getBlockReason() != null && user.getBlockReason().length() > 500) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Lý do khóa không được vượt quá 500 ký tự");
        }
        
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Email không đúng định dạng");
        }
        
        if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Ngày sinh phải trong quá khứ");
        }
        
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new AppException(ErrorCode.USER_INVALID_INPUT, "Người dùng phải có ít nhất một vai trò");
        }
        
        if (user.getIsBlocked() == null) {
            user.setIsBlocked(false);
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

    public UserResponseDto blockUser(String id, UserBlockRequestDto blockRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id));

        user.setIsBlocked(true);
        user.setBlockReason(blockRequest.getReason());
        user.setBlockedAt(LocalDateTime.now());

        User blockedUser = userRepository.save(user);
        return userMapper.toResponseDto(blockedUser);
    }

    public UserResponseDto unblockUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id));

        user.setIsBlocked(false);
        user.setBlockReason(null);
        user.setBlockedAt(null);

        User unblockedUser = userRepository.save(user);
        return userMapper.toResponseDto(unblockedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findBlockedUsers() {
        List<User> blockedUsers = userRepository.findByIsBlockedTrue();
        return userMapper.toResponseDtoList(blockedUsers);
    }

    public UserResponseDto updateUserRole(String id, UserRoleRequestDto roleRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id));

        Role newRole = roleRepository.findByName(roleRequest.getRoleType())
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Không tìm thấy role: " + roleRequest.getRoleType()));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        user.getRoles().clear();
        user.getRoles().add(newRole);

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserStatisticsDto getUserStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);

        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByIsBlockedFalse();
        Long blockedUsers = userRepository.countByIsBlockedTrue();
        Long newUsersToday = userRepository.countByCreatedAtAfter(today.atStartOfDay());
        Long newUsersThisWeek = userRepository.countByCreatedAtAfter(weekStart.atStartOfDay());
        Long newUsersThisMonth = userRepository.countByCreatedAtAfter(monthStart.atStartOfDay());
        Long maleUsers = userRepository.countByGender(com.spring.social_network.model.Gender.MALE);
        Long femaleUsers = userRepository.countByGender(com.spring.social_network.model.Gender.FEMALE);
        Long usersWithProfilePicture = userRepository.countByProfilePictureUrlIsNotNull();
        Long usersWithPhone = userRepository.countByPhoneIsNotNull();
        Long usersWithAddress = userRepository.countByAddressIsNotNull();

        return UserStatisticsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .newUsersToday(newUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .maleUsers(maleUsers)
                .femaleUsers(femaleUsers)
                .usersWithProfilePicture(usersWithProfilePicture)
                .usersWithPhone(usersWithPhone)
                .usersWithAddress(usersWithAddress)
                .build();
    }

    @Transactional(readOnly = true)
    public PostStatisticsDto getPostStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);

        Long totalPosts = postRepository.count();
        Long postsToday = postRepository.countByCreatedAtAfter(today.atStartOfDay());
        Long postsThisWeek = postRepository.countByCreatedAtAfter(weekStart.atStartOfDay());
        Long postsThisMonth = postRepository.countByCreatedAtAfter(monthStart.atStartOfDay());
        Long publicPosts = postRepository.countByPrivacy(PostPrivacy.PUBLIC);
        Long privatePosts = postRepository.countByPrivacy(PostPrivacy.PRIVATE);
        Long postsWithImages = postRepository.countByImageUrlIsNotEmpty();
        Long postsWithVideos = postRepository.countByVideoUrlIsNotEmpty();
        Long postsWithHashtags = postRepository.countByHashtagsIsNotEmpty();

        Double averageLikesPerPost = postRepository.findAll().stream()
                .mapToInt(post -> post.getLikeCount())
                .average()
                .orElse(0.0);

        Double averageCommentsPerPost = postRepository.findAll().stream()
                .mapToInt(post -> post.getCommentCount())
                .average()
                .orElse(0.0);

        Map<String, Long> topHashtags = postRepository.findAll().stream()
                .filter(post -> post.getHashtags() != null && !post.getHashtags().isEmpty())
                .flatMap(post -> post.getHashtags().stream())
                .collect(Collectors.groupingBy(hashtag -> hashtag, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return PostStatisticsDto.builder()
                .totalPosts(totalPosts)
                .postsToday(postsToday)
                .postsThisWeek(postsThisWeek)
                .postsThisMonth(postsThisMonth)
                .publicPosts(publicPosts)
                .privatePosts(privatePosts)
                .postsWithImages(postsWithImages)
                .postsWithVideos(postsWithVideos)
                .postsWithHashtags(postsWithHashtags)
                .topHashtags(topHashtags)
                .averageLikesPerPost(averageLikesPerPost)
                .averageCommentsPerPost(averageCommentsPerPost)
                .build();
    }
}
