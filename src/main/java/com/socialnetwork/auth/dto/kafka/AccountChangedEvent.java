package com.socialnetwork.auth.dto.kafka;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountChangedEvent {

    private UUID userId;
    private String newEmail;
    private LocalDateTime changedAt;
}
