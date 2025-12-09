package com.socialnetwork.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные для обновления access токена")
public class RefreshTokenRequest {

    @Schema(
            description = "Refresh токен для обновления access токена",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAifQ...",
            required = true
    )
    @NotBlank(message = "Refresh токен не может быть пустым")
    private String refreshToken;
}
