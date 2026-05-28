package com.luizotg.stock_manager.dto.purchaseOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PurchaseOrderReceiptReverseDTO(
        @NotBlank(message = "Motivo do estorno é obrigatório.")
        @Size(max = 500, message = "Motivo do estorno deve ter no máximo 500 caracteres.")
        String reason
) {
}
