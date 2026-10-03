-- Drop every foreign key constraint in the schema (no more DB-level FK enforcement
-- anywhere - ON DELETE CASCADE behavior tied to these FKs goes away too), then widen
-- the meanings/practices primary keys to (id, user_id). The old single-column PKs
-- were the last thing blocking that widening: Postgres requires a referenced column
-- set to be covered by its own unique/PK constraint, and (id, user_id) no longer
-- covers plain `id`, which is what practices.meaning_id, meaning_categories.meaning_id,
-- user_practices.practice_id and user_practice_results.practice_id pointed at.

DO $$
DECLARE
    fk RECORD;
BEGIN
    FOR fk IN
        SELECT conname, conrelid::regclass::text AS table_name
        FROM pg_constraint
        WHERE contype = 'f'
          AND connamespace = 'public'::regnamespace
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', fk.table_name, fk.conname);
    END LOOP;
END $$;

ALTER TABLE meanings DROP CONSTRAINT meanings_pkey;
ALTER TABLE meanings ADD CONSTRAINT meanings_pkey PRIMARY KEY (id, user_id);

ALTER TABLE practices DROP CONSTRAINT practices_pkey;
ALTER TABLE practices ADD CONSTRAINT practices_pkey PRIMARY KEY (id, user_id);
