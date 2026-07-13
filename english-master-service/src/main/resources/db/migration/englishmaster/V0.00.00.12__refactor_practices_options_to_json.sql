-- Truncate dependent tables first (practices are AI-generated and will be regenerated)
TRUNCATE TABLE user_practice_results CASCADE;
TRUNCATE TABLE user_practices CASCADE;
TRUNCATE TABLE practices CASCADE;

-- Restructure practices table
ALTER TABLE practices
    DROP COLUMN option_1,
    DROP COLUMN option_2,
    DROP COLUMN option_3,
    DROP COLUMN option_4;

ALTER TABLE practices
    ADD COLUMN options TEXT NOT NULL DEFAULT '[]';

ALTER TABLE practices
    DROP CONSTRAINT practices_correct_answer_check;

ALTER TABLE practices
    ALTER COLUMN correct_answer TYPE VARCHAR(100),
    ALTER COLUMN correct_answer DROP DEFAULT;

-- Remove enum constraint from user_practice_results
ALTER TABLE user_practice_results
    DROP CONSTRAINT user_practice_results_answered_option_check;

ALTER TABLE user_practice_results
    ALTER COLUMN answered_option TYPE VARCHAR(100);
