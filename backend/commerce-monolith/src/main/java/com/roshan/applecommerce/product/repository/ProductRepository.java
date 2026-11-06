
package com.roshan.applecommerce.product.repository;

import com.roshan.applecommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
    Page<Product> findAllByCategory_Id(
            Long categoryId,
            Pageable pageable
    );
}
