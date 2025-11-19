package com.socialnetwork.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private String error;
    private String errorDescription;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String error, String errorDescription) {
        return ErrorResponse.builder()
                .error(error)
                .errorDescription(errorDescription)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
