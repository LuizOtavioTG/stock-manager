package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.storageLocation.StorageLocationCreateDTO;
import com.luizotg.stock_manager.dto.storageLocation.StorageLocationUpdateDTO;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationService {

    private final StorageLocationRepository storageLocationRepository;

    public StorageLocationService(StorageLocationRepository storageLocationRepository) {
        this.storageLocationRepository = storageLocationRepository;
    }

    public Page<StorageLocation> findAllStorageLocations(Pageable pageable) {
        return storageLocationRepository.findAll(pageable);
    }

    public StorageLocation saveStorageLocation(StorageLocationCreateDTO entity) {
        StorageLocation storageLocation = new StorageLocation(entity);
        return storageLocationRepository.save(storageLocation);
    }

    public StorageLocation findStorageLocationById(Long id) {
        return storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Local de armazenamento com ID " + id + " não encontrado."
                ));
    }

    public void deleteStorageLocationById(Long id) {
        StorageLocation storageLocation = findStorageLocationById(id);
        storageLocationRepository.delete(storageLocation);
    }

    public StorageLocation updateStorageLocation(Long id, StorageLocationUpdateDTO entity) {
        StorageLocation existing = findStorageLocationById(id);
        existing.updateFromDTO(entity);
        return storageLocationRepository.save(existing);
    }
}
