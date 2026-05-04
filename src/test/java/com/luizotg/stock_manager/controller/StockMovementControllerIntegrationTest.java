package com.luizotg.stock_manager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StockMovementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM stock_movement");
        jdbcTemplate.update("DELETE FROM inventory");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM storage_location");
        jdbcTemplate.update("DELETE FROM category");

        jdbcTemplate.update("INSERT INTO category (id, name, active) VALUES (3001, 'Movement category', true)");
        jdbcTemplate.update("""
                INSERT INTO product (id, sku, name, brand, category_id, unit_of_measure, cost_price, sale_price, active)
                VALUES (3001, 'SKU-MOVEMENT', 'Movement product', 'Brand', 3001, 'UN', 10.0, 15.0, true)
                """);
        jdbcTemplate.update("""
                INSERT INTO storage_location (id, name, active)
                VALUES (3001, 'Movement warehouse', true)
                """);
        jdbcTemplate.update("""
                INSERT INTO inventory (id, product_id, storage_location_id, quantity)
                VALUES (3001, 3001, 3001, 10)
                """);
    }

    @Test
    void createOutboundMovementWithoutEnoughStockReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/stock-movements/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 3001,
                                  "storageLocationId": 3001,
                                  "quantity": 11,
                                  "reason": "Venda",
                                  "reference": "ORDER-001",
                                  "responsible": "Luiz"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para realizar saída."))
                .andExpect(jsonPath("$.path").value("/api/stock-movements/outbound"));
    }
}
