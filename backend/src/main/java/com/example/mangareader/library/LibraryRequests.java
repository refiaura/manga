package com.example.mangareader.library;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Request menambah/mengubah status perpustakaan. Provider opsional (default JIKAN). */
public class LibraryRequests {

    public record UpsertRequest(
            @Size(max = 30, message = "provider maksimal 30 karakter")
            String provider,

            @NotNull(message = "externalMangaId wajib diisi")
            @Positive(message = "externalMangaId harus positif")
            Long externalMangaId,

            @NotNull(message = "status wajib diisi")
            ReadingStatus status
    ) {
        public String providerOrDefault() {
            return (provider == null || provider.isBlank()) ? "JIKAN" : provider.trim().toUpperCase();
        }
    }

    /** Request PATCH /library/{id} — ubah status membaca saja. */
    public record StatusRequest(
            @NotNull(message = "status wajib diisi")
            ReadingStatus status
    ) {}
}
