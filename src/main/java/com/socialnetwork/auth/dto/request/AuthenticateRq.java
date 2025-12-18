package com.socialnetwork.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Данные для аутентификации пользователя")
public class AuthenticateRq {

    @Schema(
            description = "Email адрес пользователя",
            example = "user@example.com"
    )
    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Schema(
            description = "Пароль пользователя",
            example = "mySecurePassword123"
    )
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
