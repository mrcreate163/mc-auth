package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Токен не может быть пустым")
    private String token;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String newPassword;

}
