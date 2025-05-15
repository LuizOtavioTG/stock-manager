package com.luizotg.stock_manager.dto.category;

import com.luizotg.stock_manager.model.Category;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CategoryDetailDTO(

        Long id,
        String name,
        String description,
        Boolean active,
        CategorySummaryDTO parent,
        List<CategorySummaryDTO> children
) {
    public CategoryDetailDTO(Category category) {
        this(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive(),
                category.getParent() != null ? new CategorySummaryDTO(category.getParent()) : null,
                category.getChildren() != null
                        ? category.getChildren().stream()
                        .map(CategorySummaryDTO::new)
                        .toList()
                        : List.of()
        );
    }
}
