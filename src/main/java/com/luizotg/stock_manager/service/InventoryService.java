package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.InventoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StorageLocationRepository storageLocationRepository;
    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            StorageLocationRepository storageLocationRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storageLocationRepository= storageLocationRepository;
    }


    public Page<Inventory> findAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

    public Inventory saveInventory(InventoryCreateDTO inventoryDTO) {
        if (inventoryDTO.productId() != null) {
            Optional<Inventory> inventory = inventoryRepository.findById(inventoryDTO.productId());
            if (inventory.isPresent()) {
                throw new IllegalArgumentException("ProdutoId não encontrado.");
            }
        }
        Inventory inventory = new Inventory(inventoryDTO);
        return inventoryRepository.save(inventory);
    }
    public Inventory findInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estoque com ID " + id + " não encontrado."));
    }
    public void deleteInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventário não encontrado"));
        inventoryRepository.delete(inventory);
    }
    public Inventory updateInventory(Long id, InventoryUpdateDTO dto) {
        Inventory inventory = findInventoryById(id);

        if (dto.productId() != null) {
            Product product = productRepository.findById(dto.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + dto.productId() + " não encontrado."));
            inventory.setProduct(product);
        }

        if (dto.storageLocationId() != null) {
            StorageLocation location = storageLocationRepository.findById(dto.storageLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Local de armazenamento com ID " + dto.storageLocationId() + " não encontrado."));
            inventory.setStorageLocation(location);
        }

        if (dto.quantity() != null) {
            inventory.setQuantity(dto.quantity());
        }

        return inventoryRepository.save(inventory);
    }

}
