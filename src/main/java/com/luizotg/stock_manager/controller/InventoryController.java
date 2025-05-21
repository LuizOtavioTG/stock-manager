package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryDetailDTO;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.service.InventoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @GetMapping("/{id}")
    public ResponseEntity<InventoryDetailDTO> detailInventory(
            @PathVariable Long id
    ){
        Inventory inventory = inventoryService.findById(id);
        return ResponseEntity.ok(new InventoryDetailDTO(inventory));
    }
    @GetMapping
    public ResponseEntity<Page<InventoryDetailDTO>> listAllInventories(
            @PageableDefault(size = 10, sort = {"id"}) Pageable pageable
    ) {
        Page<Inventory> inventories = inventoryService.findAllInventories(pageable);
        Page<InventoryDetailDTO> dtoPage = inventories.map(InventoryDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }
}
