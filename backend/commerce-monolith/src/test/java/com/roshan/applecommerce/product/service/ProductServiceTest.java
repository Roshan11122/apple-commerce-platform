package com.roshan.applecommerce.product.service;

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

    @InjectMocks
    private ProductService service;

    private ProductRequest request;
    private Product product;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        request = new ProductRequest(
                "MacBook Air",
                "MACBOOKAIR001",
                "Apple laptop",
                new BigDecimal("99900.00"),
                10
        );

        product = new Product(
                request.name(),
                request.sku(),
                request.description(),
                request.price(),
                request.stock()
        );

        response = mock(ProductResponse.class);
    }

    @Test
    void createProductSuccessfully() {
        when(repository.existsBySku("MACBOOKAIR001"))
                .thenReturn(false);

        when(mapper.toEntity(request))
                .thenReturn(product);

        when(repository.save(product))
                .thenReturn(product);

        when(mapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = service.create(request);

        assertNotNull(result);

        verify(repository).existsBySku("MACBOOKAIR001");
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
    void getAllRejectsNegativePage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAll(-1, 10, "id", "ASC")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getAllRejectsPageSizeAbove100() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAll(0, 101, "id", "ASC")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void getAllRejectsUnsupportedSortField() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAll(0, 10, "password", "ASC")
        );

        verifyNoInteractions(repository);
    }
}