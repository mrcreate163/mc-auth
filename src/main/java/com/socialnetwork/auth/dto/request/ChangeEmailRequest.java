package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeEmailRequest {

    @Email(message = "Некорректный формат нового email")
    @NotBlank(message = "Новый email не может быть пустым")
    private String newEmail;
}
