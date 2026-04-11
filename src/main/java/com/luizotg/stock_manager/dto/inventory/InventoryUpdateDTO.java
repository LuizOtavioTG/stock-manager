package com.luizotg.stock_manager.dto.inventory;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryUpdateDTO(

        Long storageLocationId,

        @PositiveOrZero(message = "{inventory.minimumStock.min}")
        Integer minimumStock,

        @PositiveOrZero(message = "{inventory.maximumStock.min}")
        Integer maximumStock,

        @PositiveOrZero(message = "{inventory.reorderPoint.min}")
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
