package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public Category saveCategory(@Valid CategoryCreateDTO categoryDTO) {

        Category category = new Category();
        return categoryRepository.save(category);
    }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com o id: " + id));
    }
}
