package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Page<Category> findAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Category saveCategory(@Valid CategoryCreateDTO categoryDTO) {
        if (categoryDTO.parentId() != null) {
            Optional<Category> parentCategory = categoryRepository.findById(categoryDTO.parentId());
            // TODO: Do it on @RestControllerAdvice
            if (parentCategory.isEmpty()) {
                throw new IllegalArgumentException("Categoria pai não encontrada.");
            }
        }
        Category category = new Category(categoryDTO);

        return categoryRepository.save(category);
    }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    public Category findCategoryById(Long id) {
        // TODO: Do it on @RestControllerAdvice
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com o id: " + id));
    }
}
