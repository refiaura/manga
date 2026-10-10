package com.example.mangareader.manga.api;

import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.common.response.ApiResponse;
import com.example.mangareader.manga.domain.GenreRef;
import com.example.mangareader.manga.domain.MangaMetadataProvider;
import com.example.mangareader.manga.domain.MangaPage;
import com.example.mangareader.manga.domain.MangaQuery;
import com.example.mangareader.manga.integration.jikan.JikanMangaMetadataProvider;
import com.example.mangareader.manga.integration.jikan.JikanQueryValidator;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontrak API katalog publik (PRD 8.2):
 * GET /api/v1/manga, /api/v1/manga/top, /api/v1/manga/{id}, /api/v1/genres.
 * Semua request frontend lewat backend ini — tidak ada pemanggilan Jikan langsung dari browser.
 */
@RestController
@RequestMapping("/api/v1")
public class MangaCatalogController {

    private static final int DEFAULT_LIMIT = 12;
    private static final int MAX_LIMIT = MangaQuery.MAX_LIMIT;

    private final MangaMetadataProvider provider;
    private final JikanMangaMetadataProvider jikanProvider;

    public MangaCatalogController(MangaMetadataProvider provider, JikanMangaMetadataProvider jikanProvider) {
        this.provider = provider;
        this.jikanProvider = jikanProvider;
    }

    @GetMapping("/manga")
    public ApiResponse<List<MangaSummaryView>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer genres,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {

        if (page < 1) throw AppException.validation("page minimal 1");
        if (limit < 1 || limit > MAX_LIMIT) throw AppException.validation("limit harus antara 1 dan " + MAX_LIMIT);
        try {
            JikanQueryValidator.validate(lower(type), lower(status), lower(orderBy), lower(sort));
        } catch (IllegalArgumentException ex) {
            throw AppException.validation(ex.getMessage());
        }
        if (genres != null && genres <= 0) throw AppException.validation("genres tidak valid");

        MangaQuery query = new MangaQuery(trimToNull(q), genres, lower(type), lower(status),
                lower(orderBy), lower(sort), page, Math.min(limit, 25));
        MangaPage result = provider.searchManga(query);
        return ApiResponse.ok(MangaSummaryView.fromList(result.items()),
                new ApiResponse.PaginationMeta(result.page(), result.limit(), result.hasNext(), result.total()));
    }

    /** Katalog default beranda: top manga (endpoint /top/manga Jikan). */
    @GetMapping("/manga/top")
    public ApiResponse<List<MangaSummaryView>> top(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
        if (page < 1) throw AppException.validation("page minimal 1");
        if (limit < 1 || limit > MAX_LIMIT) throw AppException.validation("limit harus antara 1 dan " + MAX_LIMIT);
        MangaPage result = jikanProvider.topManga(page, Math.min(limit, 25));
        return ApiResponse.ok(MangaSummaryView.fromList(result.items()),
                new ApiResponse.PaginationMeta(result.page(), result.limit(), result.hasNext(), result.total()));
    }

    @GetMapping("/manga/{id}")
    public ApiResponse<MangaDetailView> detail(@PathVariable long id) {
        if (id <= 0) throw AppException.validation("id manga tidak valid");
        return ApiResponse.ok(MangaDetailView.from(provider.getMangaDetail(id)));
    }

    @GetMapping("/genres")
    public ApiResponse<List<GenreRef>> genres() {
        return ApiResponse.ok(provider.getGenres());
    }

    private static String lower(String s) {
        return s == null || s.isBlank() ? null : s.trim().toLowerCase();
    }

    private static String trimToNull(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.length() > 200) throw AppException.validation("q terlalu panjang (maks 200 karakter)");
        return t;
    }
}
