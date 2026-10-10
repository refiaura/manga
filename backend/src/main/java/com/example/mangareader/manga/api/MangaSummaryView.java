package com.example.mangareader.manga.api;

import java.util.List;

/** View katalog untuk response API (memisahkan kontrak publik dari domain internal). */
public record MangaSummaryView(
        String provider,
        long externalId,
        String title,
        String alternativeTitle,
        String coverUrl,
        String type,
        String publishStatus,
        Double score,
        Integer scoreCount,
        Integer rank,
        Integer popularity,
        Integer chapters,
        Integer volumes,
        List<String> genres
) {
    public static MangaSummaryView from(com.example.mangareader.manga.domain.MangaSummary s) {
        return new MangaSummaryView(s.provider(), s.externalId(), s.title(), s.alternativeTitle(),
                s.coverUrl(), s.type(), s.publishStatus(), s.score(), s.scoreCount(), s.rank(),
                s.popularity(), s.chapters(), s.volumes(), s.genres());
    }

    public static List<MangaSummaryView> fromList(List<com.example.mangareader.manga.domain.MangaSummary> items) {
        return items.stream().map(MangaSummaryView::from).toList();
    }
}
