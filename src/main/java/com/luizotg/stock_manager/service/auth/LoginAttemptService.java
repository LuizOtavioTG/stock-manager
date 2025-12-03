package com.luizotg.stock_manager.service.auth;

import com.luizotg.stock_manager.config.AuthProperties;
import com.luizotg.stock_manager.model.User;
import com.luizotg.stock_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AuthProperties authProperties;
    private final Clock clock;

    @Transactional
    public void onSuccess(String login) {
        findByLogin(login).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        });
    }

    @Transactional
    public void onFailure(String login) {
        findByLogin(login).ifPresent(user -> {
            int attempts = Optional.ofNullable(user.getFailedAttempts()).orElse(0) + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= authProperties.getMaxFailedAttempts()) {
                LocalDateTime lockUntil = LocalDateTime.now(clock).plus(authProperties.getLockDuration());
                user.setLockedUntil(lockUntil);
                user.setFailedAttempts(0);
            }
            userRepository.save(user);
        });
    }

    public boolean isLocked(User user) {
        LocalDateTime lockedUntil = user.getLockedUntil();
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now(clock));
    }

    private Optional<User> findByLogin(String login) {
        return userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login));
    }
}
