CREATE TABLE listening_challenges
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    level      VARCHAR(20)  NOT NULL,
    sentence   TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_listening_challenges_user_id ON listening_challenges (user_id);

CREATE TABLE listening_submissions
(
    id           BIGSERIAL PRIMARY KEY,
    challenge_id BIGINT    NOT NULL REFERENCES listening_challenges (id) ON DELETE CASCADE,
    user_id      BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    transcript   TEXT      NOT NULL,
    correct      BOOLEAN   NOT NULL,
    feedback     TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100)
);

CREATE INDEX idx_listening_submissions_user_id ON listening_submissions (user_id);
CREATE INDEX idx_listening_submissions_challenge_id ON listening_submissions (challenge_id);
