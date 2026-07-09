CREATE TABLE practices
(
    id             BIGSERIAL    PRIMARY KEY,
    meaning_id     BIGINT       NOT NULL REFERENCES meanings (id) ON DELETE CASCADE,
    option_1       VARCHAR(500) NOT NULL,
    option_2       VARCHAR(500) NOT NULL,
    option_3       VARCHAR(500) NOT NULL,
    option_4       VARCHAR(500) NOT NULL,
    correct_answer VARCHAR(20)  NOT NULL CHECK (correct_answer IN ('OPTION_1', 'OPTION_2', 'OPTION_3', 'OPTION_4')),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100)
);

CREATE INDEX idx_practices_meaning_id ON practices (meaning_id);
