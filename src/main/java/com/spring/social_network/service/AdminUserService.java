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
import com.spring.social_network.model.post.Post;
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
