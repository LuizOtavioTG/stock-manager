package com.luizotg.stock_manager.dto.purchaseOrder;

import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.PurchaseOrderItem;

public record PurchaseOrderItemDetailDTO(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        Integer receivedQuantity,
        Integer pendingQuantity,
        Double unitCost,
        Double estimatedSubtotal,
        String notes
) {
    public PurchaseOrderItemDetailDTO(PurchaseOrderItem item) {
        this(
                item.getId(),
                getProductId(item.getProduct()),
                getProductName(item.getProduct()),
                getProductSku(item.getProduct()),
                item.getQuantity(),
                item.getReceivedQuantity(),
                item.getPendingQuantity(),
                item.getUnitCost(),
                item.getEstimatedSubtotal(),
                item.getNotes()
        );
    }

    private static Long getProductId(Product product) {
        return product != null ? product.getId() : null;
    }

    private static String getProductName(Product product) {
        return product != null ? product.getName() : null;
    }

    private static String getProductSku(Product product) {
        return product != null ? product.getSku() : null;
    }
}
