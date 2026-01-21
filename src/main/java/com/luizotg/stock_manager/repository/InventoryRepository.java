package com.luizotg.stock_manager.repository;


import com.luizotg.stock_manager.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndStorageLocationId(Long productId, Long storageLocationId);
}
