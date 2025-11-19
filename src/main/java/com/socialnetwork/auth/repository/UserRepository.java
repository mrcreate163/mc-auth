package com.socialnetwork.auth.repository;

import com.socialnetwork.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.email = :email and u.isDeleted = false")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
}
