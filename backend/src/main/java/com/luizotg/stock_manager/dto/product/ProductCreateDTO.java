package com.luizotg.stock_manager.dto.product;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

public record ProductCreateDTO(

        @NotBlank(message = "SKU é obrigatório.")
        @Size(max = 50, message = "SKU deve ter no máximo 50 caracteres.")
        String sku,

        @NotBlank(message = "Nome do produto é obrigatório.")
        @Size(max = 100, message = "Nome do produto deve ter no máximo 100 caracteres.")
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

        @NotNull(message = "Preço de custo é obrigatório.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Preço de custo não pode ser negativo.")
        Double costPrice,

        @NotNull(message = "Preço de venda é obrigatório.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Preço de venda não pode ser negativo.")
        Double salePrice,

        @FutureOrPresent(message = "{product.expirationDate.futureOrPresent}")
        LocalDate expirationDate,

        @NotNull(message = "{product.supplierIds.notnull}")
        @Size(min = 1, message = "{product.supplierIds.size}")
        Set<@NotNull(message = "{product.supplierIds.item.notnull}") Long> supplierIds

) {}
