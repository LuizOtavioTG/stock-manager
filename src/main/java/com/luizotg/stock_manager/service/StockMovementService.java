package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.stockMovement.StockAdjustmentRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockInboundRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockOutboundRequestDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.InvalidStockMovementException;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    @Transactional
    public StockMovement saveOutboundMovement(StockOutboundRequestDTO dto) {
        inventoryService.applyOutboundStockMovement(
                dto.productId(),
                dto.storageLocationId(),
                dto.quantity()
        );

        StockMovement stockMovement = new StockMovement(new StockMovementCreateDTO(
                dto.productId(),
                dto.storageLocationId(),
                dto.quantity(),
                MovementType.OUTBOUND,
                dto.reason(),
                null,
                dto.reference(),
                dto.responsible(),
                dto.notes()
        ));

        return stockmovementRepository.save(stockMovement);
    }

    @Transactional
    public StockMovement saveAdjustmentMovement(StockAdjustmentRequestDTO dto) {
        InventoryService.StockAdjustmentResult adjustment = inventoryService.applyAdjustment(
                dto.productId(),
                dto.storageLocationId(),
                dto.newQuantity()
        );

        Integer movementQuantity = Math.abs(adjustment.difference());
        String balanceNotes = "Saldo anterior: " + adjustment.previousQuantity()
                + ". Novo saldo: " + adjustment.newQuantity()
                + ". Diferença: " + adjustment.difference() + ".";
        String notes = dto.notes() == null || dto.notes().isBlank()
                ? balanceNotes
                : dto.notes() + " " + balanceNotes;

        StockMovement stockMovement = new StockMovement(new StockMovementCreateDTO(
                dto.productId(),
                dto.storageLocationId(),
                movementQuantity,
                MovementType.ADJUSTMENT,
                dto.reason(),
                null,
                null,
                dto.responsible(),
                notes
        ));

        return stockmovementRepository.save(stockMovement);
    }

    private void validateMovement(StockMovementCreateDTO dto) {
        if (dto.movementType() == MovementType.TRANSFER) {
            throw new InvalidStockMovementException("TRANSFER precisa de origem e destino e será tratado em um fluxo separado.");
        }

        if (dto.movementType() == MovementType.ADJUSTMENT
                && (dto.reason() == null || dto.reason().isBlank())) {
            throw new InvalidStockMovementException("ADJUSTMENT exige motivo.");
        }
    }

    public void deleteStockMovementById(Long id) {
        throw new BusinessException(
                "StockMovement é histórico e não pode ser removida pela API.",
                HttpStatus.METHOD_NOT_ALLOWED,
                "STOCK_MOVEMENT_DELETE_NOT_ALLOWED"
        );
    }

    public StockMovement findStockMovementById(Long id) {
        return stockmovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimento de Estoque com ID " + id + " não encontrado."));
    }

    public StockMovement updateStockMovement(Long id, StockMovementUpdateDTO dto) {
        throw new BusinessException(
                "StockMovement é histórico e não pode ser alterada pela API.",
                HttpStatus.METHOD_NOT_ALLOWED,
                "STOCK_MOVEMENT_UPDATE_NOT_ALLOWED"
        );
    }
}
