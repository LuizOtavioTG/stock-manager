package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.stockMovement.StockAdjustmentRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockInboundRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockOutboundRequestDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.InactiveResourceException;
import com.luizotg.stock_manager.exception.InvalidStockMovementException;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
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
    private final ProductRepository productRepository;
    private final StorageLocationRepository storageLocationRepository;

    public StockMovementService(
            StockMovementRepository stockmovementRepository,
            InventoryService inventoryService,
            ProductRepository productRepository,
            StorageLocationRepository storageLocationRepository
    ) {
        this.stockmovementRepository = stockmovementRepository;
        this.inventoryService = inventoryService;
        this.productRepository = productRepository;
        this.storageLocationRepository = storageLocationRepository;
    }

    public Page<StockMovement> findAllStockMovements(Pageable pageable) {
        return stockmovementRepository.findAll(pageable);
    }

    public Page<StockMovement> findStockMovementsByProductId(Long productId, Pageable pageable) {
        return stockmovementRepository.findByProductId(productId, pageable);
    }

    public Page<StockMovement> findStockMovementsByStorageLocationId(Long storageLocationId, Pageable pageable) {
        return stockmovementRepository.findByStorageLocationId(storageLocationId, pageable);
    }

    public Page<StockMovement> findStockMovementsByMovementType(MovementType movementType, Pageable pageable) {
        return stockmovementRepository.findByMovementType(movementType, pageable);
    }

    @Transactional
    public StockMovement registerInbound(StockInboundRequestDTO dto) {
        validateMovementQuantity(dto.quantity());
        Product product = validateActiveProduct(dto.productId());
        StorageLocation storageLocation = validateActiveStorageLocation(dto.storageLocationId());

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
        ), product, storageLocation);
    }

    @Transactional
    public StockMovement registerOutbound(StockOutboundRequestDTO dto) {
        validateMovementQuantity(dto.quantity());
        Product product = validateActiveProduct(dto.productId());
        StorageLocation storageLocation = validateActiveStorageLocation(dto.storageLocationId());

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
        ), product, storageLocation);
    }

    @Transactional
    public StockMovement registerAdjustment(StockAdjustmentRequestDTO dto) {
        validateAdjustment(dto);
        Product product = validateActiveProduct(dto.productId());
        StorageLocation storageLocation = validateActiveStorageLocation(dto.storageLocationId());

        InventoryService.StockAdjustmentResult adjustment = inventoryService.applyAdjustment(
                dto.productId(),
                dto.storageLocationId(),
                dto.newQuantity()
        );

        Integer movementQuantity = Math.abs(adjustment.difference());
        validateMovementQuantity(movementQuantity);

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
        ), product, storageLocation);
    }

    private StockMovement saveMovement(StockMovementCreateDTO dto, Product product, StorageLocation storageLocation) {
        validateMovementQuantity(dto.quantity());
        return stockmovementRepository.save(new StockMovement(dto, product, storageLocation));
    }

    private Product validateActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new InactiveResourceException("Produto inativo não pode receber movimentação de estoque.");
        }

        return product;
    }

    private StorageLocation validateActiveStorageLocation(Long storageLocationId) {
        StorageLocation storageLocation = storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento não encontrado."));

        if (Boolean.FALSE.equals(storageLocation.getActive())) {
            throw new InactiveResourceException("Local de armazenamento inativo não pode receber movimentação.");
        }

        return storageLocation;
    }

    private void validateMovementQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidStockMovementException("Quantidade movimentada deve ser maior que zero.");
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
