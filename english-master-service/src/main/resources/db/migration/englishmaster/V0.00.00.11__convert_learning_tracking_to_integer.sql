ALTER TABLE user_practices ALTER COLUMN learning_tracking DROP DEFAULT;

ALTER TABLE user_practices
    ALTER COLUMN learning_tracking TYPE INTEGER
    USING CASE learning_tracking
        WHEN 'TRACKING1' THEN 1
        WHEN 'TRACKING2' THEN 2
        WHEN 'TRACKING3' THEN 3
        WHEN 'TRACKING4' THEN 4
        WHEN 'TRACKING5' THEN 5
        WHEN 'FINISH'    THEN 6
        ELSE 1
    END;

ALTER TABLE user_practices ALTER COLUMN learning_tracking SET DEFAULT 1;
