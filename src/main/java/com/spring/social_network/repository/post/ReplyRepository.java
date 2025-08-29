package com.spring.social_network.repository.post;

import com.spring.social_network.model.post.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, String> {

    @Query("SELECT r FROM Reply r WHERE r.comment.id = :commentId ORDER BY r.createdAt ASC")
    Page<Reply> findByCommentIdOrderByCreatedAtAsc(@Param("commentId") String commentId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reply r WHERE r.comment.id = :commentId")
    long countByCommentId(@Param("commentId") String commentId);
}
