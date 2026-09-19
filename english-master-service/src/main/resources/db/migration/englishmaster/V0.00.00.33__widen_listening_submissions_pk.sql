-- Widen the listening_submissions primary key to (id, user_id), same as meanings/practices
-- in V0.00.00.31. user_id already exists and is NOT NULL, and nothing else in the schema
-- references listening_submissions.id, so no FK cleanup is needed first.

ALTER TABLE listening_submissions DROP CONSTRAINT listening_submissions_pkey;
ALTER TABLE listening_submissions ADD CONSTRAINT listening_submissions_pkey PRIMARY KEY (id, user_id);
