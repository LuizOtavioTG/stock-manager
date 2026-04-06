package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByStorageLocationId(Long storageLocationId, Pageable pageable);
}
