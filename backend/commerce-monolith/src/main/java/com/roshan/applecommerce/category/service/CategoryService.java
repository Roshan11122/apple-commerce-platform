package com.roshan.applecommerce.category.service;

import com.roshan.applecommerce.category.dto.CategoryRequest;
import com.roshan.applecommerce.category.dto.CategoryResponse;
import com.roshan.applecommerce.category.entity.Category;
import com.roshan.applecommerce.category.mapper.CategoryMapper;
import com.roshan.applecommerce.category.repository.CategoryRepository;

import com.roshan.applecommerce.common.exception.CategoryNotFoundException;
import com.roshan.applecommerce.common.exception.DuplicateCategoryNameException;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryService(
            CategoryRepository repository,
            CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {

        String name = request.name().trim();

        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryNameException(name);
        }

        Category category = mapper.toEntity(request);

        return mapper.toResponse(repository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return mapper.toResponse(findCategory(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {

        return repository.findAll(
                        Sort.by(Sort.Direction.ASC, "id")
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse update(
            Long id,
            CategoryRequest request) {

        Category category = findCategory(id);
        String name = request.name().trim();

        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateCategoryNameException(name);
        }

        mapper.updateEntity(category, request);

        return mapper.toResponse(repository.save(category));
    }

    @Transactional
    public void delete(Long id) {

        Category category = findCategory(id);

        repository.delete(category);
    }

    private Category findCategory(Long id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new CategoryNotFoundException(id)
                );
    }
}