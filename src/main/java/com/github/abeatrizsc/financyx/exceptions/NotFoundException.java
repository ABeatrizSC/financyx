package com.github.abeatrizsc.financyx.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String item) {
        super(item + " not found.");
    }
}