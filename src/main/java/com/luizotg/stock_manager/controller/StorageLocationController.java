package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.storageLocation.StorageLocationCreateDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationDetailDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationUpdateDTO;
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

    public StorageLocationController(StorageLocationService storageLocationService) {
        this.storageLocationService = storageLocationService;
    }

    @PostMapping
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
    public ResponseEntity<Page<StorageLocationDetailDTO>> listAllStorageLocations(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<StorageLocation> storageLocations = storageLocationService.findAllStorageLocations(pageable);
        Page<StorageLocationDetailDTO> dtoPage = storageLocations.map(StorageLocationDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorageLocation(
            @PathVariable
            Long id
    ){
        storageLocationService.deleteStorageLocationById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<StorageLocationDetailDTO> updateStorageLocation(
            @PathVariable Long id,
            @RequestBody @Valid StorageLocationUpdateDTO dto
    ) {
        StorageLocation storageLocation = storageLocationService.updateStorageLocation(id, dto);
        return ResponseEntity.ok(new StorageLocationDetailDTO(storageLocation));
    }



}
