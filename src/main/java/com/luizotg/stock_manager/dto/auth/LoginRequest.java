package com.luizotg.stock_manager.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(max = 150)
        String username,
        @NotBlank
        @Size(min = 8, max = 255)
        String password
) {
}
