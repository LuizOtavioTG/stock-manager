package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockmovementRepository;
    public StockMovementService(StockMovementRepository stockmovementRepository) {
        this.stockmovementRepository = stockmovementRepository;
    }

    public List<StockMovement> findAllStockMovements() {
        return stockmovementRepository.findAll();
    }

    public StockMovement saveStockMovement(StockMovement entity) {
        return stockmovementRepository.save(entity);
    }

    public void deleteStockMovementById(Long id) {
        stockmovementRepository.deleteById(id);
    }
}
