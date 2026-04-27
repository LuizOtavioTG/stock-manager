package com.luizotg.stock_manager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM inventory");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM storage_location");
        jdbcTemplate.update("DELETE FROM category");

        jdbcTemplate.update("INSERT INTO category (id, name, active) VALUES (1001, 'Integration category', true)");
        jdbcTemplate.update("""
                INSERT INTO storage_location (id, name, active)
                VALUES (1001, 'Integration warehouse', true)
                """);

        insertProduct(1001L, "Out of stock product");
        insertProduct(1002L, "Low stock product");
        insertProduct(1003L, "Reorder needed product");
        insertProduct(1004L, "Overstock product");
        insertProduct(1005L, "Normal product");

        insertInventory(1001L, 1001L, 0, 10, 100, 20);
        insertInventory(1002L, 1002L, 5, 10, 100, 20);
        insertInventory(1003L, 1003L, 15, 10, 100, 20);
        insertInventory(1004L, 1004L, 120, 10, 100, 20);
        insertInventory(1005L, 1005L, 50, 10, 100, 20);
    }

    @Test
    void listLowStockReturnsOnlyItemsAtOrBelowMinimumStock() throws Exception {
        mockMvc.perform(get("/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].quantity", containsInAnyOrder(0, 5)))
                .andExpect(jsonPath("$.content[*].stockStatus", containsInAnyOrder("OUT_OF_STOCK", "LOW_STOCK")))
                .andExpect(jsonPath("$.content[*].suggestedReorderQuantity", containsInAnyOrder(100, 95)))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listReorderNeededReturnsItemsAtOrBelowReorderPoint() throws Exception {
        mockMvc.perform(get("/inventory/reorder-needed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].quantity", containsInAnyOrder(0, 5, 15)))
                .andExpect(jsonPath(
                        "$.content[*].stockStatus",
                        containsInAnyOrder("OUT_OF_STOCK", "LOW_STOCK", "REORDER_NEEDED")
                ))
                .andExpect(jsonPath("$.content[*].suggestedReorderQuantity", containsInAnyOrder(100, 95, 85)))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void listOutOfStockReturnsOnlyItemsWithZeroQuantity() throws Exception {
        mockMvc.perform(get("/inventory/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].quantity").value(0))
                .andExpect(jsonPath("$.content[0].stockStatus").value("OUT_OF_STOCK"))
                .andExpect(jsonPath("$.content[0].suggestedReorderQuantity").value(100))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listOverstockReturnsOnlyItemsAboveMaximumStock() throws Exception {
        mockMvc.perform(get("/inventory/overstock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].quantity").value(120))
                .andExpect(jsonPath("$.content[0].stockStatus").value("OVERSTOCK"))
                .andExpect(jsonPath("$.content[0].suggestedReorderQuantity").value(0))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getInventoryAlertSummaryReturnsCorrectCounters() throws Exception {
        mockMvc.perform(get("/inventory/alerts/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outOfStockCount").value(1))
                .andExpect(jsonPath("$.lowStockCount").value(2))
                .andExpect(jsonPath("$.reorderNeededCount").value(3))
                .andExpect(jsonPath("$.overstockCount").value(1));
    }

    private void insertProduct(Long id, String name) {
        jdbcTemplate.update("""
                INSERT INTO product (id, sku, name, category_id, active)
                VALUES (?, ?, ?, 1001, true)
                """, id, "SKU-" + id, name);
    }

    private void insertInventory(
            Long id,
            Long productId,
            Integer quantity,
            Integer minimumStock,
            Integer maximumStock,
            Integer reorderPoint
    ) {
        jdbcTemplate.update("""
                INSERT INTO inventory (
                    id,
                    product_id,
                    storage_location_id,
                    quantity,
                    minimum_stock,
                    maximum_stock,
                    reorder_point
                )
                VALUES (?, ?, 1001, ?, ?, ?, ?)
                """, id, productId, quantity, minimumStock, maximumStock, reorderPoint);
    }
}
