CREATE TABLE writing_challenges
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    level      VARCHAR(20)  NOT NULL,
    title      VARCHAR(255) NOT NULL,
    prompt     TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_writing_challenges_user_id ON writing_challenges (user_id);

CREATE TABLE writing_submissions
(
    id               BIGSERIAL PRIMARY KEY,
    challenge_id     BIGINT    NOT NULL REFERENCES writing_challenges (id) ON DELETE CASCADE,
    user_id          BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    text             TEXT      NOT NULL,
    overall_feedback TEXT,
    score            INT,
    issues           TEXT      NOT NULL DEFAULT '[]',
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100)
);

CREATE INDEX idx_writing_submissions_user_id ON writing_submissions (user_id);
CREATE INDEX idx_writing_submissions_challenge_id ON writing_submissions (challenge_id);
