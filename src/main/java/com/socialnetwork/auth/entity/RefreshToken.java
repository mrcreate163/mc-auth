package com.socialnetwork.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_revorked")
    private Boolean isRevoked = false; //Отозван ли токен

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRevoked == null) {
            isRevoked = false;
        }
    }
}
