package com.socialnetwork.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "JWT токены для аутентификации")
public class TokenResponse {
    @Schema(
            description = "JWT access токен для доступа к защищённым ресурсам (действителен 15 минут)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJleHAiOjE3MDA0MjI4MDB9.signature"
    )
    private String accessToken;

    @Schema(
            description = "JWT refresh токен для обновления access токена (действителен 30 дней)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJleHAiOjE3MDI5OTY4MDB9.signature"
    )
    private String refreshToken;

}
