package com.spring.social_network.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.spring.social_network.model.User;
import com.spring.social_network.model.RoleType;
import com.spring.social_network.model.Gender;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRolesName(@Param("roleName") RoleType roleName, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findByFirstNameOrLastNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<User> findByIsBlockedTrue();

    long countByIsBlockedFalse();

    long countByIsBlockedTrue();

    long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);

    long countByGender(@Param("gender") Gender gender);

    long countByProfilePictureUrlIsNotNull();

    long countByPhoneIsNotNull();

    long countByAddressIsNotNull();
}
