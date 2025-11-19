package com.socialnetwork.auth.repository;

import com.socialnetwork.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

   Optional<PasswordResetToken> findByToken(String token);

   @Query("delete from PasswordResetToken prt where prt.expiresAt < CURRENT TIMESTAMP  or prt.isUsed = true")
   void deleteExpiredAndUsedTokens();
}
