package com.socialnetwork.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные для запроса восстановления пароля")
public class RecoveryPasswordLinkRq {

    @Schema(
            description = "Email адрес для отправки ссылки восстановления пароля",
            example = "user@example.com",
            required = true
    )
    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
}
