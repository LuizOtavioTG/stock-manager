package com.luizotg.stock_manager.dto.inventory;

import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StockStatus;
import com.luizotg.stock_manager.model.StorageLocation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryDetailDTOTest {

    @Test
    void includesCalculatedStockStatus() {
        Inventory inventory = new Inventory(new Product(1L), new StorageLocation(1L), 15, 10, 100, 20);

        InventoryDetailDTO dto = new InventoryDetailDTO(inventory);

        assertThat(dto.stockStatus()).isEqualTo(StockStatus.REORDER_NEEDED);
        assertThat(dto.suggestedReorderQuantity()).isEqualTo(85);
        assertThat(dto.lowStock()).isFalse();
        assertThat(dto.needsReorder()).isTrue();
    }
}
