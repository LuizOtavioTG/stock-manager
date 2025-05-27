package com.luizotg.stock_manager.dto.product;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ProductCreateDTO(

        @NotBlank(message = "{product.name.notblank}")
        @Size(max = 100, message = "{product.name.size}")
        String name,

        @Size(max = 255, message = "{product.description.size}")
        String description,

        @NotBlank(message = "{product.brand.notblank}")
        @Size(max = 50, message = "{product.brand.size}")
        String brand,

        @NotNull(message = "{product.categoryId.notnull}")
        Long categoryId,

        @NotBlank(message = "{product.unitOfMeasure.notblank}")
        @Size(max = 20, message = "{product.unitOfMeasure.size}")
        String unitOfMeasure,

        @NotNull(message = "{product.costPrice.notnull}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{product.costPrice.min}")
        Double costPrice,

        @NotNull(message = "{product.salePrice.notnull}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{product.salePrice.min}")
        Double salePrice,

        @FutureOrPresent(message = "{product.expirationDate.futureOrPresent}")
        LocalDate expirationDate,

        @NotNull(message = "{product.supplierIds.notnull}")
        @Size(min = 1, message = "{product.supplierIds.size}")
        Set<@NotNull(message = "{product.supplierIds.item.notnull}") Long> supplierIds

) {}
