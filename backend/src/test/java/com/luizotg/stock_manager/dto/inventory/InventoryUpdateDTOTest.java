package com.luizotg.stock_manager.dto.inventory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryUpdateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rejectsNegativeStockControlValues() {
        InventoryUpdateDTO dto = new InventoryUpdateDTO(null, -1, -1, -1);

        Set<ConstraintViolation<InventoryUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("minimumStock", "maximumStock", "reorderPoint");
    }

    @Test
    void rejectsInconsistentStockControlLimits() {
        InventoryUpdateDTO dto = new InventoryUpdateDTO(null, 20, 100, 10);

        Set<ConstraintViolation<InventoryUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("stockControlsValid");
    }
}
