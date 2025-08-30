package com.spring.social_network.repository.post;

import com.spring.social_network.model.post.Post;
import com.spring.social_network.model.post.PostPrivacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

        @Query("SELECT p FROM Post p WHERE p.privacy = :privacy ORDER BY p.createdAt DESC")
        Page<Post> findByPrivacyOrderByCreatedAtDesc(@Param("privacy") PostPrivacy privacy, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.privacy = :privacy ORDER BY p.createdAt DESC")
        Page<Post> findByUserIdAndPrivacyOrderByCreatedAtDesc(@Param("userId") String userId,
                        @Param("privacy") PostPrivacy privacy, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
        Page<Post> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.user.id IN :friendIds AND p.privacy = :privacy ORDER BY p.createdAt DESC")
        Page<Post> findByUserIdInAndPrivacyOrderByCreatedAtDesc(@Param("friendIds") List<String> friendIds,
                        @Param("privacy") PostPrivacy privacy,
                        Pageable pageable);

        @Query("SELECT p FROM Post p WHERE (p.privacy = :publicPrivacy) OR (p.user.id IN :friendIds AND p.privacy = :friendsPrivacy) OR (p.user.id = :currentUserId) ORDER BY p.createdAt DESC")
        Page<Post> findFeedPosts(@Param("publicPrivacy") PostPrivacy publicPrivacy,
                        @Param("friendsPrivacy") PostPrivacy friendsPrivacy,
                        @Param("friendIds") List<String> friendIds,
                        @Param("currentUserId") String currentUserId,
                        Pageable pageable);

        @Query("UPDATE Post p SET p.commentCount = :commentCount WHERE p.id = :postId")
        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        void updateCommentCount(@Param("postId") String postId, @Param("commentCount") int commentCount);

        long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);

        long countByPrivacy(@Param("privacy") PostPrivacy privacy);

        long countByImageUrlIsNotEmpty();

        long countByVideoUrlIsNotEmpty();

        long countByHashtagsIsNotEmpty();
}
