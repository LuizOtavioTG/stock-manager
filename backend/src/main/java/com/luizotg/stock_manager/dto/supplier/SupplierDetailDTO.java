package com.luizotg.stock_manager.dto.supplier;

import com.luizotg.stock_manager.model.Supplier;

import java.time.LocalDateTime;

public record SupplierDetailDTO(
        Long id,
        String name,
        String contactName,
        String phoneNumber,
        String email,
        String address,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public SupplierDetailDTO(Supplier supplier) {
        this(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getPhoneNumber(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}
