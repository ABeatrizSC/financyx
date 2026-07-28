package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.Category;
import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.dto.CategoryRequestDto;
import com.github.abeatrizsc.financyx.enums.CategoryTypeEnum;
import com.github.abeatrizsc.financyx.exceptions.NotFoundException;
import com.github.abeatrizsc.financyx.repositories.CategoryRepository;
import com.github.abeatrizsc.financyx.utils.AuthRequestUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AuthRequestUtils authRequestUtils;
    private final UserService userService;

    @Transactional
    public Category create(CategoryRequestDto category) {
        String userId = authRequestUtils.getAuthenticatedUserId();

        User user = userService.findUserById(userId);
        // TO DO: Use MAPSTRUCT
        //TO DO: não permitir que o usuário tenha categorias de mesmo nome
        Category newCategory = Category
                .builder()
                .user(user)
                .name(category.getName())
                .description(category.getDescription())
                .type(getCategoryValueOrThrow(category.getType()))
                .color(category.getColor())
                .monthlyLimit(category.getMonthlyLimit())
                .build();

        return categoryRepository.save(newCategory);
    }

    @Transactional
    public Category update(String categoryId, CategoryRequestDto cUpdated) {
        String userId = authRequestUtils.getAuthenticatedUserId();

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Category"));

        category.setName(cUpdated.getName());
        category.setDescription(cUpdated.getDescription());
        category.setType(getCategoryValueOrThrow(cUpdated.getType()));
        category.setColor(cUpdated.getColor());
        category.setMonthlyLimit(cUpdated.getMonthlyLimit());

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(String categoryId) {
        String userId = authRequestUtils.getAuthenticatedUserId();

        Category category = this.findByIdAndUserId(categoryId);

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        String userId = authRequestUtils.getAuthenticatedUserId();

        return categoryRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Category findByIdAndUserId(String categoryId) {
        String userId = authRequestUtils.getAuthenticatedUserId();

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Category"));
    }

    private CategoryTypeEnum getCategoryValueOrThrow(String category) {
        try {
             return CategoryTypeEnum.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid category type: " + category +
                            ". Allowed values: " + Arrays.toString(CategoryTypeEnum.values())
            );
        }
    }
}