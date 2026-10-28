package com.roshan.applecommerce.category.service;

import com.roshan.applecommerce.category.dto.CategoryRequest;
import com.roshan.applecommerce.category.dto.CategoryResponse;
import com.roshan.applecommerce.category.entity.Category;
import com.roshan.applecommerce.category.mapper.CategoryMapper;
import com.roshan.applecommerce.category.repository.CategoryRepository;
import com.roshan.applecommerce.common.exception.CategoryNotFoundException;
import com.roshan.applecommerce.common.exception.DuplicateCategoryNameException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic products");

        request = new CategoryRequest(
                "Electronics",
                "Electronic products"
        );

        response = new CategoryResponse(
                1L,
                "Electronics",
                "Electronic products",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void create_shouldCreateCategory_whenNameIsUnique() {
        when(repository.existsByNameIgnoreCase("Electronics"))
                .thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(category);
        when(repository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertNotNull(result);
        assertEquals("Electronics", result.name());

        verify(repository).existsByNameIgnoreCase("Electronics");
        verify(repository).save(category);
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyExists() {
        when(repository.existsByNameIgnoreCase("Electronics"))
                .thenReturn(true);

        assertThrows(
                DuplicateCategoryNameException.class,
                () -> categoryService.create(request)
        );

        verify(repository, never()).save(any(Category.class));
    }

    @Test
    void getById_shouldReturnCategory_whenCategoryExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getById(1L);

        assertNotNull(result);
        assertEquals("Electronics", result.name());
        verify(repository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenCategoryDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getById(99L)
        );
    }

    @Test
    void getAll_shouldReturnAllCategories() {
        when(repository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .thenReturn(List.of(category));
        when(mapper.toResponse(category)).thenReturn(response);

        List<CategoryResponse> result = categoryService.getAll();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
    }

    @Test
    void update_shouldUpdateCategory_whenNameIsUnique() {
        CategoryRequest updateRequest = new CategoryRequest(
                "Computers",
                "Computers and laptops"
        );

        CategoryResponse updatedResponse = new CategoryResponse(
                1L,
                "Computers",
                "Computers and laptops",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.existsByNameIgnoreCaseAndIdNot("Computers", 1L))
                .thenReturn(false);
        when(repository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(updatedResponse);

        CategoryResponse result = categoryService.update(1L, updateRequest);

        assertEquals("Computers", result.name());
        verify(mapper).updateEntity(category, updateRequest);
        verify(repository).save(category);
    }

    @Test
    void update_shouldThrowException_whenNameBelongsToAnotherCategory() {
        CategoryRequest updateRequest = new CategoryRequest(
                "Computers",
                "Computers and laptops"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(repository.existsByNameIgnoreCaseAndIdNot("Computers", 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateCategoryNameException.class,
                () -> categoryService.update(1L, updateRequest)
        );

        verify(repository, never()).save(any(Category.class));
    }

    @Test
    void delete_shouldDeleteCategory_whenCategoryExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        assertDoesNotThrow(() -> categoryService.delete(1L));

        verify(repository).delete(category);
    }

    @Test
    void delete_shouldThrowException_whenCategoryDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.delete(99L)
        );

        verify(repository, never()).delete(any(Category.class));
    }
}
