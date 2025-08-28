package com.spring.social_network.service;

import com.spring.social_network.dto.request.SendFriendRequestDto;
import com.spring.social_network.dto.response.FriendResponseDto;
import com.spring.social_network.dto.response.FriendshipResponseDto;
import com.spring.social_network.dto.response.FriendshipStatusResponseDto;
import com.spring.social_network.dto.response.UserResponseDto;
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
import java.util.Optional;

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
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Receiver not found"));
        
        
        if (currentUser.getId().equals(receiver.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Cannot send friend request to yourself");
        }
        
        
        if (friendshipRepository.existsActiveFriendship(currentUser, receiver)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Friend request already exists or you are already friends");
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
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Friendship not found"));
        
        
        if (!friendship.getSender().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "You can only cancel your own friend requests");
        }
        
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Can only cancel pending requests");
        }
        
        friendshipRepository.delete(friendship);
    }
    
    
    public void acceptFriendRequest(String friendshipId) {
        User currentUser = getCurrentUser();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Friendship not found"));
        
        
        if (!friendship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "You can only accept friend requests sent to you");
        }
        
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Can only accept pending requests");
        }
        
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }
    
    
    public void declineFriendRequest(String friendshipId) {
        User currentUser = getCurrentUser();
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Friendship not found"));
        
        
        if (!friendship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "You can only decline friend requests sent to you");
        }
        
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Can only decline pending requests");
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
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Friendship not found"));
        
        
        if (!friendship.getSender().getId().equals(currentUser.getId()) && 
            !friendship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.USER_UNAUTHORIZED, "You can only remove your own friendships");
        }
        
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Can only remove accepted friendships");
        }
        
        friendshipRepository.delete(friendship);
    }
    
    
    public FriendshipStatusResponseDto getFriendshipStatus(String userId) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        Optional<Friendship> friendshipOpt = friendshipRepository.findByUsers(currentUser, targetUser);
        
        if (friendshipOpt.isEmpty()) {
            return FriendshipStatusResponseDto.builder().status("not_friends").build();
        }
        
        Friendship friendship = friendshipOpt.get();
        
        switch (friendship.getStatus()) {
            case ACCEPTED:
                return FriendshipStatusResponseDto.builder().status("friends").build();
            case PENDING:
                if (friendship.getSender().getId().equals(currentUser.getId())) {
                    return FriendshipStatusResponseDto.builder().status("request_sent").build();
                } else {
                    return FriendshipStatusResponseDto.builder().status("request_received").build();
                }
            case REJECTED:
            default:
                return FriendshipStatusResponseDto.builder().status("not_friends").build();
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
}
