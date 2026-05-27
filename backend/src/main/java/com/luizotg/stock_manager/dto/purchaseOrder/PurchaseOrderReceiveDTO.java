package com.luizotg.stock_manager.dto.purchaseOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PurchaseOrderReceiveDTO(
        @NotNull(message = "Local de estoque é obrigatório.")
        Long storageLocationId,

        @NotEmpty(message = "Recebimento deve ter pelo menos um item.")
        @Valid
        List<PurchaseOrderReceiveItemDTO> items,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres.")
        String notes
) {
}
