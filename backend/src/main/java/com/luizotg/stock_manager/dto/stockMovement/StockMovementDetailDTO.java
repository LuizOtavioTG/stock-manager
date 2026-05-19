package com.luizotg.stock_manager.dto.stockMovement;

import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.StorageLocation;

import java.time.LocalDateTime;

public record StockMovementDetailDTO(

        Long id,
        Product product,
        StorageLocation storageLocation,
        Integer quantity,
        MovementType movementType,
        String reason,
        LocalDateTime movementDate,
        String reference,
        String responsible,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
    public StockMovementDetailDTO(StockMovement stockMovement) {
        this(
                stockMovement.getId(),
                stockMovement.getProduct(),
                stockMovement.getStorageLocation(),
                stockMovement.getQuantity(),
                stockMovement.getMovementType(),
                stockMovement.getReason(),
                stockMovement.getMovementDate(),
                stockMovement.getReference(),
                stockMovement.getResponsible(),
                stockMovement.getNotes(),
                stockMovement.getCreatedAt(),
                stockMovement.getUpdatedAt()
        );
    }
}
