package com.example.mangareader.readingsource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReadingSourceRepository extends JpaRepository<ReadingSource, UUID> {

    List<ReadingSource> findByProviderAndExternalMangaIdAndActiveTrue(String provider, Long externalMangaId);

    List<ReadingSource> findByProviderAndExternalMangaId(String provider, Long externalMangaId);
}
