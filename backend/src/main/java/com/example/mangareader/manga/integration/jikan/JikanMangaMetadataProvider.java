package com.example.mangareader.manga.integration.jikan;

import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.manga.domain.GenreRef;
import com.example.mangareader.manga.domain.MangaDetail;
import com.example.mangareader.manga.domain.MangaMetadataProvider;
import com.example.mangareader.manga.domain.MangaPage;
import com.example.mangareader.manga.domain.MangaQuery;
import com.example.mangareader.manga.domain.MangaSummary;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Implementasi provider metadata menggunakan Jikan API v4.
 * Alur: frontend → backend (service ini + cache) → Jikan. Frontend tidak memanggil Jikan langsung.
 */
@Service
public class JikanMangaMetadataProvider implements MangaMetadataProvider {

    public static final String PROVIDER = "JIKAN";

    private final RestClient restClient;
    private final JikanRateLimiter rateLimiter;

    public JikanMangaMetadataProvider(RestClient jikanRestClient, JikanRateLimiter rateLimiter) {
        this.restClient = jikanRestClient;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String providerKey() {
        return PROVIDER;
    }

    @Override
    @Cacheable(cacheNames = "mangaCatalog", key = "#query.toString()")
    public MangaPage searchManga(MangaQuery query) {
        JikanDto.PageResponse response = rateLimiter.execute("searchManga", () ->
                restClient.get()
                        .uri(uri -> {
                            uriBuilderWithCommonParams(uri, query.page(), query.limit());
                            if (query.text() != null && !query.text().isBlank()) uri.queryParam("q", query.text());
                            if (query.genreId() != null) uri.queryParam("genres", query.genreId());
                            if (query.type() != null) uri.queryParam("type", query.type());
                            if (query.status() != null) uri.queryParam("status", query.status());
                            if (query.orderBy() != null) {
                                uri.queryParam("order_by", query.orderBy());
                                uri.queryParam("sort", query.sort() == null ? "desc" : query.sort());
                            }
                            return uri.build();
                        })
                        .retrieve()
                        .body(JikanDto.PageResponse.class));

        List<MangaSummary> items = JikanMapper.itemsOf(response).stream()
                .map(this::toSummary)
                .filter(java.util.Objects::nonNull)
                .toList();

        JikanDto.Pagination p = response == null ? null : response.pagination();
        boolean hasNext = p != null && Boolean.TRUE.equals(p.has_next_page());
        int currentPage = p != null && p.page() != null ? p.page() : query.page();
        int perPage = p != null && p.per_page() != null ? p.per_page() : query.limit();
        Long total = p != null ? p.items_count() : null;
        Integer totalPages = p != null ? p.total_pages() : null;
        return new MangaPage(items, currentPage, perPage, hasNext, total, totalPages);
    }

    /** GET /top/manga — parameter filter top tidak didukung upstream, hanya page/limit. */
    @Cacheable(cacheNames = "mangaCatalog", key = "'top:' + #page + ':' + #limit")
    public MangaPage topManga(int page, int limit) {
        JikanDto.PageResponse response = rateLimiter.execute("topManga", () ->
                restClient.get()
                        .uri(uri -> uriBuilderWithCommonParams(uri, page, limit).build())
                        .retrieve()
                        .body(JikanDto.PageResponse.class));

        List<MangaSummary> items = JikanMapper.itemsOf(response).stream()
                .map(this::toSummary)
                .filter(java.util.Objects::nonNull)
                .toList();
        JikanDto.Pagination p = response == null ? null : response.pagination();
        return new MangaPage(items,
                p != null && p.page() != null ? p.page() : page,
                p != null && p.per_page() != null ? p.per_page() : limit,
                p != null && Boolean.TRUE.equals(p.has_next_page()),
                p != null ? p.items_count() : null,
                p != null ? p.total_pages() : null);
    }

    @Override
    @Cacheable(cacheNames = "mangaDetail", key = "#externalId")
    public MangaDetail getMangaDetail(long externalId) {
        JikanDto.SingleResponse wrapper = rateLimiter.execute("getMangaDetail:" + externalId, () ->
                restClient.get()
                        .uri("/manga/{id}", externalId)
                        .retrieve()
                        .body(JikanDto.SingleResponse.class));
        JikanDto.MangaItem item = JikanMapper.itemOf(wrapper);
        if (item == null || item.mal_id() == null) {
            throw AppException.notFound("Data manga tidak tersedia pada penyedia metadata");
        }
        return new MangaDetail(
                PROVIDER,
                item.mal_id(),
                item.title() == null ? "(Tanpa judul)" : item.title(),
                JikanMapper.alternativeTitle(item),
                JikanMapper.coverUrl(item),
                blankToNull(item.synopsis()),
                JikanMapper.roleNames(item.authors()),
                JikanMapper.roleNames(item.serializations()),
                toGenreRefs(JikanMapper.genreRefs(item)),
                toGenreRefs(item.themes()),
                toGenreRefs(item.demographics()),
                blankToNull(item.type()),
                blankToNull(item.status()),
                item.chapters(),
                item.volumes(),
                null,
                JikanMapper.normalizeScore(item.score()),
                item.scoredBy(),
                item.rank(),
                item.popularity(),
                JikanMapper.coverUrl(item),
                blankToNull(item.url()),
                List.of());
    }

    @Override
    @Cacheable(cacheNames = "genres", key = "'manga'")
    public List<GenreRef> getGenres() {
        List<JikanDto.NamedRef> refs = rateLimiter.execute("getGenres", () ->
                restClient.get()
                        .uri("/genres/manga")
                        .retrieve()
                        .body(new org.springframework.core.ParameterizedTypeReference<List<JikanDto.NamedRef>>() {}));
        if (refs == null) return List.of();
        return refs.stream()
                .filter(r -> r != null && r.mal_id() != null && r.name() != null)
                .map(r -> new GenreRef(r.mal_id(), r.name()))
                .toList();
    }

    /**
     * Menambahkan parameter pagination ke URI builder.
     * Kontrak Jikan: page (1-based), limit (maksimum 25 per dokumentasi /manga dan /top/manga).
     */
    private org.springframework.web.util.UriBuilder uriBuilderWithCommonParams(
            org.springframework.web.util.UriBuilder uri, int page, int limit) {
        return uri.queryParam("page", page).queryParam("limit", Math.min(limit, 25));
    }

    private MangaSummary toSummary(JikanDto.MangaItem item) {
        if (item == null || item.mal_id() == null) return null;
        return new MangaSummary(
                PROVIDER,
                item.mal_id(),
                item.title() == null ? "(Tanpa judul)" : item.title(),
                JikanMapper.alternativeTitle(item),
                JikanMapper.coverUrl(item),
                blankToNull(item.type()),
                blankToNull(item.status()),
                JikanMapper.normalizeScore(item.score()),
                item.scoredBy(),
                item.rank(),
                item.popularity(),
                item.chapters(),
                item.volumes(),
                JikanMapper.genreNames(item));
    }

    private List<GenreRef> toGenreRefs(List<JikanDto.NamedRef> refs) {
        if (refs == null) return List.of();
        return refs.stream()
                .filter(r -> r != null && r.mal_id() != null && r.name() != null)
                .map(r -> new GenreRef(r.mal_id(), r.name()))
                .toList();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
