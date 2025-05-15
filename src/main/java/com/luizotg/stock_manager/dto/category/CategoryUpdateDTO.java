package com.luizotg.stock_manager.dto.category;

import com.luizotg.stock_manager.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryUpdateDTO(
        @NotNull(message = "{category.id.notnull}")
        Long id,

        @Size(max = 100, message = "{category.name.size}")
        @NotBlank(message = "{category.name.notblank}")
        String name,

        @Size(max = 255, message = "{category.description.size}")
        String description,

        Long parentId
) {
        public CategoryUpdateDTO(Category category) {
                this(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getParent() != null ? category.getParent().getId() : null
                );
        }
}
