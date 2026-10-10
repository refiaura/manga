package com.example.mangareader.manga.domain;

import java.util.List;

public record MangaDetail(
        String provider,
        long externalId,
        String title,
        String alternativeTitle,
        String coverUrl,
        String synopsis,          // nullable
        List<String> authors,     // bisa kosong
        List<String> serializations,
        List<GenreRef> genres,
        List<GenreRef> themes,
        List<GenreRef> demographics,
        String type,
        String publishStatus,
        Integer chapters,         // jumlah metadata; bukan daftar bab yang bisa dibaca
        Integer volumes,
        Boolean status,           // publishing/finished dari upstream (nullable)
        Double score,
        Integer scoreCount,
        Integer rank,
        Integer popularity,
        String imageUrl,
        String externalUrl,       // halaman MAL terkait
        List<String> relatedTitles
) {}
