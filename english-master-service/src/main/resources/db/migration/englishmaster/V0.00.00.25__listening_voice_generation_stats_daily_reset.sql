-- Track the date each model's request_count applies to, so usage resets automatically once a new
-- calendar day starts instead of accumulating forever.
ALTER TABLE listening_voice_generation_stats
    ADD COLUMN request_count_date DATE NOT NULL DEFAULT CURRENT_DATE;
