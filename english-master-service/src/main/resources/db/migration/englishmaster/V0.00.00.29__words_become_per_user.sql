-- Restructure: words move from a shared global dictionary (words + user_words join
-- table) to a per-user table (user_id column directly on words, user_words dropped).
-- Every user that had saved a word gets their own independent copy of that word, and
-- of its meanings/practices, so per-user data is no longer shared/cached across users.

-- 1. New per-user words table -------------------------------------------------------
CREATE TABLE words_new
(
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    text       VARCHAR(150) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_words_new_user_text UNIQUE (user_id, text)
);

INSERT INTO words_new (user_id, text, created_at, updated_at, created_by, updated_by)
SELECT uw.user_id, w.text, uw.created_at, uw.updated_at, uw.created_by, uw.updated_by
FROM user_words uw
         JOIN words w ON w.id = uw.word_id;

-- Mapping (user, old word) -> new word id. words_new's unique (user_id, text)
-- constraint makes this join deterministic. Dropped again at the end of the script.
CREATE TEMP TABLE word_id_mapping AS
SELECT uw.user_id, uw.word_id AS old_word_id, wn.id AS new_word_id
FROM user_words uw
         JOIN words w ON w.id = uw.word_id
         JOIN words_new wn ON wn.user_id = uw.user_id AND wn.text = w.text;

-- 2. New meanings table, duplicated per owning user ----------------------------------
CREATE TABLE meanings_new
(
    id             BIGSERIAL   PRIMARY KEY,
    word_id        BIGINT      NOT NULL REFERENCES words_new (id) ON DELETE CASCADE,
    part_of_speech VARCHAR(20) NOT NULL,
    meaning        TEXT        NOT NULL,
    ipa            VARCHAR(100),
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    old_meaning_id BIGINT      NOT NULL,
    new_user_id    BIGINT      NOT NULL
);

INSERT INTO meanings_new (word_id, part_of_speech, meaning, ipa, created_at, updated_at, created_by, updated_by,
                           old_meaning_id, new_user_id)
SELECT wim.new_word_id, m.part_of_speech, m.meaning, m.ipa, m.created_at, m.updated_at, m.created_by, m.updated_by,
       m.id, wim.user_id
FROM meanings m
         JOIN word_id_mapping wim ON wim.old_word_id = m.word_id;

-- 3. Duplicate the meaning <-> category tag associations for the new meaning rows ---
CREATE TABLE meaning_categories_new
(
    meaning_id  BIGINT NOT NULL REFERENCES meanings_new (id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    PRIMARY KEY (meaning_id, category_id)
);

INSERT INTO meaning_categories_new (meaning_id, category_id)
SELECT mn.id, mc.category_id
FROM meaning_categories mc
         JOIN meanings_new mn ON mn.old_meaning_id = mc.meaning_id;

-- 4. New practices table, duplicated per owning user ---------------------------------
CREATE TABLE practices_new
(
    id              BIGSERIAL    PRIMARY KEY,
    meaning_id      BIGINT       NOT NULL REFERENCES meanings_new (id) ON DELETE CASCADE,
    practice_type   VARCHAR(30)  NOT NULL,
    creation_source VARCHAR(30)  NOT NULL,
    options         TEXT         NOT NULL,
    question        VARCHAR(1000),
    correct_answer  TEXT         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    old_practice_id BIGINT       NOT NULL,
    new_user_id     BIGINT       NOT NULL
);

INSERT INTO practices_new (meaning_id, practice_type, creation_source, options, question, correct_answer,
                            created_at, updated_at, created_by, updated_by, old_practice_id, new_user_id)
SELECT mn.id, p.practice_type, p.creation_source, p.options, p.question, p.correct_answer,
       p.created_at, p.updated_at, p.created_by, p.updated_by, p.id, mn.new_user_id
FROM practices p
         JOIN meanings_new mn ON mn.old_meaning_id = p.meaning_id;

-- 5. Repoint user_practices / user_practice_results at the duplicated practice rows --
ALTER TABLE user_practices DROP CONSTRAINT user_practices_practice_id_fkey;
ALTER TABLE user_practice_results DROP CONSTRAINT user_practice_results_practice_id_fkey;

UPDATE user_practices up
SET practice_id = pn.id
FROM practices_new pn
WHERE pn.old_practice_id = up.practice_id
  AND pn.new_user_id = up.user_id;

UPDATE user_practice_results upr
SET practice_id = pn.id
FROM practices_new pn
WHERE pn.old_practice_id = upr.practice_id
  AND pn.new_user_id = upr.user_id;

-- 6. Drop old tables (dependency order) and swap the new ones into place ------------
DROP TABLE practices;
DROP TABLE meaning_categories;
DROP TABLE meanings;
DROP TABLE user_words;
DROP TABLE words;

ALTER TABLE words_new RENAME TO words;
ALTER TABLE meanings_new RENAME TO meanings;
ALTER TABLE meaning_categories_new RENAME TO meaning_categories;
ALTER TABLE practices_new RENAME TO practices;

ALTER SEQUENCE words_new_id_seq RENAME TO words_id_seq;
ALTER SEQUENCE meanings_new_id_seq RENAME TO meanings_id_seq;
ALTER SEQUENCE practices_new_id_seq RENAME TO practices_id_seq;

ALTER TABLE words RENAME CONSTRAINT words_new_pkey TO words_pkey;
ALTER TABLE words RENAME CONSTRAINT uk_words_new_user_text TO uk_words_user_text;
ALTER TABLE meanings RENAME CONSTRAINT meanings_new_pkey TO meanings_pkey;
ALTER TABLE practices RENAME CONSTRAINT practices_new_pkey TO practices_pkey;

ALTER TABLE meanings DROP COLUMN old_meaning_id;
ALTER TABLE meanings DROP COLUMN new_user_id;
ALTER TABLE practices DROP COLUMN old_practice_id;
ALTER TABLE practices DROP COLUMN new_user_id;

-- 7. Re-add the FKs from user_practices / user_practice_results to the new practices -
ALTER TABLE user_practices
    ADD CONSTRAINT user_practices_practice_id_fkey FOREIGN KEY (practice_id) REFERENCES practices (id) ON DELETE CASCADE;
ALTER TABLE user_practice_results
    ADD CONSTRAINT user_practice_results_practice_id_fkey FOREIGN KEY (practice_id) REFERENCES practices (id) ON DELETE CASCADE;

-- 8. Indexes -------------------------------------------------------------------------
CREATE INDEX idx_words_user_id ON words (user_id);
CREATE INDEX idx_meanings_word_id ON meanings (word_id);
CREATE INDEX idx_practices_meaning_id ON practices (meaning_id);

DROP TABLE word_id_mapping;
