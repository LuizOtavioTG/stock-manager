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
}
