package com.socialnetwork.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Ответ с информацией об ошибке")
public class ErrorResponse {

    @Schema(
            description = "Код ошибки",
            example = "INVALID_CREDENTIALS"
    )
    private String error;

    @Schema(
            description = "Подробное описание ошибки",
            example = "Неверный email или пароль"
    )
    private String errorDescription;

    @Schema(
            description = "Временная метка возникновения ошибки",
            example = "2025-11-21T12:00:00"
    )
    private LocalDateTime timestamp;

    public static ErrorResponse of(String error, String errorDescription) {
        return ErrorResponse.builder()
                .error(error)
                .errorDescription(errorDescription)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
