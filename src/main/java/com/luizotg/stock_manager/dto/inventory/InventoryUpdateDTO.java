package com.luizotg.stock_manager.dto.inventory;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

public record InventoryUpdateDTO(


        Long productId,


        Long storageLocationId,


        @Min(value = 0, message = "{inventory.quantity.min}")
        Integer quantity,

        @Min(value = 0, message = "{inventory.minimumStock.min}")
        Integer minimumStock,

        @Min(value = 0, message = "{inventory.maximumStock.min}")
        Integer maximumStock,

        @Min(value = 0, message = "{inventory.reorderPoint.min}")
        Integer reorderPoint
) {
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
