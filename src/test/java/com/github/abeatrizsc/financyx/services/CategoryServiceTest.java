package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.Category;
import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.dto.CategoryRequestDto;
import com.github.abeatrizsc.financyx.enums.CategoryTypeEnum;
import com.github.abeatrizsc.financyx.exceptions.NotFoundException;
import com.github.abeatrizsc.financyx.repositories.CategoryRepository;
import com.github.abeatrizsc.financyx.utils.AuthRequestUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthRequestUtils authRequestUtils;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("create: must create the category for authenticated user")
    void shouldCreateCategoryForAuthenticatedUser() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);

        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName("Alimentação");
        dto.setDescription("Gastos com mercado");
        dto.setType(CategoryTypeEnum.values()[0].name()); // evita hardcode frágil
        dto.setColor("#FF0000");
        dto.setMonthlyLimit(BigDecimal.valueOf(1200.00));

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.findUserById(userId)).thenReturn(user);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId("cat-1");
            return c;
        });

        Category result = categoryService.create(dto);

        assertNotNull(result);
        assertEquals("cat-1", result.getId());
        assertEquals("Alimentação", result.getName());
        assertEquals("Gastos com mercado", result.getDescription());
        assertEquals("#FF0000", result.getColor());
        assertEquals(BigDecimal.valueOf(1200.00), result.getMonthlyLimit());
        assertEquals(user, result.getUser());
        assertEquals(CategoryTypeEnum.values()[0], result.getType());

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());
        Category saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals("Alimentação", saved.getName());
        assertEquals(CategoryTypeEnum.values()[0], saved.getType());

        verify(userService, times(1)).findUserById(userId);
    }

    @Test
    @DisplayName("create: must throw exception when type is invalid")
    void shouldThrowWhenTypeIsInvalidOnCreate() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);

        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName("Alimentação");
        dto.setDescription("Gastos com mercado");
        dto.setType("TIPO_INEXISTENTE");
        dto.setColor("#FF0000");
        dto.setMonthlyLimit(BigDecimal.valueOf(1200.00));

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.findUserById(userId)).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: must throw exception when type is null")
    void shouldThrowWhenTypeIsNullOnCreate() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);

        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName("Alimentação");
        dto.setDescription("Gastos com mercado");
        dto.setType(null); // aqui estava passando batido no seu cenário
        dto.setColor("#FF0000");
        dto.setMonthlyLimit(BigDecimal.valueOf(1200.00));

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.findUserById(userId)).thenReturn(user);

        assertThrows(NullPointerException.class, () -> categoryService.create(dto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: must update the category when found by the authenticated user")
    void shouldUpdateCategoryWhenFoundOnUpdate() {
        String userId = "user-123";
        String categoryId = "cat-1";

        Category existing = new Category();
        existing.setId(categoryId);
        existing.setName("Antigo");
        existing.setDescription("Descrição antiga");
        existing.setType(CategoryTypeEnum.values()[0]);
        existing.setColor("#000000");
        existing.setMonthlyLimit(BigDecimal.valueOf(100.00));

        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName("Novo nome");
        dto.setDescription("Nova descrição");
        dto.setType(CategoryTypeEnum.values()[0].name());
        dto.setColor("#FFFFFF");
        dto.setMonthlyLimit(BigDecimal.valueOf(500.00));

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category result = categoryService.update(categoryId, dto);

        assertNotNull(result);
        assertEquals("Novo nome", result.getName());
        assertEquals("Nova descrição", result.getDescription());
        assertEquals("#FFFFFF", result.getColor());
        assertEquals(BigDecimal.valueOf(500.00), result.getMonthlyLimit());
        assertEquals(CategoryTypeEnum.values()[0], result.getType());

        verify(categoryRepository).findByIdAndUserId(categoryId, userId);
        verify(categoryRepository).save(existing);
    }

    @Test
    @DisplayName("update: must throw NotFoundException when category not found by the authenticated id")
    void shouldThrowWhenCategoryNotFoundOnUpdate() {
        String userId = "user-123";
        String categoryId = "cat-x";

        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setType(CategoryTypeEnum.values()[0].name());

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.update(categoryId, dto));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: must delete the category when found by authenticated user")
    void shouldDeleteCategoryWhenFoundOnDelete() {
        String userId = "user-123";
        String categoryId = "cat-1";

        Category category = new Category();
        category.setId(categoryId);

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

        categoryService.delete(categoryId);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    @DisplayName("delete: must throw NotFoundException when category not found by authenticated user")
    void shouldThrowNotFoundExceptionWhenCategoryMissingOnDelete() {
        String userId = "user-123";
        String categoryId = "cat-404";

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.delete(categoryId));

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("findAll: must return all categories by authenticated user")
    void shouldReturnAuthenticatedUserCategories() {
        String userId = "user-123";

        Category c1 = new Category();
        c1.setId("cat-1");
        Category c2 = new Category();
        c2.setId("cat-2");

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findAllByUserId(userId)).thenReturn(List.of(c1, c2));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("findByIdAndUserId: must return the category when exists by id and user id")
    void shouldReturnCategoryByIdWhenFound() {
        String userId = "user-123";
        String categoryId = "cat-1";

        Category category = new Category();
        category.setId(categoryId);

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

        Category result = categoryService.findByIdAndUserId(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
    }

    @Test
    @DisplayName("findByIdAndUserId: must throw NotFoundException when category not found by id and user id")
    void shouldThrowNotFoundWhenCategoryIdIsMissing() {
        String userId = "user-123";
        String categoryId = "cat-404";

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.findByIdAndUserId(categoryId));
    }

    @Test
    @DisplayName("must throw IllegalArgumentException when category type is invalid")
    void shouldThrowIllegalArgumentExceptionWhenTypeIsInvalid() {
        String userId = "user-123";
        User user = new User();
        user.setId(userId);

        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName("Alimentação");
        dto.setDescription("Mercado");
        dto.setType("TIPO_QUE_NAO_EXISTE");
        dto.setColor("#FF0000");
        dto.setMonthlyLimit(BigDecimal.valueOf(1000));

        when(authRequestUtils.getAuthenticatedUserId()).thenReturn(userId);
        when(userService.findUserById(userId)).thenReturn(user);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.create(dto)
        );

        assertTrue(ex.getMessage().contains("Invalid category type"));
        verify(categoryRepository, never()).save(any(Category.class));
    }
}