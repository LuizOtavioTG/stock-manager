package com.luizotg.stock_manager.model;

import jakarta.persistence.*;
import lombok.*;
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
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product productId;
    @ManyToOne
    @JoinColumn(name = "storage_location_id")
    private StorageLocation storageLocationId;
    private Integer quantity;
    @UpdateTimestamp
    private LocalDateTime lastUpdated;
}
