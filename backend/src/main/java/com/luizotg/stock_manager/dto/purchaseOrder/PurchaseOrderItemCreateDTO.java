package com.luizotg.stock_manager.dto.purchaseOrder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PurchaseOrderItemCreateDTO(
        @NotNull(message = "Produto é obrigatório.")
        Long productId,

        @NotNull(message = "Quantidade é obrigatória.")
        @Positive(message = "Quantidade deve ser maior que zero.")
        Integer quantity,

        @NotNull(message = "Custo unitário é obrigatório.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Custo unitário não pode ser negativo.")
        Double unitCost,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres.")
        String notes
) {
}
