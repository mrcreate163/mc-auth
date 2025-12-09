package com.socialnetwork.auth.dto.kafka;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
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
