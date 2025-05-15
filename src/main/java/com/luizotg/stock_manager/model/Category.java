package com.luizotg.stock_manager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity(name="Category")
@Table(name="category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    @Setter(AccessLevel.PRIVATE)
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)@EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference
    private Category parent;
    @OneToMany(mappedBy = "parent")
    @JsonManagedReference
    private List<Category> children;

    public Category( String name, String description, Boolean active, Category parent, List<Category> children) {
        this.name = name;
        this.description = description;
        this.active = true;
        this.parent = parent;
        this.children = children;
    }
    public Category(CategoryCreateDTO categoryDTO) {
        this.name = categoryDTO.name();
        this.description = categoryDTO.description() != null ? categoryDTO.description() : null;
        this.active = true;


        if (categoryDTO.parentId() != null) {
            Category parentCategory = new Category();
            parentCategory.setId(categoryDTO.parentId());  // Atribuindo apenas o ID
            this.parent = parentCategory;
        } else {
            this.parent = null;
        }


        this.children = categoryDTO.parentId() != null ? new ArrayList<>() : null;
    }
}
