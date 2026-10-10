package com.example.mangareader.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes, boolean allowInsecureDevSecret) {
    public JwtProperties {
        if (secret == null) secret = "";
        if (expirationMinutes <= 0) expirationMinutes = 720;
    }
}
