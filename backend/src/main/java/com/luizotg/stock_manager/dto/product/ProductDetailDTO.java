package com.luizotg.stock_manager.dto.product;

import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.Supplier;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;


public record ProductDetailDTO(

        Long id,
        String sku,
        String name,
        String description,
        String brand,
        String unitOfMeasure,
        Double costPrice,
        Double salePrice,
        Boolean active,
        LocalDate expirationDate,
        Category category,
        Set<Supplier> suppliers,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
    public ProductDetailDTO(Product product) {
        this(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getUnitOfMeasure(),
                product.getCostPrice(),
                product.getSalePrice(),
                product.getActive(),
                product.getExpirationDate(),
                product.getCategory() != null ? product.getCategory(): null,
                product.getSuppliers() != null ? product.getSuppliers(): null,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
