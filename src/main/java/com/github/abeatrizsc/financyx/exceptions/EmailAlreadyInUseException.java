package com.github.abeatrizsc.financyx.exceptions;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("Email already registered. Please use a different email.");
    }

}
