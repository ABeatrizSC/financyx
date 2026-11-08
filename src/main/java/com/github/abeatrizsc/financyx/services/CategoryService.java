package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.Category;
import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.dto.CategoryRequestDto;
import com.github.abeatrizsc.financyx.enums.CategoryTypeEnum;
import com.github.abeatrizsc.financyx.exceptions.NotFoundException;
import com.github.abeatrizsc.financyx.repositories.CategoryRepository;
import com.github.abeatrizsc.financyx.utils.AuthRequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.text.Normalizer;

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
        ensureUniqueName(userId, category.getName(), null);
        // TO DO: Use MAPSTRUCT
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

        ensureUniqueName(userId, cUpdated.getName(), categoryId);

        category.setName(cUpdated.getName());
        category.setDescription(cUpdated.getDescription());
        category.setType(getCategoryValueOrThrow(cUpdated.getType()));
        category.setColor(cUpdated.getColor());
        category.setMonthlyLimit(cUpdated.getMonthlyLimit());

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(String categoryId) {
        Category category = this.findUserCategoryById(categoryId);

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        String userId = authRequestUtils.getAuthenticatedUserId();

        return categoryRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Category findUserCategoryById(String categoryId) {
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

    private void ensureUniqueName(String userId, String name, String excludedId) {
        String key = normalizeName(name);
        boolean exists = categoryRepository.findAllByUserId(userId).stream()
                .anyMatch(c -> (excludedId == null || !Objects.equals(c.getId(), excludedId))
                        && normalizeName(c.getName()).equals(key));
        if (exists) {
            throw new IllegalArgumentException("Category name already in use.");
        }
    }

    static String normalizeName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("(?U)\\s+", " ")
                .strip().toLowerCase(Locale.ROOT);
    }

    public void createInitialCategories(User user) {
        List<Category> existing = categoryRepository.findAllByUserId(user.getId());
        for (String name : List.of("Alimentação", "Transporte", "Moradia", "Lazer", "Saúde", "Educação", "Salário")) {
            if (existing.stream().noneMatch(c -> normalizeName(c.getName()).equals(normalizeName(name)))) {
                Category category = Category.builder().user(user).name(name)
                        .type(name.equals("Salário") ? CategoryTypeEnum.INCOME : CategoryTypeEnum.EXPENSE)
                        .color("#808080").build();
                categoryRepository.save(category);
                existing = new java.util.ArrayList<>(existing);
                existing.add(category);
            }
        }
    }
}
