package com.luizotg.stock_manager.dto.inventory;

import com.luizotg.stock_manager.model.Inventory;

public record InventoryDetailDTO(
        Long id,
        Long productId,
        Long storageLocationId,
        Integer quantity,
        String createdAt,
        String updatedAt
) {

    public InventoryDetailDTO(Inventory inventory) {
        this(
                inventory.getId(),
                inventory.getProduct() != null ? inventory.getProduct().getId() : null,
                inventory.getStorageLocation() != null ? inventory.getStorageLocation().getId() : null,
                inventory.getQuantity(),
                inventory.getCreatedAt() != null ? inventory.getCreatedAt().toString() : null,
                inventory.getUpdatedAt() != null ? inventory.getUpdatedAt().toString() : null
        );
    }
}
