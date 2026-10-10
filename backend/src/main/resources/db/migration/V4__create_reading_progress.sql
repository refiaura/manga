-- V4: reading_progress
CREATE TABLE reading_progress (
    id                  UUID PRIMARY KEY,
    user_id             UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider            VARCHAR(30)  NOT NULL,
    external_manga_id   BIGINT       NOT NULL,
    reading_source_id   UUID REFERENCES reading_sources (id) ON DELETE SET NULL,
    chapter_external_id VARCHAR(255),
    chapter_label       VARCHAR(100),
    page_index          INTEGER,
    scroll_position     NUMERIC(6,4),
    last_read_at        TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_reading_progress_user_provider_manga UNIQUE (user_id, provider, external_manga_id),
    CONSTRAINT reading_progress_page_index_check CHECK (page_index IS NULL OR page_index >= 0),
    CONSTRAINT reading_progress_scroll_check CHECK (scroll_position IS NULL OR (scroll_position >= 0 AND scroll_position <= 1))
);

CREATE INDEX idx_reading_progress_user_last_read ON reading_progress (user_id, last_read_at DESC);
