package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDto {
    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String password1;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String password2;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательно")
    private String lastName;

    @NotBlank(message = "Код капчи обязательно")
    private String captchaCode;
}
