CREATE TABLE translation_challenges
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    direction   VARCHAR(20)  NOT NULL,
    level       VARCHAR(20)  NOT NULL,
    source_text TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE INDEX idx_translation_challenges_user_id ON translation_challenges (user_id);

CREATE TABLE translation_submissions
(
    id                    BIGSERIAL PRIMARY KEY,
    challenge_id          BIGINT    NOT NULL REFERENCES translation_challenges (id) ON DELETE CASCADE,
    user_id               BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_translation      TEXT      NOT NULL,
    correct               BOOLEAN   NOT NULL,
    feedback              TEXT,
    suggested_translation TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100)
);

CREATE INDEX idx_translation_submissions_user_id ON translation_submissions (user_id);
CREATE INDEX idx_translation_submissions_challenge_id ON translation_submissions (challenge_id);
