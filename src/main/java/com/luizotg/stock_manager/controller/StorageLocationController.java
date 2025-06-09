package com.luizotg.stock_manager.controller;


import com.luizotg.stock_manager.dto.storageLocation.StorageLocationCreateDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationDetailDTO;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.service.StorageLocationService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    }
}
