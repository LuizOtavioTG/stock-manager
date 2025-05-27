package com.luizotg.stock_manager.dto.product;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ProductUpdateDTO(

        @Size(max = 100, message = "{product.name.size}")
        String name,

        @Size(max = 255, message = "{product.description.size}")
        String description,

        @Size(max = 50, message = "{product.brand.size}")
        String brand,

        Long categoryId,

        @Size(max = 20, message = "{product.unitOfMeasure.size}")
        String unitOfMeasure,

        @DecimalMin(value = "0.0", inclusive = true, message = "{product.costPrice.min}")
        Double costPrice,

        @DecimalMin(value = "0.0", inclusive = true, message = "{product.salePrice.min}")
        Double salePrice,

        Boolean active,

        @FutureOrPresent(message = "{product.expirationDate.futureOrPresent}")
        LocalDate expirationDate,

        Set<@NotNull(message = "{product.supplierIds.item.notnull}") Long> supplierIds

) {}
