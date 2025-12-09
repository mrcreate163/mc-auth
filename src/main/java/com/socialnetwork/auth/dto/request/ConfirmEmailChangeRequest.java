package com.socialnetwork.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmEmailChangeRequest {

    @NotBlank(message = "Token cannot be empty")
    private String token;
}
