package com.github.abeatrizsc.financyx.exceptions;

public class SecurityException extends RuntimeException {
    public SecurityException() {
        super("An error occurred during the action. Please log in again.");
    }
}
