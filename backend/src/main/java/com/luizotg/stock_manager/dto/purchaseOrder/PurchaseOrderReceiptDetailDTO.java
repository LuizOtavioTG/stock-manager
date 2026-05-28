package com.luizotg.stock_manager.dto.purchaseOrder;

import com.luizotg.stock_manager.model.PurchaseOrderReceipt;
import com.luizotg.stock_manager.model.StorageLocation;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderReceiptDetailDTO(
        Long id,
        Long purchaseOrderId,
        Long storageLocationId,
        String storageLocationName,
        LocalDateTime receiptDate,
        String notes,
        List<PurchaseOrderReceiptItemDetailDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public PurchaseOrderReceiptDetailDTO(PurchaseOrderReceipt receipt) {
        this(
                receipt.getId(),
                receipt.getPurchaseOrder().getId(),
                getStorageLocationId(receipt.getStorageLocation()),
                getStorageLocationName(receipt.getStorageLocation()),
                receipt.getReceiptDate(),
                receipt.getNotes(),
                receipt.getItems().stream().map(PurchaseOrderReceiptItemDetailDTO::new).toList(),
                receipt.getCreatedAt(),
                receipt.getUpdatedAt()
        );
    }

    private static Long getStorageLocationId(StorageLocation storageLocation) {
        return storageLocation != null ? storageLocation.getId() : null;
    }

    private static String getStorageLocationName(StorageLocation storageLocation) {
        return storageLocation != null ? storageLocation.getName() : null;
    }
}
