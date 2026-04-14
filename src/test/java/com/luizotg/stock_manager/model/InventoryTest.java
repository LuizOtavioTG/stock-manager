package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryTest {

    @Test
    void createsInventoryFromCreateDTOWithStockControlFields() {
        Inventory inventory = new Inventory(new InventoryCreateDTO(1L, 2L, 10, 5, 100, null));

        assertThat(inventory.getProduct().getId()).isEqualTo(1L);
        assertThat(inventory.getStorageLocation().getId()).isEqualTo(2L);
        assertThat(inventory.getQuantity()).isEqualTo(10);
        assertThat(inventory.getMinimumStock()).isEqualTo(5);
        assertThat(inventory.getMaximumStock()).isEqualTo(100);
        assertThat(inventory.getReorderPoint()).isEqualTo(5);
    }

    @Test
    void updatesInventoryFromUpdateDTOWithoutChangingQuantity() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(2L), 10, 5, 100, 10);

        inventory.updateFromDTO(new InventoryUpdateDTO(3L, 8, 120, 20));

        assertThat(inventory.getStorageLocation().getId()).isEqualTo(3L);
        assertThat(inventory.getQuantity()).isEqualTo(10);
        assertThat(inventory.getMinimumStock()).isEqualTo(8);
        assertThat(inventory.getMaximumStock()).isEqualTo(120);
        assertThat(inventory.getReorderPoint()).isEqualTo(20);
    }

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
