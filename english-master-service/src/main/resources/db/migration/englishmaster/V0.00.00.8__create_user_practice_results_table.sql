CREATE TABLE user_practice_results
(
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    practice_id     BIGINT      NOT NULL REFERENCES practices (id) ON DELETE CASCADE,
    answered_option VARCHAR(20) NOT NULL CHECK (answered_option IN ('OPTION_1', 'OPTION_2', 'OPTION_3', 'OPTION_4')),
    is_correct      BOOLEAN     NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_upr_user_id ON user_practice_results (user_id);
CREATE INDEX idx_upr_practice_id ON user_practice_results (practice_id);
