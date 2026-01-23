package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.InventoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade inicial do inventário não pode ser negativa."
            );
        }

        Product product = productRepository.findById(inventoryDTO.productId())
                .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + inventoryDTO.productId() + " não encontrado."));
        StorageLocation storageLocation = storageLocationRepository.findById(inventoryDTO.storageLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Local de armazenamento com ID " + inventoryDTO.storageLocationId() + " não encontrado."));

        if (inventoryRepository.findByProductIdAndStorageLocationId(product.getId(), storageLocation.getId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventário já existe para este produto e local de armazenamento."
            );
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
            saved.addQuantity(inventoryDTO.quantity());
        }

        return inventoryRepository.save(saved);
    }

    public Inventory findInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estoque com ID " + id + " não encontrado."));
    }

    public void deleteInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventário não encontrado"));
        if (inventory.getQuantity() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.METHOD_NOT_ALLOWED,
                    "Inventário com saldo não pode ser removido diretamente."
            );
        }
        inventoryRepository.delete(inventory);
    }

    public Inventory updateInventory(Long id, InventoryUpdateDTO dto) {
        throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Inventory não pode ser atualizado diretamente. Crie uma StockMovement para alterar o saldo."
        );
    }

    public Inventory applyStockMovement(Long productId, Long storageLocationId, Integer quantity, MovementType movementType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + productId + " não encontrado."));
        StorageLocation storageLocation = storageLocationRepository.findById(storageLocationId)
                .orElseThrow(() -> new EntityNotFoundException("Local de armazenamento com ID " + storageLocationId + " não encontrado."));

        Inventory inventory = inventoryRepository
                .findByProductIdAndStorageLocationId(productId, storageLocationId)
                .orElseGet(() -> inventoryRepository.save(new Inventory(product, storageLocation, 0)));

        try {
            if (movementType == MovementType.OUTBOUND) {
                inventory.removeQuantity(quantity);
            } else {
                inventory.addQuantity(quantity);
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

        return inventoryRepository.save(inventory);
    }

}
