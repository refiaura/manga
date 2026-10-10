package com.example.mangareader.manga.domain;

import java.util.List;

/**
 * Abstraksi penyedia metadata manga (PRD: MangaMetadataProvider).
 * Sumber metadata dapat diganti tanpa mengubah logika katalog.
 */
public interface MangaMetadataProvider {

    MangaPage searchManga(MangaQuery query);

    MangaDetail getMangaDetail(long externalId);

    List<GenreRef> getGenres();

    String providerKey(); // contoh: "JIKAN"
}
