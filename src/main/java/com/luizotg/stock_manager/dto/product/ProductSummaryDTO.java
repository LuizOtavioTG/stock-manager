package com.luizotg.stock_manager.dto.product;

import com.luizotg.stock_manager.model.Product;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ProductSummaryDTO(

        Long id,
        String sku,
        String name,
        String brand,
        Double salePrice,
        LocalDate expirationDate

) {
    public ProductSummaryDTO (Product product){
        this(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getBrand(),
                product.getSalePrice(),
                product.getExpirationDate()
        );
    }
}
