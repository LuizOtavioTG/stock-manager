package com.luizotg.stock_manager.model;

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
@AllArgsConstructor
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
        this.quantity = quantity;
    }

    public void addQuantity(Integer quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(Integer quantity) {
        int newQuantity = this.quantity - quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para a movimentação.");
        }
        this.quantity = newQuantity;
    }

    public void adjustQuantityTo(Integer newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Saldo ajustado não pode ser negativo.");
        }
        this.quantity = newQuantity;
    }

}
