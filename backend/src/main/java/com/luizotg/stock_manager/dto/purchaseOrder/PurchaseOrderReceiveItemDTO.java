package com.luizotg.stock_manager.dto.purchaseOrder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseOrderReceiveItemDTO(
        @NotNull(message = "Item do pedido é obrigatório.")
        Long purchaseOrderItemId,

        @NotNull(message = "Quantidade recebida é obrigatória.")
        @Positive(message = "Quantidade recebida deve ser maior que zero.")
        Integer receivedQuantity
) {
}
