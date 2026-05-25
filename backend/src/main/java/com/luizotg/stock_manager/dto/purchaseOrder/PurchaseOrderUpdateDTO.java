package com.luizotg.stock_manager.dto.purchaseOrder;

import com.luizotg.stock_manager.model.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderUpdateDTO(
        LocalDate expectedDeliveryDate,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres.")
        String notes,

        PurchaseOrderStatus status,

        @NotEmpty(message = "Pedido deve ter pelo menos um item.")
        @Valid
        List<PurchaseOrderItemCreateDTO> items
) {
}
