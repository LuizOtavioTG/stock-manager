package com.luizotg.stock_manager.dto.inventory;

public record InventoryDetailDTO(
        Long id,
        Long productId,
        Long storageLocationId,
        Integer quantity,
        String createdAt,
        String updatedAt
) {
}
