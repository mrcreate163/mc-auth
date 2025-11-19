package com.socialnetwork.auth.exception;

public class CaptchaValidationException extends RuntimeException {
    public CaptchaValidationException(String message) {
        super(message);
    }
}
