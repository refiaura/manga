package com.example.mangareader.library;

/** Status membaca sesuai PRD (enum MAL-style yang dibatasi). */
public enum ReadingStatus {
    READING,
    PLAN_TO_READ,
    COMPLETED,
    ON_HOLD,
    DROPPED;

    public static ReadingStatus parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return ReadingStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
