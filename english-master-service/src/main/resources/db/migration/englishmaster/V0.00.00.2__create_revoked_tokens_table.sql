CREATE TABLE revoked_tokens
(
    token      TEXT         NOT NULL PRIMARY KEY,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_revoked_tokens_expires_at ON revoked_tokens (expires_at);
