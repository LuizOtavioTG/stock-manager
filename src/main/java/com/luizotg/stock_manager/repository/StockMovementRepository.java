package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
