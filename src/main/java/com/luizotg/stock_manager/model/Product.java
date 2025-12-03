package com.luizotg.stock_manager.model;

import com.luizotg.stock_manager.dto.product.ProductCreateDTO;
import com.luizotg.stock_manager.dto.product.ProductUpdateDTO;
import com.luizotg.stock_manager.repository.CategoryRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name="Product")
@Table(name="product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String brand;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private String unitOfMeasure;
    private Double costPrice;
    private Double salePrice;
    private Boolean active = true;
    private LocalDate expirationDate;
    @ManyToMany
    @JoinTable(
            name = "product_supplier",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    private Set<Supplier> suppliers;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockMovement> stockMovements;
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

    public Product (Long id){
        this.id = id;
    }

    public Product(ProductCreateDTO dto, CategoryRepository categoryRepository, SupplierRepository supplierRepository) {
        this.name = dto.name();
        this.description = dto.description();
        this.brand = dto.brand();
        this.unitOfMeasure = dto.unitOfMeasure();
        this.costPrice = dto.costPrice();
        this.salePrice = dto.salePrice();
        this.expirationDate = dto.expirationDate();
        this.active = true;

        if (dto.categoryId() != null) {
            this.category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria com ID " + dto.categoryId() + " não encontrada."));
        }

        if (dto.supplierIds() != null) {
            Set<Supplier> suppliers = new HashSet<>(supplierRepository.findAllById(dto.supplierIds()));
            if (suppliers.size() != dto.supplierIds().size()) {
                throw new EntityNotFoundException("Um ou mais fornecedores não foram encontrados.");
            }
            this.suppliers = suppliers;
        }
    }


    public void updateFromDTO(ProductUpdateDTO dto, CategoryRepository categoryRepository, SupplierRepository supplierRepository) {
        if (dto.name() != null) setName(dto.name());
        if (dto.description() != null) setDescription(dto.description());
        if (dto.brand() != null) setBrand(dto.brand());
        if (dto.unitOfMeasure() != null) setUnitOfMeasure(dto.unitOfMeasure());
        if (dto.costPrice() != null) setCostPrice(dto.costPrice());
        if (dto.salePrice() != null) setSalePrice(dto.salePrice());
        if (dto.expirationDate() != null) setExpirationDate(dto.expirationDate());
        if (dto.active() != null) setActive(dto.active());

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria com ID " + dto.categoryId() + " não encontrada."));
            setCategory(category);
        }

        if (dto.supplierIds() != null) {
            Set<Supplier> suppliers = new HashSet<>(supplierRepository.findAllById(dto.supplierIds()));
            if (suppliers.size() != dto.supplierIds().size()) {
                throw new EntityNotFoundException("Um ou mais fornecedores não foram encontrados.");
            }
            setSuppliers(suppliers);
        }
    }
}
