-- This table now tracks usage for every Gemini model (both v1 content-generation models and
-- v1beta TTS models), not just listening voice generation, so rename it to reflect that.
ALTER TABLE listening_voice_generation_stats RENAME TO ai_limit_stats;
