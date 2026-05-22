package com.luizotg.stock_manager.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "integration-admin",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    @Test
    void createProductWithEmptyNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePayload("SKU-EMPTY-NAME", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Nome do produto é obrigatório.")));
    }

    @Test
    void createProductWithBlankNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePayload("SKU-BLANK-NAME", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Nome do produto é obrigatório.")));
    }

    @Test
    void createProductWithNegativeCostPriceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-NEG-COST",
                                  "name": "Negative cost product",
                                  "brand": "Brand",
                                  "categoryId": 2001,
                                  "unitOfMeasure": "UN",
                                  "costPrice": -1.0,
                                  "salePrice": 15.0,
                                  "supplierIds": [2001]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Preço de custo não pode ser negativo.")));
    }

    @Test
    void createProductWithNegativeSalePriceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "SKU-NEG-SALE",
                                  "name": "Negative sale product",
                                  "brand": "Brand",
                                  "categoryId": 2001,
                                  "unitOfMeasure": "UN",
                                  "costPrice": 10.0,
                                  "salePrice": -1.0,
                                  "supplierIds": [2001]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Preço de venda não pode ser negativo.")));
    }

    @Test
    void updateProductWithEmptyNameReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/product/2001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Nome do produto não pode ser vazio."));
    }

    @Test
    void updateProductWithNegativeCostPriceReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/product/2001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costPrice": -1.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Preço de custo não pode ser negativo.")));
    }

    @Test
    void updateProductWithNegativeSalePriceReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/product/2001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "salePrice": -1.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Preço de venda não pode ser negativo.")));
    }

    private String validCreatePayload(String sku, String name) {
        return """
                {
                  "sku": "%s",
                  "name": "%s",
                  "brand": "Brand",
                  "categoryId": 2001,
                  "unitOfMeasure": "UN",
                  "costPrice": 10.0,
                  "salePrice": 15.0,
                  "supplierIds": [2001]
                }
                """.formatted(sku, name);
    }
}
