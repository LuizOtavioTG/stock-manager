package com.luizotg.stock_manager.dto.purchaseOrder;

import com.luizotg.stock_manager.model.PurchaseOrder;
import com.luizotg.stock_manager.model.PurchaseOrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderDetailDTO(
        Long id,
        Long supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        String notes,
        Double totalEstimatedCost,
        List<PurchaseOrderItemDetailDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public PurchaseOrderDetailDTO(PurchaseOrder order) {
        this(
                order.getId(),
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getStatus(),
                order.getOrderDate(),
                order.getExpectedDeliveryDate(),
                order.getNotes(),
                order.getTotalEstimatedCost(),
                order.getItems().stream().map(PurchaseOrderItemDetailDTO::new).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
