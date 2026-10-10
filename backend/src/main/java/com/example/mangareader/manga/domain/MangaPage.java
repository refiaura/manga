package com.example.mangareader.manga.domain;

import java.util.List;

public record MangaPage(
        List<MangaSummary> items,
        int page,
        int limit,
        boolean hasNext,
        Long total,
        Integer totalPages
) {}
