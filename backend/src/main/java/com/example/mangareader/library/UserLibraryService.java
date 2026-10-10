package com.example.mangareader.library;

import com.example.mangareader.common.exception.AppException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Logika My Library. Semua operasi diformalkan berdasarkan principal tepercaya (userId),
 * sehingga pengguna tidak dapat membaca/mengubah perpustakaan pengguna lain.
 * Upsert idempotent: request berulang pada manga yang sama aman.
 */
@Service
public class UserLibraryService {

    private final UserLibraryRepository repository;

    public UserLibraryService(UserLibraryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LibraryItemDto upsert(UUID userId, String provider, Long externalMangaId, ReadingStatus status) {
        if (status == null) throw AppException.validation("status membaca tidak valid");
        var existing = repository.findByUserIdAndProviderAndExternalMangaId(userId, provider, externalMangaId);
        if (existing.isPresent()) {
            UserLibrary item = existing.get();
            item.setReadingStatus(status);
            return LibraryItemDto.from(repository.save(item));
        }
        UserLibrary item = new UserLibrary();
        item.setUserId(userId);
        item.setProvider(provider);
        item.setExternalMangaId(externalMangaId);
        item.setReadingStatus(status);
        try {
            return LibraryItemDto.from(repository.save(item));
        } catch (DataIntegrityViolationException ex) {
            // Race: entri dibuat bersamaan — update saja (idempotent).
            return repository.findByUserIdAndProviderAndExternalMangaId(userId, provider, externalMangaId)
                    .map(e -> {
                        e.setReadingStatus(status);
                        return LibraryItemDto.from(repository.save(e));
                    })
                    .orElseThrow(() -> new AppException(
                            com.example.mangareader.common.exception.ErrorCode.INTERNAL_ERROR,
                            "Gagal menyimpan perpustakaan"));
        }
    }

    @Transactional(readOnly = true)
    public List<LibraryItemDto> list(UUID userId) {
        return repository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(LibraryItemDto::from)
                .toList();
    }

    @Transactional
    public void remove(UUID userId, String provider, Long externalMangaId) {
        UserLibrary item = repository.findByUserIdAndProviderAndExternalMangaId(userId, provider, externalMangaId)
                .orElseThrow(() -> AppException.notFound("Entri tidak ditemukan di perpustakaan"));
        repository.delete(item);
    }
}
