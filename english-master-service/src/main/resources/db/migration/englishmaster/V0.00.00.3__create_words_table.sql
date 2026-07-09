CREATE TABLE words
(
    id         BIGSERIAL    PRIMARY KEY,
    text       VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_words_text ON words (text);
