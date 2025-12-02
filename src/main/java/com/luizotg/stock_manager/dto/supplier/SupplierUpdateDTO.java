package com.luizotg.stock_manager.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierUpdateDTO(

        @NotBlank(message = "{supplier.name.notblank}")
        @Size(max = 255, message = "{supplier.name.size}")
        String name,

        @Size(max = 255, message = "{supplier.contactName.size}")
        String contactName,

        @Size(max = 20, message = "{supplier.phoneNumber.size}")
        String phoneNumber,

        @Email(message = "{supplier.email.valid}")
        @Size(max = 255, message = "{supplier.email.size}")
        String email,

        @Size(max = 500, message = "{supplier.address.size}")
        String address,

        Boolean active

) {
}
