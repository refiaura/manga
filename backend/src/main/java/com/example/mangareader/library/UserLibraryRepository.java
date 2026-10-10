package com.example.mangareader.library;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, UUID> {

    Optional<UserLibrary> findByUserIdAndProviderAndExternalMangaId(UUID userId, String provider, Long externalMangaId);

    List<UserLibrary> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    List<UserLibrary> findByUserIdAndReadingStatusOrderByUpdatedAtDesc(UUID userId, ReadingStatus status, Pageable pageable);

    long countByUserIdAndReadingStatus(UUID userId, ReadingStatus status);

    boolean existsByUserIdAndProviderAndExternalMangaId(UUID userId, String provider, Long externalMangaId);
}
