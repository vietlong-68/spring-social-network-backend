package com.spring.social_network.repository;

import com.spring.social_network.model.Post;
import com.spring.social_network.model.PostComment;
import com.spring.social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, String> {

    
    Page<PostComment> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);

    
    Page<PostComment> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    
    Page<PostComment> findByPostAndOwnerOrderByCreatedAtDesc(Post post, User owner, Pageable pageable);

    
    @Query("SELECT pc FROM PostComment pc WHERE pc.content LIKE %:keyword% ORDER BY pc.createdAt DESC")
    Page<PostComment> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    
    @Query("SELECT pc FROM PostComment pc WHERE pc.createdAt BETWEEN :startDate AND :endDate ORDER BY pc.createdAt DESC")
    Page<PostComment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate, 
                                             Pageable pageable);

    
    long countByPost(Post post);

    
    long countByOwner(User owner);

    
    boolean existsByPost(Post post);

    
    boolean existsByOwner(User owner);

    
    List<PostComment> findTop10ByOrderByCreatedAtDesc();

    
    List<PostComment> findTop5ByPostOrderByCreatedAtDesc(Post post);
}
