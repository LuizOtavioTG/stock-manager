package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.user.UserCreateDTO;
import com.luizotg.stock_manager.dto.user.UserResponseDTO;
import com.luizotg.stock_manager.dto.user.UserUpdateDTO;
import com.luizotg.stock_manager.model.Role;
import com.luizotg.stock_manager.model.User;
import com.luizotg.stock_manager.repository.RoleRepository;
import com.luizotg.stock_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponseDTO> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponseDTO::new);
    }

    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(resolveRoles(dto.roles()));
        user.setActive(true);
        userRepository.save(user);
        return new UserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.email() != null) {
            user.setEmail(dto.email());
        }
        if (dto.password() != null) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        if (dto.active() != null) {
            user.setActive(dto.active());
        }
        if (dto.roles() != null) {
            user.setRoles(resolveRoles(dto.roles()));
        }

        userRepository.save(user);
        return new UserResponseDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
    }

    private Set<Role> resolveRoles(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one role is required");
        }
        Set<String> normalized = roleNames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(java.util.stream.Collectors.toSet());
        List<Role> roles = roleRepository.findByNameIn(normalized);
        if (roles.size() != normalized.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more roles are invalid");
        }
        return new HashSet<>(roles);
    }
}
