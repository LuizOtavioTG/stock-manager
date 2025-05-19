package com.luizotg.stock_manager.dto.inventory;

import com.luizotg.stock_manager.model.Inventory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryCreateDTO(

        @NotNull(message = "{inventory.productId.notnull}")
        Long productId,

        @NotNull(message = "{inventory.storageLocationId.notnull}")
        Long storageLocationId,

        @NotNull(message = "{inventory.quantity.notnull}")
        @Min(value = 0, message = "{inventory.quantity.min}")
        Integer quantity
) {
        public InventoryCreateDTO(Inventory inventory) {
               this(
                       inventory.getProduct() != null ? inventory.getProduct().getId() : null,
                       inventory.getStorageLocation() != null ? inventory.getStorageLocation().getId() : null,
                       inventory.getQuantity()
               );
        }
}
