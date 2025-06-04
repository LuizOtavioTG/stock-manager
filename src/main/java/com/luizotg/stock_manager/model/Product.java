package com.luizotg.stock_manager.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity(name="Product")
@Table(name="product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    @Setter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    @Setter
    private String sku;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private String brand;
    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Setter
    private String unitOfMeasure;
    @Setter
    private Double costPrice;
    @Setter
    private Double salePrice;
    @Setter
    private Boolean active = true;
    @Setter
    private LocalDate expirationDate;
    @Setter
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
}
