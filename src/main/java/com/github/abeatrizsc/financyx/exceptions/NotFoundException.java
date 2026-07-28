package com.github.abeatrizsc.financyx.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String item) {
        super(item + " not found.");
    }
}