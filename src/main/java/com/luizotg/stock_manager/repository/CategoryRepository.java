package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
