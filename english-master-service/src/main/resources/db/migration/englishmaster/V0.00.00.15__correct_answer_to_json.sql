-- correct_answer now stores a JSON array of ids to support ordered multi-select answers
TRUNCATE TABLE user_practice_results CASCADE;
TRUNCATE TABLE user_practices CASCADE;
TRUNCATE TABLE practices CASCADE;

ALTER TABLE practices
    ALTER COLUMN correct_answer TYPE TEXT;
