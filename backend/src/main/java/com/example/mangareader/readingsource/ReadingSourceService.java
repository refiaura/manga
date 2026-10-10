package com.example.mangareader.readingsource;

import com.example.mangareader.common.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
public class ReadingSourceService {

    private final ReadingSourceRepository repository;

    public ReadingSourceService(ReadingSourceRepository repository) {
        this.repository = repository;
    }

    /** Publik: hanya sumber aktif yang tampil. */
    @Transactional(readOnly = true)
    public List<ReadingSourceDto> activeSources(String provider, long externalMangaId) {
        return repository.findByProviderAndExternalMangaIdAndActiveTrue(provider.toUpperCase(), externalMangaId)
                .stream().map(ReadingSourceDto::from).toList();
    }

    /** Admin: daftar semua (termasuk nonaktif/pending). */
    @Transactional(readOnly = true)
    public List<ReadingSourceDto> allSources(String provider, long externalMangaId) {
        return repository.findByProviderAndExternalMangaId(provider.toUpperCase(), externalMangaId)
                .stream().map(ReadingSourceDto::from).toList();
    }

    @Transactional
    public ReadingSourceDto create(ReadingSourceRequests.CreateRequest req, UUID adminId) {
        validateHttpUrl(req.sourceUrl());
        if (req.sourceType() == SourceType.LICENSED_READER && req.verificationStatus() == VerificationStatus.VERIFIED
                && (req.licenseReference() == null || req.licenseReference().isBlank())) {
            // Jangan tandai terverifikasi tanpa dasar pemeriksaan/izin yang tercatat.
            throw AppException.validation("Sumber LICENSED_READER yang VERIFIED wajib memiliki licenseReference");
        }
        ReadingSource s = new ReadingSource();
        s.setProvider(req.providerOrDefault());
        s.setExternalMangaId(req.externalMangaId());
        s.setSourceName(req.sourceName().trim());
        s.setSourceUrl(req.sourceUrl().trim());
        s.setSourceType(req.sourceType());
        s.setVerificationStatus(req.verificationStatus() == null ? VerificationStatus.PENDING : req.verificationStatus());
        s.setLicenseReference(req.licenseReference());
        s.setActive(Boolean.TRUE.equals(req.active()));
        s.setCreatedBy(adminId);
        s.setUpdatedBy(adminId);
        return ReadingSourceDto.from(repository.save(s));
    }

    /** Admin menandai verifikasi/aktif-tidaknya sumber. */
    @Transactional
    public ReadingSourceDto update(UUID id, VerificationStatus verificationStatus, Boolean active, UUID adminId) {
        ReadingSource s = repository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sumber bacaan tidak ditemukan"));
        if (verificationStatus != null) {
            if (verificationStatus == VerificationStatus.VERIFIED
                    && s.getSourceType() == SourceType.LICENSED_READER
                    && (s.getLicenseReference() == null || s.getLicenseReference().isBlank())) {
                throw AppException.validation("Tidak dapat memverifikasi sumber tanpa licenseReference");
            }
            s.setVerificationStatus(verificationStatus);
        }
        if (active != null) s.setActive(active);
        s.setUpdatedBy(adminId);
        return ReadingSourceDto.from(repository.save(s));
    }

    static void validateHttpUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || !(uri.getScheme().equals("https") || uri.getScheme().equals("http")) || uri.getHost() == null) {
                throw AppException.validation("sourceUrl harus URL http/https yang valid");
            }
        } catch (IllegalArgumentException ex) {
            throw AppException.validation("sourceUrl tidak valid");
        }
    }
}
