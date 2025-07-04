package com.luizotg.stock_manager.dto.stockMovement;

import com.luizotg.stock_manager.model.MovementType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record StockMovementUpdateDTO(

        Long productId,
        Long storageLocationId,
        @Min(value = 1, message = "{stockMovement.quantity.min}")
        Integer quantity,

        MovementType movementType,

        @Size(max = 255, message = "{stockMovement.reason.size}")
        String reason,

        @PastOrPresent(message = "{stockMovement.movementDate.pastOrPresent}")
        LocalDateTime movementDate,

        @Size(max = 100, message = "{stockMovement.reference.size}")
        String reference,

        @Size(max = 100, message = "{stockMovement.responsible.size}")
        String responsible,

        @Size(max = 500, message = "{stockMovement.notes.size}")
        String notes

) {}
