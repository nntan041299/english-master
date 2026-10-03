-- Fold each table's owning foreign-key column into its primary key, alongside user_id.
-- Every one of these columns is already NOT NULL, and no FK constraint exists anywhere
-- in the schema (dropped in V0.00.00.31), so each is a plain PK swap.

ALTER TABLE meanings DROP CONSTRAINT meanings_pkey;
ALTER TABLE meanings ADD CONSTRAINT meanings_pkey PRIMARY KEY (id, user_id, word_id);

ALTER TABLE practices DROP CONSTRAINT practices_pkey;
ALTER TABLE practices ADD CONSTRAINT practices_pkey PRIMARY KEY (id, user_id, meaning_id);

ALTER TABLE listening_submissions DROP CONSTRAINT listening_submissions_pkey;
ALTER TABLE listening_submissions ADD CONSTRAINT listening_submissions_pkey PRIMARY KEY (id, user_id, challenge_id);

ALTER TABLE writing_submissions DROP CONSTRAINT writing_submissions_pkey;
ALTER TABLE writing_submissions ADD CONSTRAINT writing_submissions_pkey PRIMARY KEY (id, user_id, challenge_id);

ALTER TABLE translation_submissions DROP CONSTRAINT translation_submissions_pkey;
ALTER TABLE translation_submissions ADD CONSTRAINT translation_submissions_pkey PRIMARY KEY (id, user_id, challenge_id);

ALTER TABLE user_practices DROP CONSTRAINT user_practices_pkey;
ALTER TABLE user_practices ADD CONSTRAINT user_practices_pkey PRIMARY KEY (id, user_id, practice_id);

ALTER TABLE user_practice_results DROP CONSTRAINT user_practice_results_pkey;
ALTER TABLE user_practice_results ADD CONSTRAINT user_practice_results_pkey PRIMARY KEY (id, user_id, practice_id);
