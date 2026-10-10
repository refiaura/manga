package com.example.mangareader.library;

import java.time.Instant;
import java.util.UUID;

/** DTO response entri perpustakaan. */
public record LibraryItemDto(UUID id, String provider, long externalMangaId, ReadingStatus status,
                             Instant addedAt, Instant updatedAt) {
    public static LibraryItemDto from(UserLibrary e) {
        return new LibraryItemDto(e.getId(), e.getProvider(), e.getExternalMangaId(),
                e.getReadingStatus(), e.getAddedAt(), e.getUpdatedAt());
    }
}
