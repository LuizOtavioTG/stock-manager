package com.luizotg.stock_manager.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryCreateDTO(
        @NotNull(message = "{inventory.id.notnull}")
        Long id,

        @NotNull(message = "{inventory.productId.notnull}")
        Long productId,

        @NotNull(message = "{inventory.storageLocationId.notnull}")
        Long storageLocationId,

        @NotNull(message = "{inventory.quantity.notnull}")
        @Min(value = 0, message = "{inventory.quantity.min}")
        Integer quantity
) {
}
