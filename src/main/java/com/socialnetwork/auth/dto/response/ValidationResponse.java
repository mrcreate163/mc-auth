package com.socialnetwork.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationResponse {
    private boolean valid;
    private UUID userId;
    private String email;
}
