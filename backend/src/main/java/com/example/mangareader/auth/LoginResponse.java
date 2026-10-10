package com.example.mangareader.auth;

import java.util.UUID;

public record LoginResponse(UUID userId, String email, String displayName, String role, String token) {
}
