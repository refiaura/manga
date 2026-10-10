package com.example.mangareader.library;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library")
public class UserLibraryController {

    private final UserLibraryService libraryService;

    public UserLibraryController(UserLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /** GET /library — daftar seluruh entri milik pengguna saat ini. */
    @GetMapping
    public ApiResponse<List<LibraryItemDto>> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.ok(libraryService.list(principal.id()));
    }

    /** POST /library — tambah/perbarui entri (idempotent upsert). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LibraryItemDto> upsert(@AuthenticationPrincipal AuthenticatedUser principal,
                                              @Valid @RequestBody LibraryRequests.UpsertRequest request) {
        LibraryItemDto item = libraryService.upsert(
                principal.id(), request.providerOrDefault(), request.externalMangaId(), request.status());
        return ApiResponse.ok(item);
    }

    /** PUT /library/{provider}/{externalMangaId} — ubah status membaca. */
    @PutMapping("/{provider}/{externalMangaId}")
    public ApiResponse<LibraryItemDto> updateStatus(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @PathVariable String provider,
                                                    @PathVariable long externalMangaId,
                                                    @RequestParam ReadingStatus status) {
        return ApiResponse.ok(libraryService.upsert(
                principal.id(), provider.toUpperCase(), externalMangaId, status));
    }

    /** DELETE /library/{provider}/{externalMangaId} — hapus entri. Idempotent terhadap state akhir. */
    @DeleteMapping("/{provider}/{externalMangaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AuthenticatedUser principal,
                       @PathVariable String provider,
                       @PathVariable long externalMangaId) {
        libraryService.remove(principal.id(), provider.toUpperCase(), externalMangaId);
    }
}
