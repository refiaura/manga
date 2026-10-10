package com.example.mangareader.auth;

import java.util.UUID;

/** Principal autentikasi tepercaya; identitas user TIDAK diambil dari body request. */
public record AuthenticatedUser(UUID id, String email, String role) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
