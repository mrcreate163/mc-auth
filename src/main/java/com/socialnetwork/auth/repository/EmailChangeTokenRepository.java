package com.socialnetwork.auth.repository;

import com.socialnetwork.auth.entity.EmailChangeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailChangeTokenRepository extends JpaRepository<EmailChangeToken, UUID> {
    Optional<EmailChangeToken> findByToken(String token);
}
