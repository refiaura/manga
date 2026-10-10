package com.example.mangareader.library;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller kompatibilitas kontrak PRD Bagian 8:
 * - PATCH /api/v1/library/{id}  (ubah status)
 * - DELETE /api/v1/library/{id} (hapus item)
 * dengan {id} = "{PROVIDER}:{externalMangaId}", mis. "JIKAN:1".
 *
 * Endpoint utama tetap PUT/DELETE /library/{provider}/{externalMangaId};
 * kelas ini hanya menyediakan alias agar frontend yang mengikuti tabel kontrak
 * PRD dapat berfungsi tanpa duplikasi business logic.
 */
@RestController
@RequestMapping("/api/v1/library")
public class LibraryAliasController {

    private final UserLibraryService libraryService;

    public LibraryAliasController(UserLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /** GET /library?status=READING — daftar entri, opsional difilter per status. */
    @GetMapping(params = "status")
    public ApiResponse<List<LibraryItemDto>> listByStatus(@AuthenticationPrincipal AuthenticatedUser principal,
                                                          @RequestParam ReadingStatus status) {
        return ApiResponse.ok(libraryService.list(principal.id()).stream()
                .filter(item -> item.status() == status)
                .toList());
    }

    /** PATCH /library/{id} — ubah status membaca. id = PROVIDER:mangaId. */
    @PatchMapping("/{id}")
    public ApiResponse<LibraryItemDto> updateStatus(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @PathVariable String id,
                                                    @Valid @RequestBody LibraryRequests.StatusRequest request) {
        Key key = parseKey(id);
        return ApiResponse.ok(libraryService.upsert(principal.id(), key.provider(), key.mangaId(), request.status()));
    }

    /** DELETE /library/{id} — hapus entri. Idempotent terhadap state akhir. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AuthenticatedUser principal,
                       @PathVariable String id) {
        Key key = parseKey(id);
        libraryService.remove(principal.id(), key.provider(), key.mangaId());
    }

    private static Key parseKey(String id) {
        int idx = id.indexOf(':');
        if (idx <= 0 || idx == id.length() - 1) {
            throw AppException.notFound("Entri library tidak ditemukan");
        }
        String provider = id.substring(0, idx).toUpperCase();
        long mangaId;
        try {
            mangaId = Long.parseLong(id.substring(idx + 1));
        } catch (NumberFormatException e) {
            throw AppException.notFound("Entri library tidak ditemukan");
        }
        if (mangaId <= 0) throw AppException.notFound("Entri library tidak ditemukan");
        return new Key(provider, mangaId);
    }

    private record Key(String provider, long mangaId) {}
}
