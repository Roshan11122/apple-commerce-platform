package com.roshan.applecommerce.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.roshan.applecommerce.category.entity.Category;
import com.roshan.applecommerce.category.repository.CategoryRepository;
import com.roshan.applecommerce.common.exception.CategoryNotFoundException;
import com.roshan.applecommerce.common.exception.DuplicateSkuException;
import com.roshan.applecommerce.common.exception.ProductNotFoundException;
import com.roshan.applecommerce.product.dto.ProductRequest;
import com.roshan.applecommerce.product.dto.ProductResponse;
import com.roshan.applecommerce.product.entity.Product;
import com.roshan.applecommerce.product.mapper.ProductMapper;
import com.roshan.applecommerce.product.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService service;

    private ProductRequest request;
    private Product product;
    private ProductResponse response;
    private Category category;

    @BeforeEach
    void setUp() {
        request = new ProductRequest(
                "MacBook Air",
                "MACBOOKAIR001",
                "Apple laptop",
                new BigDecimal("99900.00"),
                10,
                1L
        );

        product = new Product(
                request.name(),
                request.sku(),
                request.description(),
                request.price(),
                request.stock()
        );

        category = mock(Category.class);
        response = mock(ProductResponse.class);
    }

    @Test
    void createProductSuccessfully() {
        when(repository.existsBySku("MACBOOKAIR001"))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(mapper.toEntity(request))
                .thenReturn(product);

        when(repository.save(product))
                .thenReturn(product);

        when(mapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = service.create(request);

        assertNotNull(result);
        assertSame(category, product.getCategory());

        verify(repository).existsBySku("MACBOOKAIR001");
        verify(categoryRepository).findById(1L);
        verify(repository).save(product);
        verify(mapper).toResponse(product);
    }

    @Test
    void createProductWithDuplicateSkuThrowsException() {
        when(repository.existsBySku("MACBOOKAIR001"))
                .thenReturn(true);

        assertThrows(
                DuplicateSkuException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any(Product.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void createProductWithMissingCategoryThrowsException() {
        when(repository.existsBySku("MACBOOKAIR001"))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any(Product.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void getExistingProductSuccessfully() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        when(mapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = service.getById(1L);

        assertNotNull(result);

        verify(repository).findById(1L);
        verify(mapper).toResponse(product);
    }

    @Test
    void getMissingProductThrowsException() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> service.getById(999L)
        );

        verify(mapper, never()).toResponse(any(Product.class));
    }

    @Test
    void updateProductSuccessfully() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        when(repository.existsBySkuAndIdNot("MACBOOKAIR001", 1L))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(repository.save(product))
                .thenReturn(product);

        when(mapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = service.update(1L, request);

        assertNotNull(result);
        assertSame(category, product.getCategory());

        verify(mapper).updateEntity(product, request);
        verify(categoryRepository).findById(1L);
        verify(repository).save(product);
        verify(mapper).toResponse(product);
    }

    @Test
    void updateProductWithDuplicateSkuThrowsException() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        when(repository.existsBySkuAndIdNot("MACBOOKAIR001", 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateSkuException.class,
                () -> service.update(1L, request)
        );

        verify(repository, never()).save(any(Product.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void updateProductWithMissingCategoryThrowsException() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        when(repository.existsBySkuAndIdNot("MACBOOKAIR001", 1L))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> service.update(1L, request)
        );

        verify(repository, never()).save(any(Product.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteExistingProductSuccessfully() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        service.delete(1L);

        verify(repository).findById(1L);
        verify(repository).delete(product);
    }

    @Test
    void getAllRejectsNegativePage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAll(-1, 10, "id", "ASC", null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getAllRejectsPageSizeAbove100() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAll(0, 101, "id", "ASC", null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getAllRejectsUnsupportedSortField() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAll(0, 10, "password", "ASC", null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getAllProductsSuccessfully() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(repository.findAll(any(Pageable.class)))
                .thenReturn(productPage);

        when(mapper.toResponse(product))
                .thenReturn(response);

        Page<ProductResponse> result =
                service.getAll(0, 10, "id", "ASC", null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));

        verify(repository).findAll(any(Pageable.class));
        verify(repository, never())
                .findAllByCategory_Id(anyLong(), any(Pageable.class));
    }

    @Test
    void getAllProductsByCategorySuccessfully() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(repository.findAllByCategory_Id(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(productPage);

        when(mapper.toResponse(product))
                .thenReturn(response);

        Page<ProductResponse> result =
                service.getAll(0, 10, "id", "ASC", 1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));

        verify(repository).findAllByCategory_Id(
                eq(1L),
                any(Pageable.class)
        );

        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllProductsByCategoryReturnsEmptyPageWhenNoProductsExist() {
        Page<Product> emptyPage = Page.empty();

        when(repository.findAllByCategory_Id(
                eq(999L),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        Page<ProductResponse> result =
                service.getAll(0, 10, "id", "ASC", 999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(repository).findAllByCategory_Id(
                eq(999L),
                any(Pageable.class)
        );

        verifyNoInteractions(mapper);
    }
}