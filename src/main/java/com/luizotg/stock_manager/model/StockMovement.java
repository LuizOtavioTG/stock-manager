package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Entity(name="StockMovement")
@Table(name="stock_movement")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class StockMovement {
    @Setter(AccessLevel.PRIVATE)
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
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (movementDate == null) {
            movementDate = LocalDateTime.now();
        }
    }

    public StockMovement(StockMovementCreateDTO dto) {
        applyDto(dto.productId(), dto.storageLocationId(), dto.quantity(), dto.movementType(),
                dto.reason(), dto.movementDate(), dto.reference(), dto.responsible(), dto.notes());
    }

    public void updateFromDTO(StockMovementUpdateDTO dto) {
        applyDto(dto.productId(), dto.storageLocationId(), dto.quantity(), dto.movementType(),
                dto.reason(), dto.movementDate(), dto.reference(), dto.responsible(), dto.notes());
    }

    private void applyDto(Long productId, Long storageLocationId, Integer quantity, MovementType movementType,
                          String reason, LocalDateTime movementDate, String reference,
                          String responsible, String notes) {

        if (productId != null) {
            this.product = new Product(productId);
        }

        if (storageLocationId != null) {
            this.storageLocation = new StorageLocation(storageLocationId);
        }

        this.quantity = quantity;
        this.movementType = movementType;
        this.reason = reason;
        this.movementDate = (movementDate != null) ? movementDate : this.movementDate;
        this.reference = reference;
        this.responsible = responsible;
        this.notes = notes;
    }

}
