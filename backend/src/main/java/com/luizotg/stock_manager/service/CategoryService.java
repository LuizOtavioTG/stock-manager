package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import com.luizotg.stock_manager.dto.category.CategoryUpdateDTO;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Category saveCategory(CategoryCreateDTO categoryDTO) {
        if (categoryDTO.parentId() != null) {
            Optional<Category> parentCategory = categoryRepository.findById(categoryDTO.parentId());
            if (parentCategory.isEmpty()) {
                throw new ResourceNotFoundException("Categoria pai não encontrada.");
            }
        }
        Category category = new Category(categoryDTO);

        return categoryRepository.save(category);
    }

    public void deleteCategoryById(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o id: " + id));
    }


    public Category updateCategory(Long id, CategoryUpdateDTO categoryUpdateDTO) {
        Category category = findCategoryById(id);
        category.updateFromDTO(categoryUpdateDTO);
        return categoryRepository.save(category);
    }
}
