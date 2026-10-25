
package com.roshan.applecommerce.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "SKU is required")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]{3,50}$",
                message = "SKU must be 3-50 letters, numbers, hyphens or underscores"
        )
        String sku,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock
) {
}
