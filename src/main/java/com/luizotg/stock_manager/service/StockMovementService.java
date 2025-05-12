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

    public List<StockMovement> findAll() {
        return stockmovementRepository.findAll();
    }

    public StockMovement save(StockMovement entity) {
        return stockmovementRepository.save(entity);
    }

    public void deleteById(Long id) {
        stockmovementRepository.deleteById(id);
    }
}
