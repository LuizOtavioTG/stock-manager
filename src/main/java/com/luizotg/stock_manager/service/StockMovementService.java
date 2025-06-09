package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockmovementRepository;

    public StockMovementService(StockMovementRepository stockmovementRepository) {
        this.stockmovementRepository = stockmovementRepository;
    }

    public Page<StockMovement> findAllStockMovements(Pageable pageable) {
        return stockmovementRepository.findAll(pageable);
    }

    public StockMovement saveStockMovement(StockMovementCreateDTO dto) {
        StockMovement stockMovement = new StockMovement(dto);
        return stockmovementRepository.save(stockMovement);
    }

    public void deleteStockMovementById(Long id) {
        StockMovement stockMovement = findStockMovementById(id);
        stockmovementRepository.delete(stockMovement);
    }

    public StockMovement findStockMovementById(Long id) {
        return stockmovementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimento de Estoque com ID " + id + " não encontrado."));
    }

    public StockMovement updateStockMovement(Long id, StockMovementUpdateDTO dto) {
        StockMovement existing = findStockMovementById(id);
        existing.updateFromDTO(dto);
        return stockmovementRepository.save(existing);
    }
}
