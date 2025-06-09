package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.storageLocation.StorageLocationCreateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name="StorageLocation")
@Table(name="storage_location")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
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

        public StorageLocation (Long id){
                this.id = id;
        }

        public StorageLocation(StorageLocationCreateDTO dto) {
                this.name = dto.name();
                this.type = dto.type();
                this.address = dto.address();
                this.phoneNumber = dto.phoneNumber();
                this.email = dto.email();
                this.responsibleName = dto.responsibleName();
                this.notes = dto.notes();
                this.capacity = dto.capacity();
                this.defaultLocation = dto.defaultLocation();
                this.latitude = dto.latitude();
                this.longitude = dto.longitude();
                this.active = true;
        }
}
