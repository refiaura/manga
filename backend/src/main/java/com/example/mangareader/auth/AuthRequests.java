package com.example.mangareader.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequests {

    public record RegisterRequest(
            @NotBlank(message = "email wajib diisi")
            @Email(message = "format email tidak valid")
            @Size(max = 254)
            String email,

            @NotBlank(message = "password wajib diisi")
            @Size(min = 8, max = 100, message = "password minimal 8 karakter")
            String password,

            @Size(max = 100, message = "displayName maksimal 100 karakter")
            String displayName
    ) {}

    public record LoginRequest(
            @NotBlank(message = "email wajib diisi")
            @Email(message = "format email tidak valid")
            String email,

            @NotBlank(message = "password wajib diisi")
            String password
    ) {}
}
