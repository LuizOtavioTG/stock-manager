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
@Setter(AccessLevel.PRIVATE)
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
    @Setter(AccessLevel.PUBLIC)
    private Integer quantity;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "created_by", length = 100)
    private String createdBy;
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public Inventory(InventoryCreateDTO inventoryDTO) {
        if (inventoryDTO.productId() != null) {
            this.product = new Product(inventoryDTO.productId());
        }
        if (inventoryDTO.storageLocationId() != null) {
            this.storageLocation = new StorageLocation(inventoryDTO.storageLocationId());
        }
        this.quantity = inventoryDTO.quantity();
    }
    public void updateFromDTO(InventoryUpdateDTO dto) {
        if (dto.productId() != null) {
            this.product = new Product(dto.productId());
        }

        if (dto.storageLocationId() != null) {
            this.storageLocation = new StorageLocation(dto.storageLocationId());
        }

        this.quantity = dto.quantity();
    }


}
