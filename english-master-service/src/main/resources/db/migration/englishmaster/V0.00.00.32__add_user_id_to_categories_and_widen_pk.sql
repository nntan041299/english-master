-- Denormalize category ownership onto categories, same as meanings/practices before it.
-- Categories were previously shared globally by name; from here on each user gets their
-- own copy. Backfill user_id from the meanings that reference each category via
-- meaning_categories: a category referenced by only one user's meanings is assigned to
-- that user directly, while a category referenced by several users' meanings is
-- duplicated once per additional user, with those users' meaning_categories rows
-- repointed to their own copy. No foreign key is added on user_id, consistent with the
-- rest of the schema after V0.00.00.31 dropped all FK constraints.

ALTER TABLE categories ADD COLUMN user_id BIGINT;

-- 1. Assign each existing category to one of its owning users (the lowest user_id).
WITH first_owner AS (
    SELECT DISTINCT ON (mc.category_id) mc.category_id, m.user_id
    FROM meaning_categories mc
             JOIN meanings m ON m.id = mc.meaning_id
    ORDER BY mc.category_id, m.user_id
)
UPDATE categories c
SET user_id = fo.user_id
FROM first_owner fo
WHERE fo.category_id = c.id;

-- 2. Duplicate categories for every additional owner and repoint their meaning_categories rows.
DO
$$
    DECLARE
        rec               RECORD;
        new_category_id   BIGINT;
    BEGIN
        FOR rec IN
            SELECT DISTINCT mc.category_id AS old_category_id, m.user_id AS owner_id, c.name AS name
            FROM meaning_categories mc
                     JOIN meanings m ON m.id = mc.meaning_id
                     JOIN categories c ON c.id = mc.category_id
            WHERE m.user_id <> c.user_id
            LOOP
                SELECT id
                INTO new_category_id
                FROM categories
                WHERE name = rec.name
                  AND user_id = rec.owner_id;

                IF new_category_id IS NULL THEN
                    INSERT INTO categories (name, user_id, created_at, updated_at)
                    VALUES (rec.name, rec.owner_id, NOW(), NOW())
                    RETURNING id INTO new_category_id;
                END IF;

                UPDATE meaning_categories
                SET category_id = new_category_id
                WHERE category_id = rec.old_category_id
                  AND meaning_id IN (SELECT id FROM meanings WHERE user_id = rec.owner_id);

                new_category_id := NULL;
            END LOOP;
    END
$$;

-- 3. Categories with no meanings have no ownership signal to backfill from; drop them.
DELETE FROM categories WHERE user_id IS NULL;

ALTER TABLE categories ALTER COLUMN user_id SET NOT NULL;

CREATE INDEX idx_categories_user_id ON categories (user_id);

-- 4. Uniqueness on name is now per-user, not global.
ALTER TABLE categories DROP CONSTRAINT categories_name_key;
ALTER TABLE categories ADD CONSTRAINT categories_name_user_id_key UNIQUE (name, user_id);

-- 5. Widen the primary key to (id, user_id). No incoming foreign keys reference
-- categories.id anymore (every FK in the schema was dropped in V0.00.00.31), so nothing
-- else blocks the widening.
ALTER TABLE categories DROP CONSTRAINT categories_pkey;
ALTER TABLE categories ADD CONSTRAINT categories_pkey PRIMARY KEY (id, user_id);
