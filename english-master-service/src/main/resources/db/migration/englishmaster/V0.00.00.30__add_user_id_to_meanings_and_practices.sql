-- Denormalize user ownership onto meanings and practices. Both were only reachable
-- via meanings -> words.user_id (and practices -> meanings -> words.user_id); add a
-- direct user_id column to each so ownership no longer requires walking that chain.

-- 1. meanings ------------------------------------------------------------------------
ALTER TABLE meanings ADD COLUMN user_id BIGINT REFERENCES users (id) ON DELETE CASCADE;

UPDATE meanings m
SET user_id = w.user_id
FROM words w
WHERE w.id = m.word_id;

ALTER TABLE meanings ALTER COLUMN user_id SET NOT NULL;

CREATE INDEX idx_meanings_user_id ON meanings (user_id);

-- 2. practices -------------------------------------------------------------------------
ALTER TABLE practices ADD COLUMN user_id BIGINT REFERENCES users (id) ON DELETE CASCADE;

UPDATE practices p
SET user_id = m.user_id
FROM meanings m
WHERE m.id = p.meaning_id;

ALTER TABLE practices ALTER COLUMN user_id SET NOT NULL;

CREATE INDEX idx_practices_user_id ON practices (user_id);
