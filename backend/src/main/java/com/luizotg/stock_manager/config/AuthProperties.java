package com.luizotg.stock_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.auth")
public class AuthProperties {
    private int maxFailedAttempts = 5;
    private Duration lockDuration = Duration.ofMinutes(15);
}
