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
@Schema(description = "Данные для запроса изменения email")
public class ChangeEmailRequest {

    @Schema(
            description = "Новый email адрес пользователя",
            example = "newemail@example.com",
            required = true
    )
    @Email(message = "Некорректный формат нового email")
    @NotBlank(message = "Новый email не может быть пустым")
    private String newEmail;
}
