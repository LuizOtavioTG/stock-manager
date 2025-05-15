package com.luizotg.stock_manager.dto.category;

import com.luizotg.stock_manager.model.Category;
import jakarta.validation.constraints.NotNull;

public record CategorySummaryDTO(

        Long id,
        String name,
        Boolean active

) {
    public CategorySummaryDTO(Category category) {
        this(
                category.getId(),
                category.getName(),
                category.getActive());
    }
}
