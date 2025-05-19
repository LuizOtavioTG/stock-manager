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

}
