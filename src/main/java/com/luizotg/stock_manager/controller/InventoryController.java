package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryDetailDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.service.InventoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
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
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<InventoryDetailDTO> detailInventory(
            @PathVariable Long id
    ){
        Inventory inventory = inventoryService.findInventoryById(id);
        return ResponseEntity.ok(new InventoryDetailDTO(inventory));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
    public ResponseEntity<InventoryDetailDTO> updateInventory(
            @PathVariable Long id,
            @RequestBody @Valid InventoryUpdateDTO inventoryUpdateDTO
    ){
        Inventory inventory = inventoryService.updateInventory(id, inventoryUpdateDTO);
        return ResponseEntity.ok(new InventoryDetailDTO(inventory));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<Page<InventoryDetailDTO>> listAllInventories(
            @PageableDefault(size = 10, sort = {"id"}) Pageable pageable
    ) {
        Page<Inventory> inventories = inventoryService.findAllInventories(pageable);
        Page<InventoryDetailDTO> dtoPage = inventories.map(InventoryDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<InventoryDetailDTO>> listLowStock(
            @PageableDefault(size = 10, sort = {"id"}) Pageable pageable
    ) {
        Page<InventoryDetailDTO> result = inventoryService.findLowStock(pageable)
                .map(InventoryDetailDTO::new);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{productId}/location/{storageLocationId}")
    public ResponseEntity<InventoryDetailDTO> detailInventoryByProductAndStorageLocation(
            @PathVariable Long productId,
            @PathVariable Long storageLocationId
    ) {
        Inventory inventory = inventoryService.findInventoryByProductAndStorageLocation(productId, storageLocationId);
        return ResponseEntity.ok(new InventoryDetailDTO(inventory));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryDetailDTO>> listInventoriesByProduct(
            @PathVariable Long productId
    ) {
        List<InventoryDetailDTO> inventories = inventoryService.findInventoriesByProductId(productId)
                .stream()
                .map(InventoryDetailDTO::new)
                .toList();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/location/{storageLocationId}")
    public ResponseEntity<List<InventoryDetailDTO>> listInventoriesByStorageLocation(
            @PathVariable Long storageLocationId
    ) {
        List<InventoryDetailDTO> inventories = inventoryService.findInventoriesByStorageLocationId(storageLocationId)
                .stream()
                .map(InventoryDetailDTO::new)
                .toList();
        return ResponseEntity.ok(inventories);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.luizotg.stock_manager.security.Roles).ADMIN)")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable Long id
    ){
        inventoryService.deleteInventoryById(id);
        return ResponseEntity.noContent().build();
    }

}
