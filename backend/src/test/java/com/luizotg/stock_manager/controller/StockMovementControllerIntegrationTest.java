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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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
                INSERT INTO product (id, sku, name, brand, category_id, unit_of_measure, cost_price, sale_price, active)
                VALUES (3002, 'SKU-OTHER', 'Other product', 'Brand', 3001, 'UN', 10.0, 15.0, true)
                """);
        jdbcTemplate.update("""
                INSERT INTO storage_location (id, name, active)
                VALUES (3001, 'Movement warehouse', true)
                """);
        jdbcTemplate.update("""
                INSERT INTO storage_location (id, name, active)
                VALUES (3002, 'Other warehouse', true)
                """);
        jdbcTemplate.update("""
                INSERT INTO inventory (id, product_id, storage_location_id, quantity)
                VALUES (3001, 3001, 3001, 10)
                """);
        jdbcTemplate.update("""
                INSERT INTO inventory (id, product_id, storage_location_id, quantity)
                VALUES (3002, 3002, 3002, 20)
                """);

        insertMovement(4001L, 3001L, 3001L, "INBOUND", 5, "2026-05-01 10:00:00");
        insertMovement(4002L, 3001L, 3001L, "OUTBOUND", 3, "2026-05-10 11:00:00");
        insertMovement(4003L, 3002L, 3002L, "ADJUSTMENT", 12, "2026-05-15 12:00:00");
        insertMovement(4004L, 3001L, 3002L, "OUTBOUND", 2, "2026-05-22 13:00:00");
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

    @Test
    void searchWithoutFiltersReturnsAllMovementsPaged() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(4001, 4002, 4003, 4004)))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void searchByProductReturnsOnlyProductMovements() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("productId", "3001")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(4001, 4002, 4004)))
                .andExpect(jsonPath("$.content[*].product.id", containsInAnyOrder(3001, 3001, 3001)));
    }

    @Test
    void searchByStorageLocationReturnsOnlyLocationMovements() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("storageLocationId", "3002")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(4003, 4004)))
                .andExpect(jsonPath("$.content[*].storageLocation.id", containsInAnyOrder(3002, 3002)));
    }

    @Test
    void searchByMovementTypeReturnsOnlyTypeMovements() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("movementType", "OUTBOUND")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(4002, 4004)))
                .andExpect(jsonPath("$.content[*].movementType", containsInAnyOrder("OUTBOUND", "OUTBOUND")));
    }

    @Test
    void searchByPeriodReturnsOnlyMovementsInsidePeriod() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("startDate", "2026-05-10")
                        .param("endDate", "2026-05-15")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(4002, 4003)));
    }

    @Test
    void searchCombiningProductTypeAndPeriodReturnsMatchingMovements() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("productId", "3001")
                        .param("movementType", "OUTBOUND")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-20")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(4002))
                .andExpect(jsonPath("$.content[0].product.id").value(3001))
                .andExpect(jsonPath("$.content[0].movementType").value("OUTBOUND"));
    }

    @Test
    void searchWithStartDateAfterEndDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/stock-movements/search")
                        .param("startDate", "2026-05-22")
                        .param("endDate", "2026-05-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Data inicial não pode ser maior que a data final."))
                .andExpect(jsonPath("$.path").value("/api/stock-movements/search"));
    }

    private void insertMovement(
            Long id,
            Long productId,
            Long storageLocationId,
            String movementType,
            Integer quantity,
            String movementDate
    ) {
        jdbcTemplate.update("""
                INSERT INTO stock_movement (
                    id,
                    product_id,
                    storage_location_id,
                    quantity,
                    movement_type,
                    reason,
                    movement_date,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, 'Teste', ?, ?, ?)
                """, id, productId, storageLocationId, quantity, movementType, movementDate, movementDate, movementDate);
    }
}
