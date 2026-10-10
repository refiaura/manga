-- V2: user_library
CREATE TABLE user_library (
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider          VARCHAR(30)  NOT NULL,
    external_manga_id BIGINT       NOT NULL,
    reading_status    VARCHAR(30)  NOT NULL,
    added_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_library_user_provider_manga UNIQUE (user_id, provider, external_manga_id),
    CONSTRAINT user_library_status_check CHECK (reading_status IN
        ('READING', 'PLAN_TO_READ', 'COMPLETED', 'ON_HOLD', 'DROPPED'))
);

CREATE INDEX idx_user_library_user_status ON user_library (user_id, reading_status);
