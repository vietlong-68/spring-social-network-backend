package com.spring.social_network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString(of = { "id", "sender", "receiver", "status" })
@EqualsAndHashCode(of = { "id" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendships", indexes = {
        @Index(name = "idx_friendships_sender", columnList = "user_id"),
        @Index(name = "idx_friendships_receiver", columnList = "friend_id"),
        @Index(name = "idx_friendships_status", columnList = "status")
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    @JsonIgnore
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime acceptedAt;
}