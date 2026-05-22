package com.luizotg.stock_manager.dto.stockMovement;

import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.dto.product.ProductSummaryDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationSummaryDTO;

import java.time.LocalDateTime;

public record StockMovementDetailDTO(

        Long id,
        ProductSummaryDTO product,
        StorageLocationSummaryDTO storageLocation,
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
                new ProductSummaryDTO(stockMovement.getProduct()),
                new StorageLocationSummaryDTO(stockMovement.getStorageLocation()),
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
