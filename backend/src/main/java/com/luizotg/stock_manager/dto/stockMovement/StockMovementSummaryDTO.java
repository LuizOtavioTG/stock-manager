package com.luizotg.stock_manager.dto.stockMovement;

import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.MovementType;

import java.time.LocalDateTime;

public record StockMovementSummaryDTO(

        Long id,
        Long productId,
        String productName,
        Integer quantity,
        MovementType movementType,
        LocalDateTime movementDate

) {
    public StockMovementSummaryDTO(StockMovement stockMovement) {
        this(
                stockMovement.getId(),
                stockMovement.getProduct() != null ? stockMovement.getProduct().getId() : null,
                stockMovement.getProduct() != null ? stockMovement.getProduct().getName() : null,
                stockMovement.getQuantity(),
                stockMovement.getMovementType(),
                stockMovement.getMovementDate()
        );
    }
}
