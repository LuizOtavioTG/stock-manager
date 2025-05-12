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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String brand;

    @Enumerated(EnumType.STRING)
    private Category category;
    private String unitOfMeasure;
    private double costPrice;
    private double salePrice;
    private boolean active;
}
