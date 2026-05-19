package com.luizotg.stock_manager.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserUpdateDTO(
        @Email
        String email,
        @Size(min = 8, max = 255)
        String password,
        Boolean active,
        List<String> roles
) {
}
