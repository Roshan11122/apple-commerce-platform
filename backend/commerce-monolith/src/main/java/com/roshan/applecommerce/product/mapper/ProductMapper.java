
package com.roshan.applecommerce.product.mapper;

import com.roshan.applecommerce.product.dto.ProductRequest;
import com.roshan.applecommerce.product.dto.ProductResponse;
import com.roshan.applecommerce.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return new Product(
                request.name().trim(),
                request.sku().trim(),
                request.description(),
                request.price(),
                request.stock()
        );
    }

    public void updateEntity(
            Product product,
            ProductRequest request) {
        product.update(
                request.name().trim(),
                request.sku().trim(),
                request.description(),
                request.price(),
                request.stock()
        );
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
