package com.luizotg.stock_manager.dto.category;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CategoryDetailDTO(

        Long id,
        String name,
        String description,
        Boolean active,
        CategorySummaryDTO parent,
        List<CategorySummaryDTO> children
) {}
