package com.luizotg.stock_manager.security;

import com.luizotg.stock_manager.model.User;
import com.luizotg.stock_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now(clock);
        refreshLockIfExpired(user, now);
        return new UserPrincipal(user, now);
    }

    private Optional<User> findByUsernameOrEmail(String login) {
        return userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login));
    }

    private void refreshLockIfExpired(User user, LocalDateTime now) {
        if (user.getLockedUntil() != null && !user.getLockedUntil().isAfter(now)) {
            user.setLockedUntil(null);
            user.setFailedAttempts(0);
            userRepository.save(user);
        }
    }
}
