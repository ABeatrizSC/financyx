package com.github.abeatrizsc.financyx.controllers;

import com.github.abeatrizsc.financyx.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.abeatrizsc.financyx.domain.Category;
import com.github.abeatrizsc.financyx.dto.CategoryRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public Category update(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryRequestDto body
    ) {
        return categoryService.update(categoryId, body);
    }

    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public Category get(@PathVariable String categoryId) {
        return categoryService.findByIdAndUserId(categoryId);
    }

    @GetMapping
    public List<Category> findAll() {
        return categoryService.findAll();
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String categoryId) {
        categoryService.delete(categoryId);
    }
}