package com.codesoom.assignment.errors;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String token) {
        super("Token is Invalid : " + token);
    }
}
