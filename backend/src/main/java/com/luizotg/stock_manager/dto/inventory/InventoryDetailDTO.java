package com.luizotg.stock_manager.dto.inventory;

import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.StockStatus;

public record InventoryDetailDTO(
        Long id,
        Long productId,
        String productName,
        Long storageLocationId,
        String storageLocationName,
        Integer quantity,
        Integer minimumStock,
        Integer maximumStock,
        Integer reorderPoint,
        StockStatus stockStatus,
        Boolean lowStock,
        Boolean reorderNeeded,
        Integer suggestedReorderQuantity
) {

    public InventoryDetailDTO(Inventory inventory) {
        this(
                inventory.getId(),
                inventory.getProduct() != null ? inventory.getProduct().getId() : null,
                inventory.getProduct() != null ? inventory.getProduct().getName() : null,
                inventory.getStorageLocation() != null ? inventory.getStorageLocation().getId() : null,
                inventory.getStorageLocation() != null ? inventory.getStorageLocation().getName() : null,
                inventory.getQuantity(),
                inventory.getMinimumStock(),
                inventory.getMaximumStock(),
                inventory.getReorderPoint(),
                inventory.getStockStatus(),
                inventory.isLowStock(),
                inventory.needsReorder(),
                inventory.getSuggestedReorderQuantity()
        );
    }
}
