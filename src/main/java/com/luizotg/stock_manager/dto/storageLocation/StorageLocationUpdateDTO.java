package com.luizotg.stock_manager.dto.storageLocation;

import jakarta.validation.constraints.*;

public record StorageLocationUpdateDTO(

        @NotBlank(message = "{storageLocation.name.notblank}")
        @Size(max = 100, message = "{storageLocation.name.size}")
        String name,

        @NotBlank(message = "{storageLocation.type.notblank}")
        @Size(max = 50, message = "{storageLocation.type.size}")
        String type,

        @Size(max = 255, message = "{storageLocation.address.size}")
        String address,

        @Size(max = 20, message = "{storageLocation.phoneNumber.size}")
        String phoneNumber,

        @Email(message = "{storageLocation.email.valid}")
        @Size(max = 100, message = "{storageLocation.email.size}")
        String email,

        @Size(max = 100, message = "{storageLocation.responsibleName.size}")
        String responsibleName,

        @Size(max = 500, message = "{storageLocation.notes.size}")
        String notes,

        @PositiveOrZero(message = "{storageLocation.capacity.positiveOrZero}")
        Integer capacity,

        @NotNull(message = "{storageLocation.defaultLocation.notnull}")
        Boolean defaultLocation,

        @DecimalMin(value = "-90.0", message = "{storageLocation.latitude.range}")
        @DecimalMax(value = "90.0", message = "{storageLocation.latitude.range}")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "{storageLocation.longitude.range}")
        @DecimalMax(value = "180.0", message = "{storageLocation.longitude.range}")
        Double longitude

) {}
