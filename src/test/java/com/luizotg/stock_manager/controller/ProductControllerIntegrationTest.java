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
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM product_supplier");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM supplier");
        jdbcTemplate.update("DELETE FROM category");

        jdbcTemplate.update("INSERT INTO category (id, name, active) VALUES (2001, 'Product integration category', true)");
        jdbcTemplate.update("""
                INSERT INTO supplier (id, name, active)
                VALUES (2001, 'Product integration supplier', true)
                """);
        jdbcTemplate.update("""
                INSERT INTO product (id, sku, name, brand, category_id, unit_of_measure, cost_price, sale_price, active)
                VALUES (2001, 'SKU-DUP', 'Existing product', 'Brand', 2001, 'UN', 10.0, 15.0, true)
                """);
    }

    @Test
    void createProductWithDuplicateSkuReturnsConflict() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-DUP",
                                  "name": "New product",
                                  "brand": "Brand",
                                  "categoryId": 2001,
                                  "unitOfMeasure": "UN",
                                  "costPrice": 10.0,
                                  "salePrice": 15.0,
                                  "supplierIds": [2001]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("SKU já está em uso."))
                .andExpect(jsonPath("$.path").value("/product"));
    }
}
