CREATE TABLE meanings
(
    id             BIGSERIAL   PRIMARY KEY,
    word_id        BIGINT      NOT NULL REFERENCES words (id) ON DELETE CASCADE,
    part_of_speech VARCHAR(20) NOT NULL,
    meaning        TEXT        NOT NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100)
);

CREATE INDEX idx_meanings_word_id ON meanings (word_id);
