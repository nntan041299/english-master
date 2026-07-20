-- Clarify that this counter is scoped to request_count_date, not a lifetime total.
ALTER TABLE ai_limit_stats RENAME COLUMN request_count TO request_per_date_count;
