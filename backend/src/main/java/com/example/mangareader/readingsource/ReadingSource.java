package com.example.mangareader.readingsource;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Sumber bacaan untuk sebuah manga. MVP: tautan ke penyedia resmi/terverifikasi.
 * Tidak ada scraping/proxy konten. is_active=false menyembunyikan sumber dari publik.
 */
@Entity
@Table(name = "reading_sources")
public class ReadingSource {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "external_manga_id", nullable = false)
    private Long externalMangaId;

    @Column(name = "source_name", nullable = false, length = 150)
    private String sourceName;

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "license_reference")
    private String licenseReference;

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Long getExternalMangaId() { return externalMangaId; }
    public void setExternalMangaId(Long externalMangaId) { this.externalMangaId = externalMangaId; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getLicenseReference() { return licenseReference; }
    public void setLicenseReference(String licenseReference) { this.licenseReference = licenseReference; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
