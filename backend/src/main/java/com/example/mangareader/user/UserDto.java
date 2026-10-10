package com.example.mangareader.user;

import java.time.Instant;
import java.util.UUID;

public record UserDto(UUID id, String email, String displayName, String role, Instant createdAt) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole(), u.getCreatedAt());
    }
}
