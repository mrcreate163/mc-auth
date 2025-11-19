package com.socialnetwork.auth.repository;

import com.socialnetwork.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

   Optional<PasswordResetToken> findByToken(String token);

   @Modifying
   @Query("delete from PasswordResetToken prt where prt.expiresAt < CURRENT_TIMESTAMP or prt.isUsed = true")
   void deleteExpiredAndUsedTokens();
}
