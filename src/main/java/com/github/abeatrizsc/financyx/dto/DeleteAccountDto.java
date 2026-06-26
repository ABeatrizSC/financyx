package com.github.abeatrizsc.financyx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteAccountDto {
    @NotBlank(message = "Password is required.")
    private String password;
}
