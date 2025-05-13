package com.luizotg.stock_manager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name="Product")
@Table(name="products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    private String sku;
    private String name;
    private String description;
    private String brand;
    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private String unitOfMeasure;
    private Double costPrice;
    private Double salePrice;
    private Double active;

}
