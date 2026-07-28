package com.github.abeatrizsc.financyx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDto {
    @NotBlank(message = "Name is required.")
    @Size(min = 3, max = 25, message = "The name must have 3 to 25 characters.")
    private String name;

    @Size(min = 0, max = 30, message = "The description must have until 30 characters.")
    private String description;

    @NotBlank(message = "Category type is required.")
    private String type;

    @NotBlank(message = "Color is required.")
    private String color;

    @PositiveOrZero(message = "Invalid monthly limit value.")
    private BigDecimal monthlyLimit;
}
