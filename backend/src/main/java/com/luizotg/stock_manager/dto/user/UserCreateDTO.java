package com.luizotg.stock_manager.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateDTO(
        @NotBlank
        @Size(max = 150)
        String username,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8, max = 255)
        String password,
        @NotEmpty
        List<String> roles
) {
}
