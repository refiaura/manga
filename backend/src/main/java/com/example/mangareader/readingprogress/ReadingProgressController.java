package com.example.mangareader.readingprogress;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
public class ReadingProgressController {

    private final ReadingProgressService service;

    public ReadingProgressController(ReadingProgressService service) {
        this.service = service;
    }

    /** POST /progress — simpan/perbarui progres (merge last-write-wins by lastReadAt). */
    @PostMapping
    public ApiResponse<ReadingProgressDto> save(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @Valid @RequestBody ReadingProgressRequests.SaveRequest request) {
        return ApiResponse.ok(service.save(principal.id(), request));
    }

    /** GET /progress/{provider}/{externalMangaId} — ambil progres satu manga. */
    @GetMapping("/{provider}/{externalMangaId}")
    public ApiResponse<ReadingProgressDto> get(@AuthenticationPrincipal AuthenticatedUser principal,
                                               @PathVariable String provider,
                                               @PathVariable long externalMangaId) {
        return ApiResponse.ok(service.get(principal.id(), provider, externalMangaId));
    }

    /** GET /progress/history — riwayat / lanjutkan membaca. */
    @GetMapping("/history")
    public ApiResponse<List<ReadingProgressDto>> history(@AuthenticationPrincipal AuthenticatedUser principal,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(service.history(principal.id(), limit));
    }
}
