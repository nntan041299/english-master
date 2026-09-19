-- Every remaining table that carries a user_id column gets it folded into its primary
-- key, same as meanings/practices (V0.00.00.31) and categories/listening_submissions
-- (in flight). user_id is already NOT NULL on all of these, and no FK constraint exists
-- anywhere in the schema (V0.00.00.31 dropped them all), so each widening is a plain
-- PK swap with no backfill or FK cleanup needed.
--
-- Tables intentionally left out: listening_challenges (shared pool, not per-user, see
-- V0.00.00.22), revoked_tokens (no user_id column), and users itself.

ALTER TABLE words DROP CONSTRAINT words_pkey;
ALTER TABLE words ADD CONSTRAINT words_pkey PRIMARY KEY (id, user_id);

ALTER TABLE writing_challenges DROP CONSTRAINT writing_challenges_pkey;
ALTER TABLE writing_challenges ADD CONSTRAINT writing_challenges_pkey PRIMARY KEY (id, user_id);

ALTER TABLE writing_submissions DROP CONSTRAINT writing_submissions_pkey;
ALTER TABLE writing_submissions ADD CONSTRAINT writing_submissions_pkey PRIMARY KEY (id, user_id);

ALTER TABLE translation_challenges DROP CONSTRAINT translation_challenges_pkey;
ALTER TABLE translation_challenges ADD CONSTRAINT translation_challenges_pkey PRIMARY KEY (id, user_id);

ALTER TABLE translation_submissions DROP CONSTRAINT translation_submissions_pkey;
ALTER TABLE translation_submissions ADD CONSTRAINT translation_submissions_pkey PRIMARY KEY (id, user_id);

ALTER TABLE user_practices DROP CONSTRAINT user_practices_pkey;
ALTER TABLE user_practices ADD CONSTRAINT user_practices_pkey PRIMARY KEY (id, user_id);

ALTER TABLE user_practice_results DROP CONSTRAINT user_practice_results_pkey;
ALTER TABLE user_practice_results ADD CONSTRAINT user_practice_results_pkey PRIMARY KEY (id, user_id);
