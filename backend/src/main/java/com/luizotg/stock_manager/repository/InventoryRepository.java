package com.luizotg.stock_manager.repository;


import com.luizotg.stock_manager.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            AND (:productId IS NULL OR i.product.id = :productId)
            AND (:categoryId IS NULL OR i.product.category.id = :categoryId)
            AND (:storageLocationId IS NULL OR i.storageLocation.id = :storageLocationId)
            AND (:supplierId IS NULL OR EXISTS (
                SELECT s FROM Product p JOIN p.suppliers s
                WHERE p = i.product
                AND s.id = :supplierId
            ))
            """)
    Page<Inventory> findLowStock(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("storageLocationId") Long storageLocationId,
            @Param("supplierId") Long supplierId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(i) FROM Inventory i
            WHERE i.quantity <= i.minimumStock
            """)
    long countLowStock();

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.quantity <= i.reorderPoint
            AND (:productId IS NULL OR i.product.id = :productId)
            AND (:categoryId IS NULL OR i.product.category.id = :categoryId)
            AND (:storageLocationId IS NULL OR i.storageLocation.id = :storageLocationId)
            AND (:supplierId IS NULL OR EXISTS (
                SELECT s FROM Product p JOIN p.suppliers s
                WHERE p = i.product
                AND s.id = :supplierId
            ))
            """)
    Page<Inventory> findReorderNeeded(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("storageLocationId") Long storageLocationId,
            @Param("supplierId") Long supplierId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(i) FROM Inventory i
            WHERE i.quantity <= i.reorderPoint
            """)
    long countReorderNeeded();

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.quantity = 0
            AND (:productId IS NULL OR i.product.id = :productId)
            AND (:categoryId IS NULL OR i.product.category.id = :categoryId)
            AND (:storageLocationId IS NULL OR i.storageLocation.id = :storageLocationId)
            AND (:supplierId IS NULL OR EXISTS (
                SELECT s FROM Product p JOIN p.suppliers s
                WHERE p = i.product
                AND s.id = :supplierId
            ))
            """)
    Page<Inventory> findOutOfStock(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("storageLocationId") Long storageLocationId,
            @Param("supplierId") Long supplierId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(i) FROM Inventory i
            WHERE i.quantity = 0
            """)
    long countOutOfStock();

    @Query("""
            SELECT i FROM Inventory i
            WHERE i.maximumStock IS NOT NULL
            AND i.quantity > i.maximumStock
            AND (:productId IS NULL OR i.product.id = :productId)
            AND (:categoryId IS NULL OR i.product.category.id = :categoryId)
            AND (:storageLocationId IS NULL OR i.storageLocation.id = :storageLocationId)
            AND (:supplierId IS NULL OR EXISTS (
                SELECT s FROM Product p JOIN p.suppliers s
                WHERE p = i.product
                AND s.id = :supplierId
            ))
            """)
    Page<Inventory> findOverstock(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("storageLocationId") Long storageLocationId,
            @Param("supplierId") Long supplierId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(i) FROM Inventory i
            WHERE i.maximumStock IS NOT NULL
            AND i.quantity > i.maximumStock
            """)
    long countOverstock();
}
