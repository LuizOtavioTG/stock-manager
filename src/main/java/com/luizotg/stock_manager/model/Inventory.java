package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.exception.InsufficientStockException;
import com.luizotg.stock_manager.exception.InvalidStockMovementException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Entity(name="Inventory")
@Table(name="inventory")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter(AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "storage_location_id")
    private StorageLocation storageLocation;
    private Integer quantity;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
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

    public Inventory(Product product, StorageLocation storageLocation, Integer quantity) {
        this.product = product;
        this.storageLocation = storageLocation;
        adjustQuantity(quantity);
    }

    public Inventory(
            Product product,
            StorageLocation storageLocation,
            Integer quantity,
            Integer minimumStock,
            Integer maximumStock,
            Integer reorderPoint
    ) {
        this(product, storageLocation, quantity);
        updateStockControls(minimumStock, maximumStock, reorderPoint);
    }

    public void increaseQuantity(Integer quantity) {
        this.quantity += quantity;
    }

    public void decreaseQuantity(Integer quantity) {
        if (this.quantity < quantity) {
            throw new InsufficientStockException("Estoque insuficiente.");
        }
        this.quantity -= quantity;
    }

    public void adjustQuantity(Integer newQuantity) {
        if (newQuantity < 0) {
            throw new InvalidStockMovementException("Saldo ajustado não pode ser negativo.");
        }
        this.quantity = newQuantity;
    }

    public void updateStockControls(Integer minimumStock, Integer maximumStock, Integer reorderPoint) {
        validateStockControls(minimumStock, maximumStock, reorderPoint);
        this.minimumStock = minimumStock;
        this.maximumStock = maximumStock;
        this.reorderPoint = reorderPoint;
    }

    public boolean isOutOfStock() {
        return quantity != null && quantity == 0;
    }

    public boolean isBelowMinimumStock() {
        return quantity != null && minimumStock != null && quantity <= minimumStock;
    }

    public boolean needsReorder() {
        return quantity != null && reorderPoint != null && quantity <= reorderPoint;
    }

    public boolean isAboveMaximumStock() {
        return quantity != null && maximumStock != null && quantity > maximumStock;
    }

    public StockStatus getStockStatus() {
        if (isOutOfStock()) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (isAboveMaximumStock()) {
            return StockStatus.OVERSTOCK;
        }
        if (isBelowMinimumStock()) {
            return StockStatus.LOW_STOCK;
        }
        if (needsReorder()) {
            return StockStatus.REORDER_NEEDED;
        }
        return StockStatus.NORMAL;
    }

    private void validateStockControls(Integer minimumStock, Integer maximumStock, Integer reorderPoint) {
        if (minimumStock != null && minimumStock < 0) {
            throw new InvalidStockMovementException("Estoque mínimo não pode ser negativo.");
        }
        if (maximumStock != null && maximumStock < 0) {
            throw new InvalidStockMovementException("Estoque máximo não pode ser negativo.");
        }
        if (reorderPoint != null && reorderPoint < 0) {
            throw new InvalidStockMovementException("Ponto de reposição não pode ser negativo.");
        }
        if (minimumStock != null && maximumStock != null && maximumStock < minimumStock) {
            throw new InvalidStockMovementException("Estoque máximo deve ser maior ou igual ao estoque mínimo.");
        }
        if (minimumStock != null && reorderPoint != null && reorderPoint < minimumStock) {
            throw new InvalidStockMovementException("Ponto de reposição deve ser maior ou igual ao estoque mínimo.");
        }
        if (maximumStock != null && reorderPoint != null && reorderPoint > maximumStock) {
            throw new InvalidStockMovementException("Ponto de reposição deve ser menor ou igual ao estoque máximo.");
        }
    }

}
