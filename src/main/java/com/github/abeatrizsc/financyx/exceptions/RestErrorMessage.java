package com.github.abeatrizsc.financyx.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class RestErrorMessage {
    private Integer status;
    private HttpStatus error;
    private String message;
}
