package com.example.mangareader.readingprogress;

import com.example.mangareader.common.exception.AppException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Penyimpanan progres membaca dengan aturan merge "last-write-wins berdasarkan lastReadAt":
 * data lama tidak pernah menimpa progres yang lebih baru.
 */
@Service
public class ReadingProgressService {

    public static final int MAX_HISTORY = 50;

    private final ReadingProgressRepository repository;

    public ReadingProgressService(ReadingProgressRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReadingProgressDto save(UUID userId, ReadingProgressRequests.SaveRequest req) {
        validate(req);
        String provider = req.providerOrDefault();
        ReadingProgress entity = repository
                .findByUserIdAndProviderAndExternalMangaId(userId, provider, req.externalMangaId())
                .orElseGet(() -> {
                    ReadingProgress p = new ReadingProgress();
                    p.setUserId(userId);
                    p.setProvider(provider);
                    p.setExternalMangaId(req.externalMangaId());
                    return p;
                });

        Instant incoming = req.lastReadAt() == null ? Instant.now() : req.lastReadAt();
        boolean isNewer = entity.getLastReadAt() == null || !incoming.isBefore(entity.getLastReadAt());
        if (!isNewer) {
            // Data lama menolak menimpa progres lebih baru (merge rule terdokumentasi).
            return ReadingProgressDto.from(entity);
        }

        entity.setChapterExternalId(trimToNull(req.chapterExternalId(), 255));
        entity.setChapterLabel(trimToNull(req.chapterLabel(), 100));
        entity.setPageIndex(req.pageIndex());
        entity.setScrollPosition(req.scrollPosition());
        entity.setLastReadAt(incoming);
        return ReadingProgressDto.from(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ReadingProgressDto get(UUID userId, String provider, long externalMangaId) {
        return repository.findByUserIdAndProviderAndExternalMangaId(userId, provider.toUpperCase(), externalMangaId)
                .map(ReadingProgressDto::from)
                .orElseThrow(() -> AppException.notFound("Progres membaca belum tersimpan untuk manga ini"));
    }

    @Transactional(readOnly = true)
    public List<ReadingProgressDto> history(UUID userId, int limit) {
        int capped = Math.min(Math.max(limit, 1), MAX_HISTORY);
        return repository.findByUserIdAndLastReadAtNotNullOrderByLastReadAtDesc(userId, PageRequest.of(0, capped))
                .stream().map(ReadingProgressDto::from).toList();
    }

    void validate(ReadingProgressRequests.SaveRequest req) {
        if (req.pageIndex() != null && req.pageIndex() < 0) {
            throw AppException.validation("pageIndex tidak boleh negatif");
        }
        BigDecimal scroll = req.scrollPosition();
        if (scroll != null && (scroll.signum() < 0 || scroll.compareTo(BigDecimal.ONE) > 0)) {
            throw AppException.validation("scrollPosition harus berada di rentang 0..1");
        }
        if (req.lastReadAt() != null && req.lastReadAt().isAfter(Instant.now().plusSeconds(300))) {
            throw AppException.validation("lastReadAt tidak boleh di masa depan");
        }
    }

    static String trimToNull(String s, int max) {
        if (s == null || s.isBlank()) return null;
        String trimmed = s.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
