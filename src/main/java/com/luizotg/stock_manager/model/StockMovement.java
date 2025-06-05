package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity(name="StockMovement")
@Table(name="stock_movement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
public class StockMovement {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne
    @JoinColumn(name = "storage_location_id", nullable = false)
    private StorageLocation storageLocation;
    private Integer quantity;
    private MovementType movementType;
    private String reason;
    private LocalDateTime movementDate;
    private String reference;
    private String responsible;
    private String notes;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }

    public StockMovement(StockMovementCreateDTO stockMovementCreateDTO) {

        if(stockMovementCreateDTO.productId() != null) {
            Product product = new Product();
            product.setId(stockMovementCreateDTO.productId());
            this.product = product;
        }
        if(stockMovementCreateDTO.storageLocationId() != null) {
            StorageLocation storageLocation = new StorageLocation();
            storageLocation.setId(stockMovementCreateDTO.storageLocationId());
            this.storageLocation = storageLocation;
        }
        this.quantity = stockMovementCreateDTO.quantity();
        this.movementType = stockMovementCreateDTO.movementType();
        this.reason = stockMovementCreateDTO.reason();
        this.movementDate = this.getMovementDate();
        this.reference = stockMovementCreateDTO.reference();
        this.responsible = stockMovementCreateDTO.responsible();
        this.notes = stockMovementCreateDTO.notes();

    }
    public StockMovement(Long id, StockMovementUpdateDTO stockMovementUpdateDTO) {
        this.id = id;
        if(stockMovementUpdateDTO.productId() != null) {
            Product product = new Product();
            product.setId(stockMovementUpdateDTO.productId());
            this.product = product;
        }
            if(stockMovementUpdateDTO.storageLocationId() != null) {
            StorageLocation storageLocation = new StorageLocation();
            storageLocation.setId(stockMovementUpdateDTO.storageLocationId());
            this.storageLocation = storageLocation;
        }
        this.quantity = stockMovementUpdateDTO.quantity();
        this.movementType = stockMovementUpdateDTO.movementType();
        this.reason = stockMovementUpdateDTO.reason();
        this.movementDate = this.getMovementDate();
        this.reference = stockMovementUpdateDTO.reference();
        this.responsible = stockMovementUpdateDTO.responsible();
        this.notes = stockMovementUpdateDTO.notes();
    }
}
