package com.luizotg.stock_manager.repository;


import com.luizotg.stock_manager.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndStorageLocationId(Long productId, Long storageLocationId);

    boolean existsByProductIdAndStorageLocationId(Long productId, Long storageLocationId);

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByStorageLocationId(Long storageLocationId);
}
