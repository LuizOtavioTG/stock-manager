package com.luizotg.stock_manager.dto.user;

import com.luizotg.stock_manager.model.Role;
import com.luizotg.stock_manager.model.User;

import java.util.List;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Boolean active,
        List<String> roles
) {
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getActive(),
                user.getRoles().stream().map(Role::getName).toList()
        );
    }
}
