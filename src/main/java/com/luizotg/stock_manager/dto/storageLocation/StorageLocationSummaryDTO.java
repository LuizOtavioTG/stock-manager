package com.luizotg.stock_manager.dto.storageLocation;

import com.luizotg.stock_manager.model.StorageLocation;

public record StorageLocationSummaryDTO(

        Long id,
        String name,
        String type,
        Boolean defaultLocation

) {
    public StorageLocationSummaryDTO(StorageLocation storageLocation) {
        this(
                storageLocation.getId(),
                storageLocation.getName(),
                storageLocation.getType(),
                storageLocation.getDefaultLocation()
        );
    }
}