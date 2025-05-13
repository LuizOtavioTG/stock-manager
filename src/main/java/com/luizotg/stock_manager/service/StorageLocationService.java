package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageLocationService {

    private final StorageLocationRepository storagelocationRepository;
    public StorageLocationService(StorageLocationRepository storagelocationRepository) {
        this.storagelocationRepository = storagelocationRepository;
    }

    public List<StorageLocation> findAllStorageLocations() {
        return storagelocationRepository.findAll();
    }

    public StorageLocation saveStorageLocation(StorageLocation entity) {
        return storagelocationRepository.save(entity);
    }

    public void deleteStorageLocationById(Long id) {
        storagelocationRepository.deleteById(id);
    }
}
