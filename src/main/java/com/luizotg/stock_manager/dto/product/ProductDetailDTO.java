package com.luizotg.stock_manager.dto.product;

import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.model.Supplier;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        List<Supplier> suppliers,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {}
