package com.example.mangareader.readingprogress;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller kompatibilitas kontrak PRD Bagian 8 (nama path alternatif):
 * - GET /api/v1/reading-progress            -> daftar progres pengguna
 * - PUT /api/v1/reading-progress/{provider}/{mangaId} -> upsert progres
 * Endpoint utama tetap /api/v1/progress; kelas ini hanya alias tanpa logika duplikat.
 */
@RestController
@RequestMapping("/api/v1/reading-progress")
public class ReadingProgressAliasController {

    private final ReadingProgressService service;

    public ReadingProgressAliasController(ReadingProgressService service) {
        this.service = service;
    }

    /** GET /reading-progress — seluruh progres pengguna (riwayat). */
    @GetMapping
    public ApiResponse<List<ReadingProgressDto>> list(@AuthenticationPrincipal AuthenticatedUser principal,
                                                      @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(service.history(principal.id(), limit));
    }

    /** GET /reading-progress/{provider}/{externalMangaId} — progres satu manga. */
    @GetMapping("/{provider}/{externalMangaId}")
    public ApiResponse<ReadingProgressDto> get(@AuthenticationPrincipal AuthenticatedUser principal,
                                               @PathVariable String provider,
                                               @PathVariable long externalMangaId) {
        return ApiResponse.ok(service.get(principal.id(), provider, externalMangaId));
    }

    /** PUT /reading-progress/{provider}/{externalMangaId} — upsert progres valid. */
    @PutMapping("/{provider}/{externalMangaId}")
    public ApiResponse<ReadingProgressDto> upsert(@AuthenticationPrincipal AuthenticatedUser principal,
                                                  @PathVariable String provider,
                                                  @PathVariable long externalMangaId,
                                                  @Valid @RequestBody ReadingProgressRequests.SaveRequest request) {
        if (request.externalMangaId() != null && request.externalMangaId() != externalMangaId) {
            throw com.example.mangareader.common.exception.AppException
                    .validation("externalMangaId pada body harus sama dengan path");
        }
        if (request.provider() != null && !request.provider().isBlank()
                && !request.provider().trim().equalsIgnoreCase(provider)) {
            throw com.example.mangareader.common.exception.AppException
                    .validation("provider pada body harus sama dengan path");
        }
        ReadingProgressRequests.SaveRequest normalized = new ReadingProgressRequests.SaveRequest(
                provider.toUpperCase(), externalMangaId,
                request.chapterExternalId(), request.chapterLabel(),
                request.pageIndex(), request.scrollPosition(), request.lastReadAt());
        return ApiResponse.ok(service.save(principal.id(), normalized));
    }

    /** POST /reading-progress — alias body-only dari kontrak utama POST /progress. */
    @PostMapping
    public ApiResponse<ReadingProgressDto> save(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @Valid @RequestBody ReadingProgressRequests.SaveRequest request) {
        return ApiResponse.ok(service.save(principal.id(), request));
    }
}
