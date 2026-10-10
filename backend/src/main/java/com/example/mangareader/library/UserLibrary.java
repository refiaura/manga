package com.example.mangareader.library;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entri perpustakaan pengguna untuk manga tertentu.
 * Unique constraint: (user_id, provider, external_manga_id) - mencegah duplikasi.
 */
@Entity
@Table(name = "user_library")
public class UserLibrary {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "external_manga_id", nullable = false)
    private Long externalMangaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_status", nullable = false, length = 30)
    private ReadingStatus readingStatus;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        if (addedAt == null) addedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Long getExternalMangaId() { return externalMangaId; }
    public void setExternalMangaId(Long externalMangaId) { this.externalMangaId = externalMangaId; }
    public ReadingStatus getReadingStatus() { return readingStatus; }
    public void setReadingStatus(ReadingStatus readingStatus) { this.readingStatus = readingStatus; }
    public Instant getAddedAt() { return addedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
