package com.luizotg.stock_manager.dto.supplier;

import com.luizotg.stock_manager.model.Supplier;

public record SupplierSummaryDTO(
        Long id,
        String name,
        Boolean active
) {
    public SupplierSummaryDTO(Supplier supplier) {
        this(
                supplier.getId(),
                supplier.getName(),
                supplier.getActive()
        );
    }
}
