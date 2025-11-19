package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTokenRequest {

    @NotBlank(message = "Токен обязателен")
    private String token;
}
