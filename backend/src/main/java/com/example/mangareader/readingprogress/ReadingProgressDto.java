package com.example.mangareader.readingprogress;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReadingProgressDto(UUID id, String provider, long externalMangaId, UUID readingSourceId,
                                 String chapterExternalId, String chapterLabel, Integer pageIndex,
                                 BigDecimal scrollPosition, Instant lastReadAt, Instant updatedAt) {
    public static ReadingProgressDto from(ReadingProgress p) {
        return new ReadingProgressDto(p.getId(), p.getProvider(), p.getExternalMangaId(),
                p.getReadingSourceId(), p.getChapterExternalId(), p.getChapterLabel(),
                p.getPageIndex(), p.getScrollPosition(), p.getLastReadAt(), p.getUpdatedAt());
    }
}
