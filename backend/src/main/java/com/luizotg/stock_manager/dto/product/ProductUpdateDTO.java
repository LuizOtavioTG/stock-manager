package com.luizotg.stock_manager.dto.product;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

public record ProductUpdateDTO(

        @Size(max = 50, message = "SKU deve ter no máximo 50 caracteres.")
        String sku,

        @Size(max = 100, message = "Nome do produto deve ter no máximo 100 caracteres.")
        String name,

        @Size(max = 255, message = "{product.description.size}")
        String description,

        @Size(max = 50, message = "{product.brand.size}")
        String brand,

        Long categoryId,

        @Size(max = 20, message = "{product.unitOfMeasure.size}")
        String unitOfMeasure,

        @DecimalMin(value = "0.0", inclusive = true, message = "Preço de custo não pode ser negativo.")
        Double costPrice,

        @DecimalMin(value = "0.0", inclusive = true, message = "Preço de venda não pode ser negativo.")
        Double salePrice,

        Boolean active,

        @FutureOrPresent(message = "{product.expirationDate.futureOrPresent}")
        LocalDate expirationDate,

        Set<@NotNull(message = "{product.supplierIds.item.notnull}") Long> supplierIds

) {}
