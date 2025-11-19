package com.socialnetwork.auth.repository;

import com.socialnetwork.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Query("select rt from RefreshToken rt where rt.user.id = :userId and rt.isRevoked = false")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("update RefreshToken rt set rt.isRevoked = true where rt.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from RefreshToken rt where rt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
