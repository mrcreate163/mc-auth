package com.socialnetwork.auth.exception;

import com.socialnetwork.auth.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsExcpetion.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsExcpetion ex) {
        ErrorResponse error = ErrorResponse.of("USER_ALREADY_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse error = ErrorResponse.of("INVALID_CREDENTIALS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        ErrorResponse error = ErrorResponse.of("INVALID_TOKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(CaptchaValidationException.class)
    public ResponseEntity<ErrorResponse> handleCaptchaValidation(CaptchaValidationException ex) {
        ErrorResponse error = ErrorResponse.of("CAPTCHA_VALIDATION_FAILED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.joining(", "));
        ErrorResponse error = ErrorResponse.of("VALIDATION_FAILED", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.of("INTERNAL_SERVER_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
