package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.repository.StorageLocationRepository;
import com.luizotg.stock_manager.service.StorageLocationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage-location")
public class StorageLocationController {

    private final StorageLocationService storageLocationService;
    private StorageLocationController(StorageLocationService storageLocationService) {
        this.storageLocationService = storageLocationService;
    }
}
