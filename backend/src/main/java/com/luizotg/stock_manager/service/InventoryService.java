package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.inventory.InventoryAlertSummaryDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.DuplicateInventoryException;
import com.luizotg.stock_manager.exception.InvalidStockMovementException;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.InventoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            StorageLocationRepository storageLocationRepository,
            StockMovementRepository stockMovementRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Page<Inventory> findAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    public Page<Inventory> findLowStock(Pageable pageable) {
        return findLowStock(null, null, null, null, pageable);
    }

    public Page<Inventory> findLowStock(
            Long productId,
            Long categoryId,
            Long storageLocationId,
            Long supplierId,
            Pageable pageable
    ) {
        return inventoryRepository.findLowStock(productId, categoryId, storageLocationId, supplierId, pageable);
    }

    public Page<Inventory> findReorderNeeded(Pageable pageable) {
        return findReorderNeeded(null, null, null, null, pageable);
    }

    public Page<Inventory> findReorderNeeded(
            Long productId,
            Long categoryId,
            Long storageLocationId,
            Long supplierId,
            Pageable pageable
    ) {
        return inventoryRepository.findReorderNeeded(productId, categoryId, storageLocationId, supplierId, pageable);
    }

    public Page<Inventory> findOutOfStock(Pageable pageable) {
        return findOutOfStock(null, null, null, null, pageable);
    }

    public Page<Inventory> findOutOfStock(
            Long productId,
            Long categoryId,
            Long storageLocationId,
            Long supplierId,
            Pageable pageable
    ) {
        return inventoryRepository.findOutOfStock(productId, categoryId, storageLocationId, supplierId, pageable);
    }

    public Page<Inventory> findOverstock(Pageable pageable) {
        return findOverstock(null, null, null, null, pageable);
    }

    public Page<Inventory> findOverstock(
            Long productId,
            Long categoryId,
            Long storageLocationId,
            Long supplierId,
            Pageable pageable
    ) {
        return inventoryRepository.findOverstock(productId, categoryId, storageLocationId, supplierId, pageable);
    }

    public InventoryAlertSummaryDTO getInventoryAlertSummary() {
        return new InventoryAlertSummaryDTO(
                inventoryRepository.countOutOfStock(),
                inventoryRepository.countLowStock(),
                inventoryRepository.countReorderNeeded(),
                inventoryRepository.countOverstock()
        );
    }

    public Inventory findInventoryByProductAndStorageLocation(Long productId, Long storageLocationId) {
        return inventoryRepository.findByProductIdAndStorageLocationId(productId, storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventário não encontrado para este produto e local de armazenamento."));
    }

    public List<Inventory> findInventoriesByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<Inventory> findInventoriesByStorageLocationId(Long storageLocationId) {
        return inventoryRepository.findByStorageLocationId(storageLocationId);
    }

    @Transactional
    public Inventory saveInventory(InventoryCreateDTO inventoryDTO) {
        if (inventoryDTO.quantity() < 0) {
            throw new InvalidStockMovementException("Quantidade inicial do inventário não pode ser negativa.");
        }
        validateStockLimits(
                inventoryDTO.minimumStock(),
                inventoryDTO.reorderPoint(),
                inventoryDTO.maximumStock()
        );

        Product product = productRepository.findById(inventoryDTO.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + inventoryDTO.productId() + " não encontrado."));
        StorageLocation storageLocation = storageLocationRepository.findById(inventoryDTO.storageLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento com ID " + inventoryDTO.storageLocationId() + " não encontrado."));

        if (inventoryRepository.existsByProductIdAndStorageLocationId(product.getId(), storageLocation.getId())) {
            throw new DuplicateInventoryException("Inventário já existe para este produto e local de armazenamento.");
        }

        Inventory inventory = new Inventory(
                product,
                storageLocation,
                0,
                inventoryDTO.minimumStock(),
                inventoryDTO.maximumStock(),
                inventoryDTO.reorderPoint()
        );
        Inventory saved = inventoryRepository.save(inventory);

        if (inventoryDTO.quantity() > 0) {
            StockMovement initialBalance = new StockMovement(new StockMovementCreateDTO(
                    product.getId(),
                    storageLocation.getId(),
                    inventoryDTO.quantity(),
                    MovementType.INITIAL_BALANCE,
                    "Saldo inicial do inventário.",
                    null,
                    "INVENTORY-" + saved.getId(),
                    null,
                    null
            ));
            stockMovementRepository.save(initialBalance);
            saved.increaseQuantity(inventoryDTO.quantity());
        }

        return inventoryRepository.save(saved);
    }

    public Inventory findInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque com ID " + id + " não encontrado."));
    }

    public void deleteInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventário não encontrado"));
        if (inventory.getQuantity() != 0) {
            throw new BusinessException(
                    "Inventário com saldo não pode ser removido diretamente.",
                    HttpStatus.METHOD_NOT_ALLOWED,
                    "INVENTORY_DELETE_NOT_ALLOWED"
            );
        }
        inventoryRepository.delete(inventory);
    }

    public Inventory updateInventory(Long id, InventoryUpdateDTO dto) {
        validateStockLimits(dto.minimumStock(), dto.reorderPoint(), dto.maximumStock());

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque com ID " + id + " não encontrado."));

        if (dto.storageLocationId() != null) {
            StorageLocation storageLocation = storageLocationRepository.findById(dto.storageLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento com ID " + dto.storageLocationId() + " não encontrado."));

            Long productId = inventory.getProduct() != null ? inventory.getProduct().getId() : null;
            Long currentStorageLocationId = inventory.getStorageLocation() != null
                    ? inventory.getStorageLocation().getId()
                    : null;
            if (productId != null
                    && !storageLocation.getId().equals(currentStorageLocationId)
                    && inventoryRepository.existsByProductIdAndStorageLocationId(productId, storageLocation.getId())) {
                throw new DuplicateInventoryException("Inventário já existe para este produto e local de armazenamento.");
            }
        }

        inventory.updateFromDTO(dto);
        return inventoryRepository.save(inventory);
    }

    public Inventory applyStockMovement(Long productId, Long storageLocationId, Integer quantity, MovementType movementType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + productId + " não encontrado."));
        StorageLocation storageLocation = storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento com ID " + storageLocationId + " não encontrado."));

        Inventory inventory = inventoryRepository
                .findByProductIdAndStorageLocationId(productId, storageLocationId)
                .orElseGet(() -> inventoryRepository.save(new Inventory(product, storageLocation, 0)));

        switch (movementType) {
            case INBOUND, RETURN, ADJUSTMENT, INITIAL_BALANCE -> inventory.increaseQuantity(quantity);
            case OUTBOUND, LOSS, DAMAGED -> inventory.decreaseQuantity(quantity);
            case TRANSFER -> throw new InvalidStockMovementException("TRANSFER precisa de origem e destino e será tratado em um fluxo separado.");
        }

        return inventoryRepository.save(inventory);
    }

    public Inventory applyOutboundStockMovement(Long productId, Long storageLocationId, Integer quantity) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + productId + " não encontrado."));
        storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento com ID " + storageLocationId + " não encontrado."));

        Inventory inventory = inventoryRepository
                .findByProductIdAndStorageLocationId(productId, storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventário não encontrado para este produto e local de armazenamento."));

        inventory.decreaseQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public StockAdjustmentResult applyAdjustment(Long productId, Long storageLocationId, Integer newQuantity) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + productId + " não encontrado."));
        storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento com ID " + storageLocationId + " não encontrado."));

        Inventory inventory = inventoryRepository
                .findByProductIdAndStorageLocationId(productId, storageLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventário não encontrado para este produto e local de armazenamento."));

        Integer previousQuantity = inventory.getQuantity();
        inventory.adjustQuantity(newQuantity);

        Inventory saved = inventoryRepository.save(inventory);
        return new StockAdjustmentResult(saved, previousQuantity, newQuantity, newQuantity - previousQuantity);
    }

    public record StockAdjustmentResult(
            Inventory inventory,
            Integer previousQuantity,
            Integer newQuantity,
            Integer difference
    ) {
    }

    private void validateStockLimits(Integer minimumStock, Integer reorderPoint, Integer maximumStock) {
        if (minimumStock != null && minimumStock < 0) {
            throw new InvalidStockMovementException("Estoque mínimo não pode ser negativo.");
        }
        if (reorderPoint != null && reorderPoint < 0) {
            throw new InvalidStockMovementException("Ponto de reposição não pode ser negativo.");
        }
        if (maximumStock != null && maximumStock < 0) {
            throw new InvalidStockMovementException("Estoque máximo não pode ser negativo.");
        }
        if (minimumStock != null && reorderPoint != null && reorderPoint < minimumStock) {
            throw new InvalidStockMovementException("Ponto de reposição deve ser maior ou igual ao estoque mínimo.");
        }
        if (maximumStock != null && minimumStock != null && maximumStock < minimumStock) {
            throw new InvalidStockMovementException("Estoque máximo deve ser maior ou igual ao estoque mínimo.");
        }
        if (maximumStock != null && reorderPoint != null && maximumStock < reorderPoint) {
            throw new InvalidStockMovementException("Estoque máximo deve ser maior ou igual ao ponto de reposição.");
        }
    }

}
