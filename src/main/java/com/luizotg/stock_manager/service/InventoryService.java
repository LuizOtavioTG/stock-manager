package com.luizotg.stock_manager.service;

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

    @Transactional
    public Inventory saveInventory(InventoryCreateDTO inventoryDTO) {
        if (inventoryDTO.quantity() < 0) {
            throw new InvalidStockMovementException("Quantidade inicial do inventário não pode ser negativa.");
        }

        Product product = productRepository.findById(inventoryDTO.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + inventoryDTO.productId() + " não encontrado."));
        StorageLocation storageLocation = storageLocationRepository.findById(inventoryDTO.storageLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Local de armazenamento com ID " + inventoryDTO.storageLocationId() + " não encontrado."));

        if (inventoryRepository.findByProductIdAndStorageLocationId(product.getId(), storageLocation.getId()).isPresent()) {
            throw new DuplicateInventoryException("Inventário já existe para este produto e local de armazenamento.");
        }

        Inventory inventory = new Inventory(product, storageLocation, 0);
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
        throw new BusinessException(
                "Inventory não pode ser atualizado diretamente. Crie uma StockMovement para alterar o saldo.",
                HttpStatus.METHOD_NOT_ALLOWED,
                "INVENTORY_UPDATE_NOT_ALLOWED"
        );
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

}
