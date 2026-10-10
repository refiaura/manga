package com.example.mangareader.readingprogress;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/** Request menyimpan progres membaca. */
public class ReadingProgressRequests {

    public record SaveRequest(
            @Size(max = 30) String provider,

            @NotNull(message = "externalMangaId wajib diisi")
            @Positive(message = "externalMangaId harus positif")
            Long externalMangaId,

            @Size(max = 255, message = "chapterExternalId maksimal 255 karakter")
            String chapterExternalId,

            @Size(max = 100, message = "chapterLabel maksimal 100 karakter")
            String chapterLabel,

            Integer pageIndex,

            BigDecimal scrollPosition,

            Instant lastReadAt
    ) {
        public String providerOrDefault() {
            return (provider == null || provider.isBlank()) ? "JIKAN" : provider.trim().toUpperCase();
        }
    }
}
