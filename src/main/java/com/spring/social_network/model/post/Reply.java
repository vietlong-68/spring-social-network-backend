package com.spring.social_network.model.post;

import com.spring.social_network.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString(of = { "id", "content", "user", "comment", "createdAt" })
@EqualsAndHashCode(of = { "id" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "replies", indexes = {
        @Index(name = "idx_replies_comment_id", columnList = "comment_id"),
        @Index(name = "idx_replies_user_id", columnList = "user_id"),
        @Index(name = "idx_replies_created_at", columnList = "created_at")
})
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Nội dung reply là bắt buộc")
    @Size(max = 1000, message = "Nội dung reply không được vượt quá 1000 ký tự")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
