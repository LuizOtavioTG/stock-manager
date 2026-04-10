package com.luizotg.stock_manager.dto.inventory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void defaultsReorderPointToMinimumStockWhenNull() {
        InventoryCreateDTO dto = new InventoryCreateDTO(1L, 1L, 10, 5, null, null);

        assertThat(dto.reorderPoint()).isEqualTo(5);
    }

    @Test
    void rejectsMissingRequiredFields() {
        InventoryCreateDTO dto = new InventoryCreateDTO(null, null, null, null, null, null);

        Set<ConstraintViolation<InventoryCreateDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("productId", "storageLocationId", "quantity", "minimumStock");
    }

    @Test
    void rejectsNegativeValues() {
        InventoryCreateDTO dto = new InventoryCreateDTO(1L, 1L, -1, -1, -1, -1);

        Set<ConstraintViolation<InventoryCreateDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("quantity", "minimumStock", "maximumStock", "reorderPoint");
    }
}
