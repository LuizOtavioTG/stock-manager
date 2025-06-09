package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.storageLocation.StorageLocationCreateDTO;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageLocationService {

    private final StorageLocationRepository storagelocationRepository;
    public StorageLocationService(StorageLocationRepository storagelocationRepository) {
        this.storagelocationRepository = storagelocationRepository;
    }

    public List<StorageLocation> findAllStorageLocations(Pageable pageable) {
        return storagelocationRepository.findAll();
    }

    public StorageLocation saveStorageLocation(StorageLocationCreateDTO entity) {
        StorageLocation storageLocation = new StorageLocation(entity);
        return storagelocationRepository.save(storageLocation);
    }
    public StorageLocation findStorageLocationById(Long id) {
        return storagelocationRepository.findById(id).orElse(null);}

    public void deleteStorageLocationById(Long id) {
        storagelocationRepository.deleteById(id);
    }
}
