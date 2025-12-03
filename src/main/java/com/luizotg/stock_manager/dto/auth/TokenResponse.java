package com.luizotg.stock_manager.dto.auth;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken
) {
    public TokenResponse(String accessToken, String refreshToken) {
        this("Bearer", accessToken, refreshToken);
    }
}
