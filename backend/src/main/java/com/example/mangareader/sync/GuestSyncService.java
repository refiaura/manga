package com.example.mangareader.sync;

import com.example.mangareader.common.exception.AppException;
import com.example.mangareader.library.LibraryItemDto;
import com.example.mangareader.library.ReadingStatus;
import com.example.mangareader.library.UserLibraryService;
import com.example.mangareader.readingprogress.ReadingProgressDto;
import com.example.mangareader.readingprogress.ReadingProgressRequests;
import com.example.mangareader.readingprogress.ReadingProgressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sinkronisasi data guest (localStorage) ke akun.
 * Aturan merge:
 *  - Library: upsert idempotent per (provider, externalMangaId). Entri akun yang sudah ada
 *    tidak ditimpa oleh status guest.
 *  - Progress: didelegasikan ke ReadingProgressService dengan aturan last-write-wins by lastReadAt,
 *    sehingga progres akun yang lebih baru tidak pernah ditimpa data lama guest.
 *  - Item invalid dilewati dan dilaporkan pada response (kegagalan parsial), bukan membatalkan semuanya.
 *  - Frontend wajib menunggu response sukses sebelum menghapus data lokal.
 */
@Service
public class GuestSyncService {

    public static final int MAX_ITEMS = 200;

    private final UserLibraryService libraryService;
    private final ReadingProgressService progressService;

    public GuestSyncService(UserLibraryService libraryService, ReadingProgressService progressService) {
        this.libraryService = libraryService;
        this.progressService = progressService;
    }

    public SyncResult sync(UUID userId, SyncRequest request) {
        if (request.schemaVersion() == null || request.schemaVersion() < CURRENT_SCHEMA_VERSION) {
            throw AppException.validation("Skema data guest tidak dikenal/telah usang");
        }
        List<String> skipped = new ArrayList<>();
        List<LibraryItemDto> library = new ArrayList<>();
        List<ReadingProgressDto> progress = new ArrayList<>();

        List<GuestLibraryItem> libItems = request.library() == null ? List.of() : request.library();
        if (libItems.size() > MAX_ITEMS) throw AppException.validation("Terlalu banyak item library (maks " + MAX_ITEMS + ")");
        List<LibraryItemDto> existingEntries = libraryService.list(userId);
        for (GuestLibraryItem item : libItems) {
            String key = "library:" + normalizeProvider(item.provider()) + ":" + item.externalMangaId();
            if (item.externalMangaId() == null || item.externalMangaId() <= 0) {
                skipped.add(key + " (externalMangaId tidak valid)");
                continue;
            }
            ReadingStatus status = ReadingStatus.parse(item.status());
            if (status == null) {
                skipped.add(key + " (status tidak valid)");
                continue;
            }
            String provider = normalizeProvider(item.provider());
            boolean alreadyExists = existingEntries.stream()
                    .anyMatch(e -> e.provider().equals(provider) && e.externalMangaId() == item.externalMangaId());
            if (alreadyExists) {
                // Jangan menimpa entri akun yang sudah ada dengan status guest.
                skipped.add(key + " (sudah ada di akun, dipertahankan)");
            } else {
                library.add(libraryService.upsert(userId, provider, item.externalMangaId(), status));
            }
        }

        List<GuestProgressItem> progItems = request.progress() == null ? List.of() : request.progress();
        if (progItems.size() > MAX_ITEMS) throw AppException.validation("Terlalu banyak item progress (maks " + MAX_ITEMS + ")");
        for (GuestProgressItem item : progItems) {
            String key = "progress:" + normalizeProvider(item.provider()) + ":" + item.externalMangaId();
            if (item.externalMangaId() == null || item.externalMangaId() <= 0) {
                skipped.add(key + " (externalMangaId tidak valid)");
                continue;
            }
            if (item.lastReadAt() == null) {
                skipped.add(key + " (lastReadAt tidak tersedia)");
                continue;
            }
            try {
                progress.add(progressService.save(userId, new ReadingProgressRequests.SaveRequest(
                        item.provider(), item.externalMangaId(), item.chapterExternalId(), item.chapterLabel(),
                        item.pageIndex(), item.scrollPosition(), item.lastReadAt())));
            } catch (AppException ex) {
                skipped.add(key + " (" + ex.getPublicMessage() + ")");
            }
        }

        return new SyncResult(library.size(), progress.size(), skipped.size(), library, progress, skipped);
    }

    /** Versi skema data guest yang diterima server. Naikkan bila format localStorage berubah. */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    static String normalizeProvider(String provider) {
        return (provider == null || provider.isBlank()) ? "JIKAN" : provider.trim().toUpperCase();
    }

    // ==== DTO ====

    public record SyncRequest(
            @NotNull(message = "schemaVersion wajib diisi")
            Integer schemaVersion,
            @Size(max = MAX_ITEMS) List<@Valid GuestLibraryItem> library,
            @Size(max = MAX_ITEMS) List<@Valid GuestProgressItem> progress
    ) {}

    public record GuestLibraryItem(
            @Size(max = 30) String provider,
            Long externalMangaId,
            @Size(max = 30) String status
    ) {}

    public record GuestProgressItem(
            @Size(max = 30) String provider,
            Long externalMangaId,
            @Size(max = 255) String chapterExternalId,
            @Size(max = 100) String chapterLabel,
            Integer pageIndex,
            BigDecimal scrollPosition,
            Instant lastReadAt
    ) {}

    public record SyncResult(int librarySynced, int progressSynced, int skippedCount,
                             List<LibraryItemDto> library, List<ReadingProgressDto> progress,
                             List<String> skipped) {}
}
