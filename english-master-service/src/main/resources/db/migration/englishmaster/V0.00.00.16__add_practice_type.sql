TRUNCATE TABLE user_practice_results, user_practices, practices RESTART IDENTITY CASCADE;

ALTER TABLE practices
    ADD COLUMN practice_type VARCHAR(30) NOT NULL DEFAULT 'SINGLE_CHOICE';
