package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity(name="Inventory")
@Table(name="inventory")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    @Setter(AccessLevel.PUBLIC)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Setter(AccessLevel.PUBLIC)
    @ManyToOne
    @JoinColumn(name = "storage_location_id")
    private StorageLocation storageLocation;
    @Setter(AccessLevel.PUBLIC)
    private Integer quantity;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Inventory(InventoryCreateDTO inventoryDTO) {
        if (inventoryDTO.productId() != null) {
            Product product = new Product();
            this.product = product;
        }
        if (inventoryDTO.storageLocationId() != null) {
            StorageLocation storageLocation = new StorageLocation();
            this.storageLocation = storageLocation;
        }
        this.quantity = inventoryDTO.quantity();
    }
    public Inventory(InventoryUpdateDTO inventoryDTO) {
        if (inventoryDTO.productId() != null) {
            Product product = new Product();
            this.product = product;
        }
        if (inventoryDTO.storageLocationId() != null) {
            StorageLocation storageLocation = new StorageLocation();
            this.storageLocation = storageLocation;
        }
        this.quantity = inventoryDTO.quantity();
    }


}
