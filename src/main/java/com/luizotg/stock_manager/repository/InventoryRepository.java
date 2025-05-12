package com.luizotg.stock_manager.repository;


import com.luizotg.stock_manager.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
