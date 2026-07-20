-- Track voice-generation usage per Gemini TTS model, so the caller can switch to the next
-- configured model once one of them hits its request quota.
ALTER TABLE listening_voice_generation_stats
    ADD COLUMN model VARCHAR(100);

UPDATE listening_voice_generation_stats
SET model = 'gemini-2.5-flash-preview-tts'
WHERE model IS NULL;

ALTER TABLE listening_voice_generation_stats
    ALTER COLUMN model SET NOT NULL;

ALTER TABLE listening_voice_generation_stats
    ADD CONSTRAINT uq_listening_voice_generation_stats_model UNIQUE (model);
