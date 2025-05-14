package com.luizotg.stock_manager.dto.category;

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
) {}
