package com.luizotg.stock_manager.dto.inventory;

public record InventoryAlertSummaryDTO(
        Long outOfStockCount,
        Long lowStockCount,
        Long reorderNeededCount,
        Long overstockCount
) {
}
