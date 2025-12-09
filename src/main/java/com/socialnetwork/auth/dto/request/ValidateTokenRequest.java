package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTokenRequest {

    @NotBlank(message = "Токен обязателен")
    private String token;
}
