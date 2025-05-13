package com.luizotg.stock_manager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Entity(name="Category")
@Table(name="category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference
    private Category parent;
    @JsonManagedReference
    private List<Category> children;

}
