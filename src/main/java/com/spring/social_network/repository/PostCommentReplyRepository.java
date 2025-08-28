package com.spring.social_network.repository;

import com.spring.social_network.model.PostComment;
import com.spring.social_network.model.PostCommentReply;
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
public interface PostCommentReplyRepository extends JpaRepository<PostCommentReply, String> {

    
    Page<PostCommentReply> findByPostCommentOrderByCreatedAtDesc(PostComment postComment, Pageable pageable);

    
    Page<PostCommentReply> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    
    Page<PostCommentReply> findByPostCommentAndOwnerOrderByCreatedAtDesc(PostComment postComment, User owner, Pageable pageable);

    
    @Query("SELECT pcr FROM PostCommentReply pcr WHERE pcr.content LIKE %:keyword% ORDER BY pcr.createdAt DESC")
    Page<PostCommentReply> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    
    @Query("SELECT pcr FROM PostCommentReply pcr WHERE pcr.createdAt BETWEEN :startDate AND :endDate ORDER BY pcr.createdAt DESC")
    Page<PostCommentReply> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate, 
                                                  Pageable pageable);

    
    long countByPostComment(PostComment postComment);

    
    long countByOwner(User owner);

    
    boolean existsByPostComment(PostComment postComment);

    
    boolean existsByOwner(User owner);

    
    List<PostCommentReply> findTop10ByOrderByCreatedAtDesc();

    
    List<PostCommentReply> findTop5ByPostCommentOrderByCreatedAtDesc(PostComment postComment);
}
