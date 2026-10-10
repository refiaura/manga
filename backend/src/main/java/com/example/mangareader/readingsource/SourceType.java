package com.example.mangareader.readingsource;

/**
 * Tipe sumber bacaan. MVP hanya mendukung tautan ke penyedia resmi/berizin.
 * LICENSED_READER dicadangkan untuk reader internal di masa depan (hanya konten berizin).
 */
public enum SourceType {
    OFFICIAL_LINK,
    LICENSED_READER
}
