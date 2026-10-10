package com.example.mangareader.readingprogress;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, UUID> {

    Optional<ReadingProgress> findByUserIdAndProviderAndExternalMangaId(UUID userId, String provider, Long externalMangaId);

    /** Riwayat / lanjutkan membaca: hanya entri yang punya waktu baca. */
    List<ReadingProgress> findByUserIdAndLastReadAtNotNullOrderByLastReadAtDesc(UUID userId, Pageable pageable);
}
