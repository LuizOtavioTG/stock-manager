package com.luizotg.stock_manager.dto.stockMovement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockInboundRequestDTO(

        @NotNull(message = "{stockMovement.productId.notnull}")
        Long productId,

        @NotNull(message = "{stockMovement.storageLocationId.notnull}")
        Long storageLocationId,

        @NotNull(message = "Quantidade é obrigatória.")
        @Positive(message = "Quantidade movimentada deve ser maior que zero.")
        Integer quantity,

        @Size(max = 255, message = "{stockMovement.reason.size}")
        String reason,

        @Size(max = 100, message = "{stockMovement.reference.size}")
        String reference,

        @Size(max = 100, message = "{stockMovement.responsible.size}")
        String responsible,

        @Size(max = 500, message = "{stockMovement.notes.size}")
        String notes
) {
}
