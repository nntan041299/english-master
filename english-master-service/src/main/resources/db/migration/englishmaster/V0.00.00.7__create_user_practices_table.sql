CREATE TABLE user_practices
(
    id                BIGSERIAL   PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    practice_id       BIGINT      NOT NULL REFERENCES practices (id) ON DELETE CASCADE,
    level             VARCHAR(20) NOT NULL DEFAULT 'TRACKING1',
    last_practiced_at TIMESTAMP,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    CONSTRAINT uq_user_practices UNIQUE (user_id, practice_id)
);

CREATE INDEX idx_user_practices_user_id ON user_practices (user_id);
CREATE INDEX idx_user_practices_practice_id ON user_practices (practice_id);
