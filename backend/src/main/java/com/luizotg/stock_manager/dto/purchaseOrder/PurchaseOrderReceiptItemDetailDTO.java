package com.luizotg.stock_manager.dto.purchaseOrder;

import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.PurchaseOrderReceiptItem;

public record PurchaseOrderReceiptItemDetailDTO(
        Long id,
        Long purchaseOrderItemId,
        Long productId,
        String productName,
        String productSku,
        Integer receivedQuantity
) {
    public PurchaseOrderReceiptItemDetailDTO(PurchaseOrderReceiptItem item) {
        this(
                item.getId(),
                item.getPurchaseOrderItem().getId(),
                getProductId(item.getProduct()),
                getProductName(item.getProduct()),
                getProductSku(item.getProduct()),
                item.getReceivedQuantity()
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
