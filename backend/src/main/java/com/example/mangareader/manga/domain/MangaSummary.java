package com.example.mangareader.manga.domain;

import java.util.List;

/** DTO internal katalog; tidak pernah mengekspos JSON Jikan mentah ke frontend. */
public record MangaSummary(
        String provider,
        long externalId,
        String title,
        String alternativeTitle,
        String coverUrl,
        String type,
        String publishStatus,
        Double score,           // null jika belum ada skor
        Integer scoreCount,
        Integer rank,
        Integer popularity,
        Integer chapters,       // jumlah dari metadata; BUKAN daftar bab
        Integer volumes,
        List<String> genres
) {}
