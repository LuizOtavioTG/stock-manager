package com.luizotg.stock_manager.dto.category;

import jakarta.validation.constraints.NotNull;

public record CategorySummaryDTO(

        Long id,
        String name,
        Boolean active
) {}
