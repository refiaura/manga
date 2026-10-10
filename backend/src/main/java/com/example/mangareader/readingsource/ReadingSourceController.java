package com.example.mangareader.readingsource;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoint publik untuk membaca sumber aktif + endpoint admin untuk pengelolaan.
 * Otorisasi admin ditegakkan di backend (SecurityConfig membatasi /api/v1/admin/** ke ROLE_ADMIN).
 */
@RestController
@RequestMapping("/api/v1")
public class ReadingSourceController {

    private final ReadingSourceService service;

    public ReadingSourceController(ReadingSourceService service) {
        this.service = service;
    }

    /** Publik: sumber aktif untuk manga. */
    @GetMapping("/reading-sources/{provider}/{externalMangaId}")
    public ApiResponse<List<ReadingSourceDto>> list(@PathVariable String provider,
                                                    @PathVariable long externalMangaId) {
        return ApiResponse.ok(service.activeSources(provider, externalMangaId));
    }

    /** Admin: buat sumber baru. */
    @PostMapping("/admin/reading-sources")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReadingSourceDto> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @Valid @RequestBody ReadingSourceRequests.CreateRequest request) {
        return ApiResponse.ok(service.create(request, principal.id()));
    }

    /** Admin: verifikasi / aktif-nonaktifkan sumber. */
    @PatchMapping("/admin/reading-sources/{id}")
    public ApiResponse<ReadingSourceDto> update(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @PathVariable java.util.UUID id,
                                                @RequestParam(required = false) VerificationStatus verificationStatus,
                                                @RequestParam(required = false) Boolean active) {
        return ApiResponse.ok(service.update(id, verificationStatus, active, principal.id()));
    }

    /** Admin: lihat semua sumber termasuk yang nonaktif. */
    @GetMapping("/admin/reading-sources/{provider}/{externalMangaId}")
    public ApiResponse<List<ReadingSourceDto>> all(@PathVariable String provider,
                                                   @PathVariable long externalMangaId) {
        return ApiResponse.ok(service.allSources(provider, externalMangaId));
    }
}
