package com.luizotg.stock_manager.dto.product;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ProductSummaryDTO(

        Long id,

        String sku,

        String name,

        String brand,

        Double salePrice,

        LocalDate expirationDate

) {}
