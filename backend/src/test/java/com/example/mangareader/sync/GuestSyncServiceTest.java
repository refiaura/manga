package com.example.mangareader.library;

import com.example.mangareader.readingprogress.ReadingProgressRequests;
import com.example.mangareader.sync.GuestSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Aturan merge sinkronisasi guest → akun. */
class GuestSyncServiceTest {

    private UserLibraryService libraryService;
    private com.example.mangareader.readingprogress.ReadingProgressService progressService;
    private GuestSyncService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        libraryService = mock(UserLibraryService.class);
        progressService = mock(com.example.mangareader.readingprogress.ReadingProgressService.class);
        service = new GuestSyncService(libraryService, progressService);
    }

    @Test
    void syncsNewLibraryItemsAndSkipsExistingOnes() {
        LibraryItemDto existing = new LibraryItemDto(UUID.randomUUID(), "JIKAN", 1L,
                ReadingStatus.COMPLETED, Instant.now(), Instant.now());
        when(libraryService.list(userId)).thenReturn(List.of(existing));
        when(libraryService.upsert(eq(userId), eq("JIKAN"), eq(2L), eq(ReadingStatus.READING)))
                .thenReturn(new LibraryItemDto(UUID.randomUUID(), "JIKAN", 2L,
                        ReadingStatus.READING, Instant.now(), Instant.now()));

        var request = new GuestSyncService.SyncRequest(1, List.of(
                new GuestSyncService.GuestLibraryItem(null, 1L, "READING"),   // sudah ada di akun -> skip
                new GuestSyncService.GuestLibraryItem("jikan", 2L, "reading")  // baru -> sinkron (case-insensitive)
        ), List.of());

        var result = service.sync(userId, request);

        assertThat(result.librarySynced()).isEqualTo(1);
        assertThat(result.skipped()).anySatisfy(s -> assertThat(s).contains("library:JIKAN:1"));
        verify(libraryService).upsert(userId, "JIKAN", 2L, ReadingStatus.READING);
        verify(libraryService, never()).upsert(eq(userId), eq("JIKAN"), eq(1L), any());
    }

    @Test
    void rejectsInvalidSchemaVersion() {
        var request = new GuestSyncService.SyncRequest(0, List.of(), List.of());
        try {
            service.sync(userId, request);
            assertThat(false).as("should throw").isTrue();
        } catch (com.example.mangareader.common.exception.AppException ex) {
            assertThat(ex.getPublicMessage()).contains("Skema");
        }
    }

    @Test
    void skipsInvalidItemsButKeepsValidOnes_partialFailure() {
        when(libraryService.list(userId)).thenReturn(List.of());
        when(progressService.save(eq(userId), any(ReadingProgressRequests.SaveRequest.class)))
                .thenAnswer(inv -> new com.example.mangareader.readingprogress.ReadingProgressDto(
                        UUID.randomUUID(), "JIKAN", 5L, null, "ch-1", "Ch 1", 3, null,
                        Instant.parse("2026-01-01T00:00:00Z"), Instant.now()));

        var request = new GuestSyncService.SyncRequest(1,
                List.of(
                        new GuestSyncService.GuestLibraryItem("JIKAN", -3L, "READING"), // id tidak valid
                        new GuestSyncService.GuestLibraryItem("JIKAN", 4L, "BOGUS")     // status tidak valid
                ),
                List.of(
                        new GuestSyncService.GuestProgressItem("JIKAN", 5L, "ch-1", "Ch 1", 3, null,
                                Instant.parse("2026-01-01T00:00:00Z")),
                        new GuestSyncService.GuestProgressItem("JIKAN", 6L, null, null, null, null, null) // tanpa lastReadAt
                ));

        var result = service.sync(userId, request);

        assertThat(result.librarySynced()).isZero();
        assertThat(result.progressSynced()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(3);
    }

    @Test
    void syncIsIdempotent_secondRunAddsNothingNew() {
        // Run 1: kosong -> 1 item disinkronkan
        when(libraryService.list(userId)).thenReturn(List.of());
        when(libraryService.upsert(eq(userId), eq("JIKAN"), eq(7L), eq(ReadingStatus.PLAN_TO_READ)))
                .thenReturn(new LibraryItemDto(UUID.randomUUID(), "JIKAN", 7L,
                        ReadingStatus.PLAN_TO_READ, Instant.now(), Instant.now()));
        var request = new GuestSyncService.SyncRequest(1, List.of(
                new GuestSyncService.GuestLibraryItem("JIKAN", 7L, "PLAN_TO_READ")), List.of());
        assertThat(service.sync(userId, request).librarySynced()).isEqualTo(1);

        // Run 2: entri sudah ada di akun -> 0新增, state akhir sama (idempoten)
        when(libraryService.list(userId)).thenReturn(List.of(
                new LibraryItemDto(UUID.randomUUID(), "JIKAN", 7L,
                        ReadingStatus.PLAN_TO_READ, Instant.now(), Instant.now())));
        var second = service.sync(userId, request);
        assertThat(second.librarySynced()).isZero();
        assertThat(second.skipped()).anySatisfy(s -> assertThat(s).contains("dipertahankan"));
    }
}
