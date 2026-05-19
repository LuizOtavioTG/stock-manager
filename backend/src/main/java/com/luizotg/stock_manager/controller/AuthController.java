package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.auth.LoginRequest;
import com.luizotg.stock_manager.dto.auth.RefreshTokenRequest;
import com.luizotg.stock_manager.dto.auth.TokenResponse;
import com.luizotg.stock_manager.service.auth.JwtService;
import com.luizotg.stock_manager.service.auth.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            loginAttemptService.onSuccess(request.username());

            UserDetails user = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("login.success user={} ip={} agent={}", user.getUsername(),
                    httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"));

            return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
        } catch (BadCredentialsException e) {
            loginAttemptService.onFailure(request.username());
            log.warn("login.failed user={} ip={} agent={} reason=bad_credentials", request.username(),
                    httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"));
            throw e;
        } catch (LockedException e) {
            log.warn("login.locked user={} ip={} agent={}", request.username(),
                    httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"));
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isRefreshToken(token)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }

        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccess = jwtService.generateAccessToken(userDetails);
        String newRefresh = jwtService.generateRefreshToken(userDetails);
        return ResponseEntity.ok(new TokenResponse(newAccess, newRefresh));
    }
}
