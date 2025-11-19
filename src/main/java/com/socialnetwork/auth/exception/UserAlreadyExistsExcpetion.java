package com.socialnetwork.auth.exception;

public class UserAlreadyExistsExcpetion extends RuntimeException {
    public UserAlreadyExistsExcpetion(String message) {
        super(message);
    }
}
