
package com.roshan.applecommerce.product.service;

import com.roshan.applecommerce.common.exception.DuplicateSkuException;
import com.roshan.applecommerce.common.exception.ProductNotFoundException;
import com.roshan.applecommerce.product.dto.ProductRequest;
import com.roshan.applecommerce.product.dto.ProductResponse;
import com.roshan.applecommerce.product.entity.Product;
import com.roshan.applecommerce.product.mapper.ProductMapper;
import com.roshan.applecommerce.product.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(
            ProductRepository repository,
            ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = request.sku().trim();

        if (repository.existsBySku(sku)) {
            throw new DuplicateSkuException(sku);
        }

        Product product = mapper.toEntity(request);
        return mapper.toResponse(repository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return mapper.toResponse(findProduct(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(
            int page, int size, String sortBy, String direction) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }

        if (!"id".equals(sortBy)
                && !"name".equals(sortBy)
                && !"price".equals(sortBy)
                && !"createdAt".equals(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field"
            );
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Direction must be ASC or DESC"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public ProductResponse update(
            Long id, ProductRequest request) {

        Product product = findProduct(id);
        String sku = request.sku().trim();

        if (repository.existsBySkuAndIdNot(sku, id)) {
            throw new DuplicateSkuException(sku);
        }

        mapper.updateEntity(product, request);

        return mapper.toResponse(repository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProduct(id);
        repository.delete(product);
    }

    private Product findProduct(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
