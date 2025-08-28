package com.spring.social_network.repository;

import com.spring.social_network.model.Friendship;
import com.spring.social_network.model.FriendshipStatus;
import com.spring.social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)")
    Optional<Friendship> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    
    @Query("SELECT f FROM Friendship f WHERE ((f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)) AND f.status = :status")
    Optional<Friendship> findByUsersAndStatus(@Param("user1") User user1, @Param("user2") User user2, @Param("status") FriendshipStatus status);

    
    Page<Friendship> findBySenderAndStatusOrderByRequestedAtDesc(User sender, FriendshipStatus status, Pageable pageable);

    
    Page<Friendship> findByReceiverAndStatusOrderByRequestedAtDesc(User receiver, FriendshipStatus status, Pageable pageable);

    
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user OR f.receiver = :user) AND f.status = 'ACCEPTED' ORDER BY f.acceptedAt DESC")
    Page<Friendship> findFriendshipsByUser(@Param("user") User user, Pageable pageable);

    
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE ((f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)) AND f.status IN ('PENDING', 'ACCEPTED')")
    boolean existsActiveFriendship(@Param("user1") User user1, @Param("user2") User user2);
}
