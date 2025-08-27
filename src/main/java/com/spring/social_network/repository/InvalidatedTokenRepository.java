package com.spring.social_network.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.spring.social_network.model.InvalidatedToken;
import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiresAt < :currentTime")
    int deleteExpiredTokens(Date currentTime);
}
