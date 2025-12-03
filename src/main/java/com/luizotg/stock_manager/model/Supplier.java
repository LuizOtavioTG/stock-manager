package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.supplier.SupplierCreateDTO;
import com.luizotg.stock_manager.dto.supplier.SupplierUpdateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name="Supplier")
@Table(name="supplier")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Supplier {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String contactName;
    private String phoneNumber;
    private String email;
    private String address;
    private Boolean active = true;
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
    @ManyToMany(mappedBy = "suppliers")
    private List<Product> products;

    public Supplier(SupplierCreateDTO dto) {
        this.name = dto.name();
        this.contactName = dto.contactName();
        this.phoneNumber = dto.phoneNumber();
        this.email = dto.email();
        this.address = dto.address();
        this.active = true;
    }

    public void updateFromDTO(SupplierUpdateDTO dto) {
        if (dto.name() != null) {
            this.name = dto.name();
        }
        if (dto.contactName() != null) {
            this.contactName = dto.contactName();
        }
        if (dto.phoneNumber() != null) {
            this.phoneNumber = dto.phoneNumber();
        }
        if (dto.email() != null) {
            this.email = dto.email();
        }
        if (dto.address() != null) {
            this.address = dto.address();
        }
        if (dto.active() != null) {
            this.active = dto.active();
        }
    }
}
