-- Persist each model's requests-per-minute window so it survives server restarts instead of
-- resetting in memory.
ALTER TABLE listening_voice_generation_stats
    ADD COLUMN rpm_window_start TIMESTAMP,
    ADD COLUMN rpm_count        INT NOT NULL DEFAULT 0;
