package com.example.mangareader.manga.domain;

/** Query katalog yang sudah divalidasi/dinormalisasi oleh lapisan aplikasi. */
public record MangaQuery(
        String text,
        Integer genreId,
        String type,          // null atau salah satu dari JikanMangaType
        String status,        // null atau salah satu dari JikanMangaStatus
        String orderBy,       // allowlist
        String sort,          // asc|desc
        int page,             // >= 1
        int limit             // 1..MAX_LIMIT
) {
    public static final int MAX_LIMIT = 50;
}
