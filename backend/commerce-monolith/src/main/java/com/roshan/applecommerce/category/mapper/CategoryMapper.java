package com.roshan.applecommerce.category.mapper;

import com.roshan.applecommerce.category.dto.CategoryRequest;
import com.roshan.applecommerce.category.dto.CategoryResponse;
import com.roshan.applecommerce.category.entity.Category;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        return new Category(
                request.name().trim(),
                request.description()
        );
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public void updateEntity(
            Category category,
            CategoryRequest request) {

        category.update(
                request.name().trim(),
                request.description()
        );
    }
}