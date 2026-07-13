-- Truncate regeneratable data because the options JSON format changes (type discriminator removed)
TRUNCATE TABLE user_practice_results CASCADE;
TRUNCATE TABLE user_practices CASCADE;
TRUNCATE TABLE practices CASCADE;

ALTER TABLE practices
    ADD COLUMN question VARCHAR(1000);
