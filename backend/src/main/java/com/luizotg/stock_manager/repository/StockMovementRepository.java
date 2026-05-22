package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByStorageLocationId(Long storageLocationId, Pageable pageable);

    Page<StockMovement> findByMovementType(MovementType movementType, Pageable pageable);

    @Query("""
            SELECT sm
            FROM StockMovement sm
            WHERE (:productId IS NULL OR sm.product.id = :productId)
              AND (:storageLocationId IS NULL OR sm.storageLocation.id = :storageLocationId)
              AND (:movementType IS NULL OR sm.movementType = :movementType)
              AND (:startDate IS NULL OR sm.movementDate >= :startDate)
              AND (:endDate IS NULL OR sm.movementDate <= :endDate)
            """)
    Page<StockMovement> search(
            @Param("productId") Long productId,
            @Param("storageLocationId") Long storageLocationId,
            @Param("movementType") MovementType movementType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
