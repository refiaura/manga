package com.example.mangareader.readingsource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ReadingSourceRequests {

    public record CreateRequest(
            @Size(max = 30) String provider,
            @NotNull @Positive Long externalMangaId,
            @NotBlank @Size(max = 150) String sourceName,
            @NotBlank @Size(max = 2000) String sourceUrl,
            @NotNull SourceType sourceType,
            VerificationStatus verificationStatus,
            @Size(max = 2000) String licenseReference,
            Boolean active
    ) {
        public String providerOrDefault() {
            return (provider == null || provider.isBlank()) ? "JIKAN" : provider.trim().toUpperCase();
        }
    }
}
