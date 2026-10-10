package com.example.mangareader.manga.api;

import com.example.mangareader.manga.domain.GenreRef;
import com.example.mangareader.manga.domain.MangaDetail;

import java.util.List;

public record MangaDetailView(
        String provider,
        long externalId,
        String title,
        String alternativeTitle,
        String coverUrl,
        String synopsis,
        List<String> authors,
        List<String> serializations,
        List<GenreRef> genres,
        List<GenreRef> themes,
        List<GenreRef> demographics,
        String type,
        String publishStatus,
        Integer chapters,
        Integer volumes,
        Double score,
        Integer scoreCount,
        Integer rank,
        Integer popularity,
        String imageUrl,
        String externalUrl
) {
    public static MangaDetailView from(MangaDetail d) {
        return new MangaDetailView(d.provider(), d.externalId(), d.title(), d.alternativeTitle(),
                d.coverUrl(), d.synopsis(), d.authors(), d.serializations(), d.genres(),
                d.themes(), d.demographics(), d.type(), d.publishStatus(), d.chapters(),
                d.volumes(), d.score(), d.scoreCount(), d.rank(), d.popularity(),
                d.imageUrl(), d.externalUrl());
    }
}
