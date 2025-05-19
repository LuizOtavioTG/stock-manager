package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryDetailDTO;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.service.InventoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<InventoryDetailDTO> createInventory(
            @RequestBody @Valid InventoryCreateDTO inventoryCreateDTO
    ) {
        Inventory inventory = inventoryService.saveInventory(inventoryCreateDTO);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(inventory.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new InventoryDetailDTO(inventory));
    }
}
