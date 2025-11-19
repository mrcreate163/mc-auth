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
public class AccountChangedEvent {

    private UUID userId;
    private String newEmail;
    private LocalDateTime changedAt;
}
