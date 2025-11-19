package com.socialnetwork.auth.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisteredEvent {
    private UUID userId;
    private String email;
    private String firstname;
    private String lastname;
    private LocalDateTime registeredAt;
}
