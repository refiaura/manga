package com.example.mangareader.readingsource;

import java.util.UUID;

/**
 * Response publik sumber bacaan. License reference internal tidak diekspos ke publik.
 */
public record ReadingSourceDto(UUID id, String provider, long externalMangaId, String sourceName,
                               String sourceUrl, SourceType sourceType,
                               VerificationStatus verificationStatus, boolean active) {
    public static ReadingSourceDto from(ReadingSource s) {
        return new ReadingSourceDto(s.getId(), s.getProvider(), s.getExternalMangaId(),
                s.getSourceName(), s.getSourceUrl(), s.getSourceType(),
                s.getVerificationStatus(), s.isActive());
    }
}
