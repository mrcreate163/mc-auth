package com.socialnetwork.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Данные для аутентификации пользователя")
public class AuthenticateRq {

    @Schema(
            description = "Email адрес пользователя",
            example = "user@example.com",
            required = true
    )
    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Schema(
            description = "Пароль пользователя",
            example = "mySecurePassword123",
            required = true
    )
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
