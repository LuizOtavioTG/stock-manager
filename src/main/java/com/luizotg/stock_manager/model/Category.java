package com.luizotg.stock_manager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
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
        this.name = categoryDTO.name();  // Nome não pode ser nulo ou vazio devido às validações no DTO
        this.description = categoryDTO.description() != null ? categoryDTO.description() : "Descrição não fornecida"; // Valor padrão para descrição
        this.active = true; // Definindo 'active' como true por padrão

        // Se o parentId for fornecido, você pode buscar a categoria pai, caso contrário, deixa null
        this.parent = categoryDTO.parentId() != null ? new Category(categoryDTO.parentId()) : null;

        // Se não houver children, você pode deixá-los como uma lista vazia ou nula, dependendo do seu modelo de dados
        this.children = categoryDTO.parentId() != null ? new ArrayList<>() : null;
    }
}
