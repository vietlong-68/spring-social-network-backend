package com.spring.social_network.repository;

import com.spring.social_network.model.Post;
import com.spring.social_network.model.PostVisibility;
import com.spring.social_network.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    
    Page<Post> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    
    Page<Post> findByVisibilityOrderByCreatedAtDesc(PostVisibility visibility, Pageable pageable);

    
    Page<Post> findByOwnerAndVisibilityOrderByCreatedAtDesc(User owner, PostVisibility visibility, Pageable pageable);

    
    long countByOwner(User owner);

    
    boolean existsByOwner(User owner);
}
