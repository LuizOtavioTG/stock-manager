package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.stockMovement.StockInboundRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockMovementService {

    private final StockMovementRepository stockmovementRepository;
    private final InventoryService inventoryService;

    public StockMovementService(StockMovementRepository stockmovementRepository, InventoryService inventoryService) {
        this.stockmovementRepository = stockmovementRepository;
        this.inventoryService = inventoryService;
    }

    public Page<StockMovement> findAllStockMovements(Pageable pageable) {
        return stockmovementRepository.findAll(pageable);
    }

    @Transactional
    public StockMovement saveStockMovement(StockMovementCreateDTO dto) {
        validateMovement(dto);
        StockMovement stockMovement = new StockMovement(dto);
        StockMovement saved = stockmovementRepository.save(stockMovement);
        inventoryService.applyStockMovement(dto.productId(), dto.storageLocationId(), dto.quantity(), dto.movementType());
        return saved;
    }

    @Transactional
    public StockMovement saveInboundMovement(StockInboundRequestDTO dto) {
        inventoryService.applyStockMovement(
                dto.productId(),
                dto.storageLocationId(),
                dto.quantity(),
                MovementType.INBOUND
        );

        StockMovement stockMovement = new StockMovement(new StockMovementCreateDTO(
                dto.productId(),
                dto.storageLocationId(),
                dto.quantity(),
                MovementType.INBOUND,
                dto.reason(),
                null,
                dto.reference(),
                dto.responsible(),
                dto.notes()
        ));

        return stockmovementRepository.save(stockMovement);
    }

    private void validateMovement(StockMovementCreateDTO dto) {
        if (dto.movementType() == MovementType.TRANSFER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "TRANSFER precisa de origem e destino e será tratado em um fluxo separado."
            );
        }

        if (dto.movementType() == MovementType.ADJUSTMENT
                && (dto.reason() == null || dto.reason().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ADJUSTMENT exige motivo."
            );
        }
    }

    public void deleteStockMovementById(Long id) {
        throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "StockMovement é histórico e não pode ser removida pela API."
        );
    }

    public StockMovement findStockMovementById(Long id) {
        return stockmovementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimento de Estoque com ID " + id + " não encontrado."));
    }

    public StockMovement updateStockMovement(Long id, StockMovementUpdateDTO dto) {
        throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "StockMovement é histórico e não pode ser alterada pela API."
        );
    }
}
