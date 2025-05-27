package com.luizotg.stock_manager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name="StorageLocation")
@Table(name="storage_location")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StorageLocation {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @EqualsAndHashCode.Include
        private Long id;
        private String name;
        private String type;
        private String address;
        private String phoneNumber;
        private String email;
        private String responsibleName;
        private String notes;
        private Integer capacity;
        private Boolean defaultLocation = false;
        private Boolean active = true;
        private Double latitude;
        private Double longitude;
        @CreationTimestamp
        private LocalDateTime createdAt;
        @UpdateTimestamp
        private LocalDateTime updatedAt;
        @OneToMany(mappedBy = "storageLocation", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Inventory> inventories;

}
