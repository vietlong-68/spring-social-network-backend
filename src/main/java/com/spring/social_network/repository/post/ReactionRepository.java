package com.spring.social_network.repository.post;

import com.spring.social_network.model.post.Reaction;
import com.spring.social_network.model.post.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {

        @Query("SELECT r FROM Reaction r WHERE r.post.id = :postId AND r.user.id = :userId AND r.type = :type")
        Optional<Reaction> findByPostIdAndUserIdAndType(@Param("postId") String postId,
                        @Param("userId") String userId,
                        @Param("type") ReactionType type);

        @Query("SELECT r FROM Reaction r WHERE r.post.id = :postId AND r.type = :type")
        Page<Reaction> findByPostIdAndType(@Param("postId") String postId,
                        @Param("type") ReactionType type,
                        Pageable pageable);

        @Query("SELECT COUNT(r) FROM Reaction r WHERE r.post.id = :postId AND r.type = :type")
        long countByPostIdAndType(@Param("postId") String postId, @Param("type") ReactionType type);

        @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reaction r WHERE r.post.id = :postId AND r.user.id = :userId AND r.type = :type")
        boolean existsByPostIdAndUserIdAndType(@Param("postId") String postId,
                        @Param("userId") String userId,
                        @Param("type") ReactionType type);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @Query("DELETE FROM Reaction r WHERE r.post.id = :postId")
        int deleteByPostId(@Param("postId") String postId);
}
