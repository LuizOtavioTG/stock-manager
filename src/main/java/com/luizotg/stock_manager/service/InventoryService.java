package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> findAllInventories() {
        return inventoryRepository.findAll();
    }

    public Inventory saveInventory(Inventory entity) {
        return inventoryRepository.save(entity);
    }

    public void deleteInventoryById(Long id) {
        inventoryRepository.deleteById(id);
    }
}
