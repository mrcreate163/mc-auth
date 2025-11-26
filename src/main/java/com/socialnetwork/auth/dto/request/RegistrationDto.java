package com.socialnetwork.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Данные для регистрации нового пользователя")
public class RegistrationDto {
    @Schema(
            description = "Email адрес для регистрации",
            example = "newuser@example.com",
            required = true
    )
    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;
    @Schema(
            description = "Имя нового пользователя",
            example = "Иван",
            required = true
    )
    @NotBlank(message = "Имя обязательно")
    private String firstname;

    @Schema(
            description = "Фамилия нового пользователя",
            example = "Сидоров",
            required = true
    )
    @NotBlank(message = "Фамилия обязательна")
    private String lastname;

    @Schema(
            description = "Пароль (минимум 6 символов)",
            example = "securePassword123",
            required = true,
            minLength = 6
    )
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String password1;

    @Schema(
            description = "Подтверждение пароля (должен совпадать с password1)",
            example = "securePassword123",
            required = true
    )
    @NotBlank(message = "Подтверждение пароля обязательно")
    private String password2;

    @Schema(
            description = "Имя пользователя",
            example = "Иван",
            required = true
    )
    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @Schema(
            description = "Фамилия пользователя",
            example = "Иванов",
            required = true
    )
    @NotBlank(message = "Фамилия обязательно")
    private String lastName;

    @Schema(
            description = "Код капчи, полученный из /captcha эндпоинта",
            example = "ABC123",
            required = true
    )
    @NotBlank(message = "Код капчи обязательно")
    private String captchaCode;
}

