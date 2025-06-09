package com.luizotg.stock_manager.controller;


import com.luizotg.stock_manager.dto.inventory.InventoryDetailDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationCreateDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationDetailDTO;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.service.StorageLocationService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/storage-location")
public class StorageLocationController {

    private final StorageLocationService storageLocationService;
    private StorageLocationController(StorageLocationService storageLocationService) {
        this.storageLocationService = storageLocationService;
    }
    @PostMapping()
    @Transactional
    public ResponseEntity<StorageLocationDetailDTO> createStorageLocation(@RequestBody @Valid StorageLocationCreateDTO dto) {
        StorageLocation storageLocation = storageLocationService.saveStorageLocation(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(storageLocation.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new StorageLocationDetailDTO(storageLocation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageLocationDetailDTO> detailStorageLocation(@PathVariable Long id) {
        StorageLocation storageLocation = storageLocationService.findStorageLocationById(id);
        return ResponseEntity.ok(new StorageLocationDetailDTO(storageLocation));
    }

    @GetMapping
    public ResponseEntity<Page<InventoryDetailDTO>> listAllInventories(@PageableDefault (size = 20, sort = "movementDate") Pageable pageable) {
        Page<StorageLocation> storageLocations = storageLocationService.findAllStorageLocations(pageable);
        return ResponseEntity.ok(new StorageLocationDetailDTO(storageLocation));

    }



}
