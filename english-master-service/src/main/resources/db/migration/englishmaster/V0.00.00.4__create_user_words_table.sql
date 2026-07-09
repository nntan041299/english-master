CREATE TABLE user_words
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    word_id            BIGINT    NOT NULL REFERENCES words (id) ON DELETE CASCADE,
    practices_assigned BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    CONSTRAINT uk_user_words_user_word UNIQUE (user_id, word_id)
);

CREATE INDEX idx_user_words_user_id ON user_words (user_id);
CREATE INDEX idx_user_words_word_id ON user_words (word_id);
