package com.example.mangareader.readingprogress;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Progres membaca terakhir per manga per pengguna.
 * Unique (user_id, provider, external_manga_id). chapter_external_id/label adalah referensi
 * bebas (bukan FK) karena sumber bab berasal dari provider eksternal/guest lokal.
 */
@Entity
@Table(name = "reading_progress")
public class ReadingProgress {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "external_manga_id", nullable = false)
    private Long externalMangaId;

    @Column(name = "reading_source_id")
    private UUID readingSourceId;

    @Column(name = "chapter_external_id", length = 255)
    private String chapterExternalId;

    @Column(name = "chapter_label", length = 100)
    private String chapterLabel;

    @Column(name = "page_index")
    private Integer pageIndex;

    @Column(name = "scroll_position", precision = 6, scale = 4)
    private BigDecimal scrollPosition;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        if (id == null) id = UUID.randomUUID();
        updatedAt = Instant.now();
    }

    // Getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Long getExternalMangaId() { return externalMangaId; }
    public void setExternalMangaId(Long externalMangaId) { this.externalMangaId = externalMangaId; }
    public UUID getReadingSourceId() { return readingSourceId; }
    public void setReadingSourceId(UUID readingSourceId) { this.readingSourceId = readingSourceId; }
    public String getChapterExternalId() { return chapterExternalId; }
    public void setChapterExternalId(String chapterExternalId) { this.chapterExternalId = chapterExternalId; }
    public String getChapterLabel() { return chapterLabel; }
    public void setChapterLabel(String chapterLabel) { this.chapterLabel = chapterLabel; }
    public Integer getPageIndex() { return pageIndex; }
    public void setPageIndex(Integer pageIndex) { this.pageIndex = pageIndex; }
    public BigDecimal getScrollPosition() { return scrollPosition; }
    public void setScrollPosition(BigDecimal scrollPosition) { this.scrollPosition = scrollPosition; }
    public Instant getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Instant lastReadAt) { this.lastReadAt = lastReadAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
