package com.spring.social_network.model.post;

import com.spring.social_network.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString(of = { "id", "type", "user", "post", "createdAt" })
@EqualsAndHashCode(of = { "id" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reactions", indexes = {
        @Index(name = "idx_reactions_user_id", columnList = "user_id"),
        @Index(name = "idx_reactions_post_id", columnList = "post_id"),
        @Index(name = "idx_reactions_type", columnList = "type"),
        @Index(name = "idx_reactions_created_at", columnList = "created_at")
})
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private ReactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
