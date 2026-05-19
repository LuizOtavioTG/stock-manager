package com.luizotg.stock_manager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import com.luizotg.stock_manager.dto.category.CategoryUpdateDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Setter(AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Category {
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
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

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
    public void updateFromDTO(CategoryUpdateDTO dto) {
        this.name = dto.name();
        this.description = dto.description();

        if (dto.parentId() != null) {
            Category parentCategory = new Category();
            parentCategory.setId(dto.parentId());
            this.parent = parentCategory;
        } else {
            this.parent = null;
        }
    }
}
