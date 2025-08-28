package com.spring.social_network.repository;

import com.spring.social_network.model.Post;
import com.spring.social_network.model.PostReaction;
import com.spring.social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, String> {

    
    Page<PostReaction> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);

    
    Page<PostReaction> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    
    Optional<PostReaction> findByPostAndOwner(Post post, User owner);

    
    @Query("SELECT pr FROM PostReaction pr WHERE pr.createdAt BETWEEN :startDate AND :endDate ORDER BY pr.createdAt DESC")
    Page<PostReaction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate, 
                                              Pageable pageable);

    
    long countByPost(Post post);

    
    long countByOwner(User owner);

    
    boolean existsByPostAndOwner(Post post, User owner);

    
    boolean existsByPost(Post post);

    
    boolean existsByOwner(User owner);

    
    List<PostReaction> findTop10ByOrderByCreatedAtDesc();

    
    List<PostReaction> findTop5ByPostOrderByCreatedAtDesc(Post post);

    
    void deleteByPostAndOwner(Post post, User owner);
}
