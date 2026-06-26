package com.github.abeatrizsc.financyx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "First Name is required.")
    @Size(min = 3, max = 25, message = "The name must have 3 to 25 characters.")
    private String firstName;

    @NotBlank(message = "Last Name is required.")
    @Size(min = 3, max = 30, message = "The name must have 3 to 25 characters.")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email.")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 7, max = 8, message = "The password must have 7 to 8 characters.")
    private String password;
}
