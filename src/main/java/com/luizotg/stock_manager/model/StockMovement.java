package com.luizotg.stock_manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity(name="StockMovement")
@Table(name="stock_movement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StockMovement {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long storageLocationId;
    private Integer quantity;
    private String movementType;
    private String reason;
    private LocalDateTime movementDate;
    private String reference;
    private String responsible;
    private String notes;

    @PrePersist
    public void prePersist() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }
}
