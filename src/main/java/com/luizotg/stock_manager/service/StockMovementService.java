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
    public StockMovement registerInbound(StockInboundRequestDTO dto) {
        validatePositiveQuantity(dto.quantity(), "Quantidade de entrada deve ser maior que zero.");

        inventoryService.applyStockMovement(
                dto.productId(),
                dto.storageLocationId(),
                dto.quantity(),
                MovementType.INBOUND
        );

        return saveMovement(new StockMovementCreateDTO(
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
    }

    @Transactional
    public StockMovement registerOutbound(StockOutboundRequestDTO dto) {
        validatePositiveQuantity(dto.quantity(), "Quantidade de saída deve ser maior que zero.");

        inventoryService.applyOutboundStockMovement(
                dto.productId(),
                dto.storageLocationId(),
                dto.quantity()
        );

        return saveMovement(new StockMovementCreateDTO(
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
    }

    @Transactional
    public StockMovement registerAdjustment(StockAdjustmentRequestDTO dto) {
        validateAdjustment(dto);

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

        return saveMovement(new StockMovementCreateDTO(
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
    }

    private StockMovement saveMovement(StockMovementCreateDTO dto) {
        return stockmovementRepository.save(new StockMovement(dto));
    }

    private void validatePositiveQuantity(Integer quantity, String message) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidStockMovementException(message);
        }
    }

    private void validateAdjustment(StockAdjustmentRequestDTO dto) {
        if (dto.newQuantity() == null || dto.newQuantity() < 0) {
            throw new InvalidStockMovementException("Novo saldo ajustado deve ser maior ou igual a zero.");
        }

        if (dto.reason() == null || dto.reason().isBlank()) {
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
