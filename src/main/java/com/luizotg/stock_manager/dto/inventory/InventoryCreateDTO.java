package com.luizotg.stock_manager.dto.inventory;

import com.luizotg.stock_manager.model.Inventory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryCreateDTO(

        @NotNull(message = "{inventory.productId.notnull}")
        Long productId,

        @NotNull(message = "{inventory.storageLocationId.notnull}")
        Long storageLocationId,

        @NotNull(message = "{inventory.quantity.notnull}")
        @PositiveOrZero(message = "{inventory.quantity.min}")
        Integer quantity,

        @NotNull(message = "{inventory.minimumStock.notnull}")
        @PositiveOrZero(message = "{inventory.minimumStock.min}")
        Integer minimumStock,

        @PositiveOrZero(message = "{inventory.maximumStock.min}")
        Integer maximumStock,

        @PositiveOrZero(message = "{inventory.reorderPoint.min}")
        Integer reorderPoint
) {
        public InventoryCreateDTO {
               if (reorderPoint == null) {
                      reorderPoint = minimumStock;
               }
        }

        public InventoryCreateDTO(Inventory inventory) {
               this(
                       inventory.getProduct() != null ? inventory.getProduct().getId() : null,
                       inventory.getStorageLocation() != null ? inventory.getStorageLocation().getId() : null,
                       inventory.getQuantity(),
                       inventory.getMinimumStock(),
                       inventory.getMaximumStock(),
                       inventory.getReorderPoint()
               );
        }

        @AssertTrue(message = "{inventory.stockControls.valid}")
        public boolean isStockControlsValid() {
               if (minimumStock != null && maximumStock != null && maximumStock < minimumStock) {
                      return false;
               }
               if (minimumStock != null && reorderPoint != null && reorderPoint < minimumStock) {
                      return false;
               }
               return maximumStock == null || reorderPoint == null || reorderPoint <= maximumStock;
        }
}
