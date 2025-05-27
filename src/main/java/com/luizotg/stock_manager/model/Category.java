package com.luizotg.stock_manager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import com.luizotg.stock_manager.dto.category.CategoryUpdateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


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
    private Boolean active = true;
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference
    private Category parent;
    @OneToMany(mappedBy = "parent")
    @JsonManagedReference
    private List<Category> children;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Category( String name, String description, Category parent, List<Category> children) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.children = children;
    }
    public Category(CategoryCreateDTO categoryDTO) {
        this.name = categoryDTO.name();
        this.description = categoryDTO.description() != null ? categoryDTO.description() : null;

        if (categoryDTO.parentId() != null) {
            Category parentCategory = new Category();
            parentCategory.setId(categoryDTO.parentId());
            this.parent = parentCategory;
        } else {
            this.parent = null;
        }

        this.children = categoryDTO.parentId() != null ? new ArrayList<>() : null;
    }
    public void updateFromDTO(CategoryUpdateDTO dto, Function<Long, Category> parentResolver) {
        this.name = dto.name();
        this.description = dto.description();

        if (dto.parentId() != null) {
            this.parent = parentResolver.apply(dto.parentId());
        } else {
            this.parent = null;
        }
    }
}
