package com.spring.social_network.service;

import com.spring.social_network.dto.request.SendFriendRequestDto;
import com.spring.social_network.dto.response.FriendResponseDto;
import com.spring.social_network.dto.response.FriendshipResponseDto;
import com.spring.social_network.dto.response.FriendshipStatusResponseDto;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.mapper.UserMapper;
import com.spring.social_network.model.Friendship;
import com.spring.social_network.model.FriendshipStatus;
import com.spring.social_network.model.User;
import com.spring.social_network.repository.FriendshipRepository;
import com.spring.social_network.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public FriendshipService(FriendshipRepository friendshipRepository,
            UserRepository userRepository,
            UserMapper userMapper) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public FriendshipResponseDto sendFriendRequest(SendFriendRequestDto request) {
        User currentUser = getCurrentUser();
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người nhận"));

        if (currentUser.getId().equals(receiver.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể gửi lời mời kết bạn cho chính mình");
        }

        if (friendshipRepository.existsActiveFriendship(currentUser, receiver)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Lời mời kết bạn đã tồn tại hoặc bạn đã là bạn bè");
        }

        Friendship friendship = Friendship.builder()
                .sender(currentUser)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        Friendship savedFriendship = friendshipRepository.save(friendship);

        return mapToFriendshipResponseDto(savedFriendship);
    }

    public Page<FriendshipResponseDto> getSentRequests(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Friendship> friendships = friendshipRepository.findBySenderAndStatusOrderByRequestedAtDesc(
                currentUser, FriendshipStatus.PENDING, pageable);

        return friendships.map(this::mapToFriendshipResponseDto);
    }

    public Page<FriendshipResponseDto> getReceivedRequests(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Friendship> friendships = friendshipRepository.findByReceiverAndStatusOrderByRequestedAtDesc(
                currentUser, FriendshipStatus.PENDING, pageable);

        return friendships.map(this::mapToFriendshipResponseDto);
    }

    public void cancelFriendRequest(String friendshipId) {
        User currentUser = getCurrentUser();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mối quan hệ bạn bè"));

        if (!friendship.getSender().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "Bạn chỉ có thể hủy lời mời kết bạn của mình");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể hủy lời mời đang chờ");
        }

        friendshipRepository.delete(friendship);
    }

    public void acceptFriendRequest(String friendshipId) {
        User currentUser = getCurrentUser();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mối quan hệ bạn bè"));

        if (!friendship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED,
                    "Bạn chỉ có thể chấp nhận lời mời kết bạn gửi đến mình");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể chấp nhận lời mời đang chờ");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    public void declineFriendRequest(String friendshipId) {
        User currentUser = getCurrentUser();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mối quan hệ bạn bè"));

        if (!friendship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "Bạn chỉ có thể từ chối lời mời kết bạn gửi đến mình");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể từ chối lời mời đang chờ");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
    }

    public Page<FriendResponseDto> getFriends(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Friendship> friendships = friendshipRepository.findFriendshipsByUser(currentUser, pageable);

        return friendships.map(this::mapToFriendResponseDto);
    }

    public void removeFriend(String friendshipId) {
        User currentUser = getCurrentUser();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mối quan hệ bạn bè"));

        if (!friendship.getSender().getId().equals(currentUser.getId()) &&
                !friendship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "Bạn chỉ có thể hủy mối quan hệ bạn bè của mình");
        }

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể hủy mối quan hệ bạn bè đã được chấp nhận");
        }

        friendshipRepository.delete(friendship);
    }

    public FriendshipStatusResponseDto getFriendshipStatus(String userId) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

        List<Friendship> friendships = friendshipRepository.findByUsers(currentUser, targetUser);

        if (friendships.isEmpty()) {
            return FriendshipStatusResponseDto.builder()
                    .status("not_friends")
                    .friendshipId(null)
                    .build();
        }

        if (friendships.size() > 1) {
            System.out.println("WARNING: Found " + friendships.size() + " friendships between users " +
                    currentUser.getId() + " and " + targetUser.getId());

            cleanupDuplicateFriendships(friendships);

            friendships = friendshipRepository.findByUsers(currentUser, targetUser);
        }

        Friendship friendship = friendships.get(0);

        switch (friendship.getStatus()) {
            case ACCEPTED:
                return FriendshipStatusResponseDto.builder()
                        .status("friends")
                        .friendshipId(friendship.getId())
                        .build();
            case PENDING:
                if (friendship.getSender().getId().equals(currentUser.getId())) {
                    return FriendshipStatusResponseDto.builder()
                            .status("request_sent")
                            .friendshipId(friendship.getId())
                            .build();
                } else {
                    return FriendshipStatusResponseDto.builder()
                            .status("request_received")
                            .friendshipId(friendship.getId())
                            .build();
                }
            case REJECTED:
            default:
                return FriendshipStatusResponseDto.builder()
                        .status("not_friends")
                        .friendshipId(friendship.getId())
                        .build();
        }
    }

    private FriendshipResponseDto mapToFriendshipResponseDto(Friendship friendship) {
        return FriendshipResponseDto.builder()
                .id(friendship.getId())
                .sender(userMapper.toResponseDto(friendship.getSender()))
                .receiver(userMapper.toResponseDto(friendship.getReceiver()))
                .status(friendship.getStatus())
                .requestedAt(friendship.getRequestedAt())
                .acceptedAt(friendship.getAcceptedAt())
                .build();
    }

    private FriendResponseDto mapToFriendResponseDto(Friendship friendship) {
        User currentUser = getCurrentUser();
        User friend = friendship.getSender().getId().equals(currentUser.getId())
                ? friendship.getReceiver()
                : friendship.getSender();

        return FriendResponseDto.builder()
                .id(friendship.getId())
                .friend(userMapper.toResponseDto(friend))
                .acceptedAt(friendship.getAcceptedAt())
                .build();
    }

    private void cleanupDuplicateFriendships(List<Friendship> friendships) {
        if (friendships.size() <= 1) {
            return;
        }

        friendships.sort((f1, f2) -> f2.getRequestedAt().compareTo(f1.getRequestedAt()));

        for (int i = 1; i < friendships.size(); i++) {
            Friendship duplicate = friendships.get(i);
            System.out.println("Removing duplicate friendship: " + duplicate.getId() +
                    " created at: " + duplicate.getRequestedAt());
            friendshipRepository.delete(duplicate);
        }
    }
}
