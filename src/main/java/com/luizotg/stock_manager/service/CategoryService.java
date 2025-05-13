package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.repository.CategoryRepository;
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

    public Category saveCategory(Category entity) {
        return categoryRepository.save(entity);
    }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }
}
