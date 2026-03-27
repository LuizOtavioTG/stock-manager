package com.luizotg.stock_manager.dto.stockMovement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequestDTO(

        @NotNull(message = "{stockMovement.productId.notnull}")
        Long productId,

        @NotNull(message = "{stockMovement.storageLocationId.notnull}")
        Long storageLocationId,

        @NotNull(message = "{stockMovement.quantity.notnull}")
        @PositiveOrZero(message = "{stockMovement.newQuantity.positiveOrZero}")
        Integer newQuantity,

        @NotBlank(message = "{stockMovement.reason.notblank}")
        @Size(max = 255, message = "{stockMovement.reason.size}")
        String reason,

        @Size(max = 100, message = "{stockMovement.responsible.size}")
        String responsible,

        @Size(max = 500, message = "{stockMovement.notes.size}")
        String notes
) {
}
