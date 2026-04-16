package com.luizotg.stock_manager.repository;


import com.luizotg.stock_manager.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndStorageLocationId(Long productId, Long storageLocationId);

    boolean existsByProductIdAndStorageLocationId(Long productId, Long storageLocationId);

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByStorageLocationId(Long storageLocationId);

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.quantity <= i.minimumStock
            """)
    Page<Inventory> findLowStock(Pageable pageable);

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.quantity <= i.reorderPoint
            """)
    Page<Inventory> findReorderNeeded(Pageable pageable);
}
