package com.example.mangareader.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Model token: JWT bearer stateless.
 * - Sub = user id (UUID), claim "role" dan "email".
 * - Token tidak pernah dicatat ke log.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtTokenProvider(JwtProperties props) {
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            // Jangan pakai default lemah di production; fail fast kecuali profile test/local.
            if (props.allowInsecureDevSecret()) {
                // tambahkan padding deterministik khusus dev/test
                StringBuilder sb = new StringBuilder(props.secret());
                while (sb.length() < 32) sb.append("-dev-secret-padding-");
                secretBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                throw new IllegalStateException("APP_JWT_SECRET harus minimal 32 karakter. Set env APP_JWT_SECRET.");
            }
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMinutes = props.expirationMinutes();
    }

    public String generate(UUID userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
