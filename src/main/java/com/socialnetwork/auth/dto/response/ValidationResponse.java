package com.socialnetwork.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Результат валидации JWT токена")
public class ValidationResponse {
    @Schema(
            description = "Признак валидности токена",
            example = "true"
    )
    private boolean valid;

    @Schema(
            description = "ID пользователя, если токен валиден",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID userId;

    @Schema(
            description = "Email пользователя, если токен валиден",
            example = "user@example.com"
    )
    private String email;
}
