package com.example.mangareader.sync;

import com.example.mangareader.auth.AuthenticatedUser;
import com.example.mangareader.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class GuestSyncController {

    private final GuestSyncService syncService;

    public GuestSyncController(GuestSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * POST /sync/guest — kirim data guest (library + progress) ke akun.
     * Idempotent: pengiriman berulang menghasilkan state akhir yang sama.
     */
    @PostMapping({"/guest", "/guest-library"})
    public ApiResponse<GuestSyncService.SyncResult> sync(@AuthenticationPrincipal AuthenticatedUser principal,
                                                         @Valid @RequestBody GuestSyncService.SyncRequest request) {
        return ApiResponse.ok(syncService.sync(principal.id(), request));
    }
}
