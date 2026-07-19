ALTER TABLE users
    ADD COLUMN language_level VARCHAR(10) NOT NULL DEFAULT 'B1';

-- Re-map existing writing challenges from the old 3-tier scale to the CEFR scale
-- now used by both users.language_level and writing_challenges.level.
UPDATE writing_challenges
SET level = CASE level
    WHEN 'BEGINNER' THEN 'A2'
    WHEN 'INTERMEDIATE' THEN 'B1'
    WHEN 'ADVANCED' THEN 'C1'
    ELSE level
END;
