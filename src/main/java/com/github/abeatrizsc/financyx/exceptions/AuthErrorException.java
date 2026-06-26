package com.github.abeatrizsc.financyx.exceptions;

public class AuthErrorException extends RuntimeException {
    public AuthErrorException() {
        super("Invalid credentials.");
    }
}
