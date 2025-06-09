package com.luizotg.stock_manager.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryUpdateDTO(


        Long productId,


        Long storageLocationId,


        @Min(value = 0, message = "{inventory.quantity.min}")
        Integer quantity
) {
}
