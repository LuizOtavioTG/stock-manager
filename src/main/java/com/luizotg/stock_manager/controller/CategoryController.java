package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.category.CategoryCreateDTO;
import com.luizotg.stock_manager.dto.category.CategoryDetailDTO;
import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CategoryDetailDTO> createCategory(@RequestBody @Valid CategoryCreateDTO categoryDTO) {
        Category category = categoryService.saveCategory(categoryDTO);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(category.getId())
                .toUri();
        return ResponseEntity.created(uri)
                .body(new CategoryDetailDTO(category));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDetailDTO> detailCategory(@PathVariable Long id) {
        Category category = categoryService.findCategoryById(id);
        return ResponseEntity.ok(new CategoryDetailDTO(category));
    }
    @GetMapping
    public ResponseEntity<Page<CategoryDetailDTO>> listAllCategories(@PageableDefault(size = 10, sort ={"name"}) Pageable pageable) {
        Page<Category> categories = categoryService.findAllCategories(pageable);
        Page<CategoryDetailDTO> dtoPage = categories.map(CategoryDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory (@PathVariable Long id) {
        categoryService.findCategoryById(id);
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
