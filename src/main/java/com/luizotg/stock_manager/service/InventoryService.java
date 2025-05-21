package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> findAllInventories() {
        return inventoryRepository.findAll();
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
    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estoque com ID " + id + " não encontrado."));
    }
    public void deleteInventoryById(Long id) {

        inventoryRepository.deleteById(id);
    }
}
