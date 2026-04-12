package com.luizotg.stock_manager.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryTest {

    @Test
    void calculatesOutOfStockStatus() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 0, 10, 100, 20);

        assertThat(inventory.getStockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void calculatesOutOfStockStatusWhenQuantityIsNull() {
        Inventory inventory = new Inventory();

        assertThat(inventory.getStockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void calculatesLowStockStatus() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 10, 10, 100, 20);

        assertThat(inventory.getStockStatus()).isEqualTo(StockStatus.LOW_STOCK);
    }

    @Test
    void calculatesReorderNeededStatus() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 15, 10, 100, 20);

        assertThat(inventory.getStockStatus()).isEqualTo(StockStatus.REORDER_NEEDED);
    }

    @Test
    void calculatesNormalStatus() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 50, 10, 100, 20);

        assertThat(inventory.getStockStatus()).isEqualTo(StockStatus.NORMAL);
    }

    @Test
    void calculatesOverstockStatus() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 101, 10, 100, 20);

        assertThat(inventory.getStockStatus()).isEqualTo(StockStatus.OVERSTOCK);
    }

    @Test
    void identifiesLowStockOnlyForOutOfStockAndLowStockStatuses() {
        Inventory lowStock = new Inventory(new Product(1L), new StorageLocation(1L), 10, 10, 100, 20);
        Inventory reorderNeeded = new Inventory(new Product(1L), new StorageLocation(1L), 15, 10, 100, 20);

        assertThat(lowStock.isLowStock()).isTrue();
        assertThat(reorderNeeded.isLowStock()).isFalse();
    }

    @Test
    void identifiesReorderNeededForOutOfStockLowStockAndReorderNeededStatuses() {
        Inventory outOfStock = new Inventory(new Product(1L), new StorageLocation(1L), 0, 10, 100, 20);
        Inventory lowStock = new Inventory(new Product(1L), new StorageLocation(1L), 10, 10, 100, 20);
        Inventory reorderNeeded = new Inventory(new Product(1L), new StorageLocation(1L), 15, 10, 100, 20);
        Inventory normal = new Inventory(new Product(1L), new StorageLocation(1L), 50, 10, 100, 20);

        assertThat(outOfStock.needsReorder()).isTrue();
        assertThat(lowStock.needsReorder()).isTrue();
        assertThat(reorderNeeded.needsReorder()).isTrue();
        assertThat(normal.needsReorder()).isFalse();
    }

    @Test
    void suggestsReorderQuantityUpToMaximumStockWhenAvailable() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 15, 10, 100, 20);

        assertThat(inventory.getSuggestedReorderQuantity()).isEqualTo(85);
    }

    @Test
    void suggestsReorderQuantityUpToReorderPointWhenMaximumStockIsUnavailable() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 15, 10, null, 20);

        assertThat(inventory.getSuggestedReorderQuantity()).isEqualTo(5);
    }

    @Test
    void returnsZeroSuggestedReorderQuantityWhenReorderIsNotNeeded() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 50, 10, 100, 20);

        assertThat(inventory.getSuggestedReorderQuantity()).isZero();
    }
}
