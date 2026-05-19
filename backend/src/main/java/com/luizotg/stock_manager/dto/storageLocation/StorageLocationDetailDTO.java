package com.luizotg.stock_manager.dto.storageLocation;

import com.luizotg.stock_manager.model.StorageLocation;

import java.time.LocalDateTime;

public record StorageLocationDetailDTO(

        Long id,
        String name,
        String type,
        String address,
        String phoneNumber,
        String email,
        String responsibleName,
        String notes,
        Integer capacity,
        Boolean defaultLocation,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
    public StorageLocationDetailDTO(StorageLocation storageLocation) {
        this(
                storageLocation.getId(),
                storageLocation.getName(),
                storageLocation.getType(),
                storageLocation.getAddress(),
                storageLocation.getPhoneNumber(),
                storageLocation.getEmail(),
                storageLocation.getResponsibleName(),
                storageLocation.getNotes(),
                storageLocation.getCapacity(),
                storageLocation.getDefaultLocation(),
                storageLocation.getLatitude(),
                storageLocation.getLongitude(),
                storageLocation.getCreatedAt(),
                storageLocation.getUpdatedAt()
        );
    }
}
