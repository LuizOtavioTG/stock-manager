package com.luizotg.stock_manager.dto.category;

import com.luizotg.stock_manager.model.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryCreateDTO(
        @NotBlank(message = "{category.name.notblank}")
        @Size(max = 100, message = "{category.name.size}")
        String name,

        @Size(max = 255, message = "{category.description.size}")
        String description,

        Long parentId
) {
        public CategoryCreateDTO(Category category) {
                this(
                        category.getName(),
                        category.getDescription(),
                        category.getParent() != null ? category.getParent().getId() : null
                );
        }
}