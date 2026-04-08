package com.luizotg.stock_manager.dto.inventory;

public record InventorySummaryDTO(
        Long id,
        Integer quantity,
        Integer minimumStock,
        Integer maximumStock,
        Integer reorderPoint
) {
}
