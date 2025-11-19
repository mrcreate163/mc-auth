package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateRq {

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}
