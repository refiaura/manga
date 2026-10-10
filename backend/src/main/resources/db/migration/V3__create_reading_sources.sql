-- V3: reading_sources
CREATE TABLE reading_sources (
    id                  UUID PRIMARY KEY,
    provider            VARCHAR(30) NOT NULL,
    external_manga_id   BIGINT      NOT NULL,
    source_name         VARCHAR(150) NOT NULL,
    source_url          TEXT         NOT NULL,
    source_type         VARCHAR(30)  NOT NULL,
    verification_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    license_reference   TEXT,
    is_active           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by          UUID REFERENCES users (id) ON DELETE SET NULL,
    updated_by          UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT reading_sources_type_check CHECK (source_type IN ('OFFICIAL_LINK', 'LICENSED_READER')),
    CONSTRAINT reading_sources_verification_check CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX idx_reading_sources_provider_manga ON reading_sources (provider, external_manga_id);
