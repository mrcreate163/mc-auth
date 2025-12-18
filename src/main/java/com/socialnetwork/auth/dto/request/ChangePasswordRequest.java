package com.socialnetwork.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные для смены пароля по токену восстановления")
public class ChangePasswordRequest {

    @Schema(
            description = "Токен восстановления пароля из письма",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    @NotBlank(message = "Токен не может быть пустым")
    private String token;

    @Schema(
            description = "Новый пароль (минимум 6 символов)",
            example = "newSecurePassword456",
            minLength = 6
    )
    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String newPassword;

}
